package com.example.bodhak.context;

import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.reference.SemanticReference;
import com.example.bodhak.compiler.symbol.Symbol;
import com.example.bodhak.compiler.symbol.EntitySymbol;
import com.example.bodhak.compiler.symbol.MemberSymbol;

import java.util.*;

public class SemanticGraphIndex {

    private final Map<String, Integer> entityNameToId = new HashMap<>();
    private final List<EntityInfo> idToEntity = new ArrayList<>();

    private int[][] forwardAdj;
    private int[][] reverseAdj;

    private int[] entityToSccId;
    private int[][] sccToEntities;
    private int[][] sccAdj;

    private String getEntityQualifiedName(Symbol symbol) {
        if (symbol instanceof EntitySymbol) {
            return symbol.name();
        } else if (symbol instanceof MemberSymbol) {
            return ((MemberSymbol) symbol).parent().name();
        }
        return symbol.name();
    }

    public SemanticGraphIndex(AnalysisContext context) {
        buildIndex(context);
    }

    private void buildIndex(AnalysisContext context) {
        if (context == null || context.getEntities() == null) return;

        // 1. Map entities to integer IDs
        int idCounter = 0;
        for (EntityInfo entity : context.getEntities()) {
            entityNameToId.put(entity.getEntityName(), idCounter);
            idToEntity.add(entity);
            idCounter++;
        }

        int numEntities = idCounter;

        // Use temporary lists to build adjacency
        List<List<Integer>> tempForwardAdj = new ArrayList<>(numEntities);
        List<List<Integer>> tempReverseAdj = new ArrayList<>(numEntities);
        for (int i = 0; i < numEntities; i++) {
            tempForwardAdj.add(new ArrayList<>());
            tempReverseAdj.add(new ArrayList<>());
        }

        // 2. Build adjacency lists
        if (context.getReferenceDatabase() != null) {
            for (SemanticReference ref : context.getReferenceDatabase().getAllReferences()) {
                String srcName = getEntityQualifiedName(ref.sourceSymbol());
                String dstName = getEntityQualifiedName(ref.targetSymbol());

                Integer srcId = entityNameToId.get(srcName);
                Integer dstId = entityNameToId.get(dstName);

                if (srcId != null && dstId != null) {
                    tempForwardAdj.get(srcId).add(dstId);
                    tempReverseAdj.get(dstId).add(srcId);
                }
            }
        }

        // Convert lists to arrays
        forwardAdj = new int[numEntities][];
        reverseAdj = new int[numEntities][];
        for (int i = 0; i < numEntities; i++) {
            forwardAdj[i] = tempForwardAdj.get(i).stream().mapToInt(Integer::intValue).toArray();
            reverseAdj[i] = tempReverseAdj.get(i).stream().mapToInt(Integer::intValue).toArray();
        }

        // 3. Compute SCCs using Iterative Kosaraju's Algorithm (with integers)
        computeStronglyConnectedComponents(numEntities);
    }

    private void computeStronglyConnectedComponents(int numEntities) {
        // Pass 1: Iterative DFS on forward graph to compute finishing order
        int[] finishOrder = new int[numEntities];
        int finishIndex = 0;
        boolean[] visited = new boolean[numEntities];

        for (int startNode = 0; startNode < numEntities; startNode++) {
            if (!visited[startNode]) {
                int[] stack = new int[numEntities];
                int stackSize = 0;
                stack[stackSize++] = startNode;
                visited[startNode] = true;

                int[] edgeIndex = new int[numEntities];

                while (stackSize > 0) {
                    int curr = stack[stackSize - 1];
                    int[] neighbors = forwardAdj[curr];
                    int idx = edgeIndex[curr];

                    int next = -1;
                    if (neighbors != null && idx < neighbors.length) {
                        for (int i = idx; i < neighbors.length; i++) {
                            int n = neighbors[i];
                            if (!visited[n]) {
                                next = n;
                                edgeIndex[curr] = i + 1;
                                break;
                            }
                        }
                        if (next == -1) {
                            edgeIndex[curr] = neighbors.length;
                        }
                    }

                    if (next != -1) {
                        visited[next] = true;
                        stack[stackSize++] = next;
                    } else {
                        stackSize--;
                        finishOrder[finishIndex++] = curr;
                    }
                }
            }
        }

        // Pass 2: Iterative DFS on reverse graph to extract components
        int sccIdCounter = 0;
        boolean[] visitedRev = new boolean[numEntities];
        entityToSccId = new int[numEntities];
        Arrays.fill(entityToSccId, -1);
        
        List<List<Integer>> tempSccToEntities = new ArrayList<>();

        for (int i = finishOrder.length - 1; i >= 0; i--) {
            int startNode = finishOrder[i];
            if (!visitedRev[startNode]) {
                List<Integer> component = new ArrayList<>();
                int[] stack = new int[numEntities];
                int stackSize = 0;
                stack[stackSize++] = startNode;
                visitedRev[startNode] = true;

                while (stackSize > 0) {
                    int curr = stack[--stackSize];
                    component.add(curr);
                    entityToSccId[curr] = sccIdCounter;

                    int[] revNeighbors = reverseAdj[curr];
                    if (revNeighbors != null) {
                        for (int n : revNeighbors) {
                            if (!visitedRev[n]) {
                                visitedRev[n] = true;
                                stack[stackSize++] = n;
                            }
                        }
                    }
                }

                tempSccToEntities.add(component);
                sccIdCounter++;
            }
        }

        sccToEntities = new int[sccIdCounter][];
        for (int i = 0; i < sccIdCounter; i++) {
            sccToEntities[i] = tempSccToEntities.get(i).stream().mapToInt(Integer::intValue).toArray();
        }

        // 4. Build Condensed SCC DAG Adjacency
        buildSccDagAdjacency(sccIdCounter);
    }

    private void buildSccDagAdjacency(int numSccs) {
        List<Set<Integer>> tempSccAdj = new ArrayList<>(numSccs);
        for (int i = 0; i < numSccs; i++) {
            tempSccAdj.add(new HashSet<>());
        }

        for (int srcScc = 0; srcScc < numSccs; srcScc++) {
            int[] nodes = sccToEntities[srcScc];
            Set<Integer> targetSccs = tempSccAdj.get(srcScc);

            for (int node : nodes) {
                int[] neighbors = forwardAdj[node];
                if (neighbors != null) {
                    for (int neighbor : neighbors) {
                        int dstScc = entityToSccId[neighbor];
                        if (dstScc != srcScc && dstScc != -1) {
                            targetSccs.add(dstScc);
                        }
                    }
                }
            }
        }

        sccAdj = new int[numSccs][];
        for (int i = 0; i < numSccs; i++) {
            sccAdj[i] = tempSccAdj.get(i).stream().mapToInt(Integer::intValue).toArray();
        }
    }

    // --- Registry Methods ---
    public int getEntityId(String qualifiedName) {
        return entityNameToId.getOrDefault(qualifiedName, -1);
    }

    public String getEntityName(int id) {
        if (id >= 0 && id < idToEntity.size()) {
            return idToEntity.get(id).getEntityName();
        }
        return null;
    }

    public EntityInfo getEntityInfo(int id) {
        if (id >= 0 && id < idToEntity.size()) {
            return idToEntity.get(id);
        }
        return null;
    }

    // --- Graph Array Getters ---
    public int[] getForwardEdges(int id) {
        if (id >= 0 && id < forwardAdj.length) {
            return forwardAdj[id];
        }
        return new int[0];
    }

    public int[] getReverseEdges(int id) {
        if (id >= 0 && id < reverseAdj.length) {
            return reverseAdj[id];
        }
        return new int[0];
    }

    public int getSccId(int entityId) {
        if (entityId >= 0 && entityId < entityToSccId.length) {
            return entityToSccId[entityId];
        }
        return -1;
    }

    public int[] getSccEntities(int sccId) {
        if (sccId >= 0 && sccId < sccToEntities.length) {
            return sccToEntities[sccId];
        }
        return new int[0];
    }

    public int[] getSccForwardEdges(int sccId) {
        if (sccId >= 0 && sccId < sccAdj.length) {
            return sccAdj[sccId];
        }
        return new int[0];
    }

    public int getEntityCount() {
        return idToEntity.size();
    }
}
