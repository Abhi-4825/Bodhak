package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.SemanticGraphIndex;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.reference.SemanticReference;
import com.example.bodhak.compiler.symbol.Symbol;
import com.example.bodhak.compiler.symbol.EntitySymbol;
import com.example.bodhak.compiler.symbol.MemberSymbol;

import java.util.*;

public class PathQueryEngine {

    public static String getEntityQualifiedName(Symbol symbol) {
        if (symbol instanceof EntitySymbol) {
            return symbol.name();
        } else if (symbol instanceof MemberSymbol) {
            return ((MemberSymbol) symbol).parent().name();
        }
        return symbol.name();
    }

    public static class PathQueryResult {
        private final List<EntityPath> paths = new ArrayList<>();
        private long searchTimeMs;
        private boolean connected;
        private int totalEntitiesTraversed;
        private int totalReferencesTraversed;
        private int circularCount;
        private int externalCount;

        public List<EntityPath> getPaths() { return paths; }
        public long getSearchTimeMs() { return searchTimeMs; }
        public boolean isConnected() { return connected; }
        public int getTotalEntitiesTraversed() { return totalEntitiesTraversed; }
        public int getTotalReferencesTraversed() { return totalReferencesTraversed; }
        public int getCircularCount() { return circularCount; }
        public int getExternalCount() { return externalCount; }
    }

    /**
     * Executes shortest path query using Bidirectional BFS.
     */
    public PathQueryResult queryShortestPath(SemanticGraphIndex index, AnalysisContext context, String start, String end) {
        PathQueryResult result = new PathQueryResult();
        long startTime = System.currentTimeMillis();

        if (index == null || start == null || end == null) {
            result.searchTimeMs = System.currentTimeMillis() - startTime;
            return result;
        }

        List<String> pathNodes = findShortestPathBidirectional(index, start, end);
        if (!pathNodes.isEmpty()) {
            result.paths.add(new EntityPath(pathNodes, new ArrayList<>())); // Lazy connections
            result.connected = true;
            result.totalEntitiesTraversed = pathNodes.size();
        }

        result.circularCount = detectCycles(index, start);
        result.searchTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * Executes alternative paths query using bounded, iterative BFS.
     */
    public PathQueryResult queryAlternativePaths(SemanticGraphIndex index, AnalysisContext context, String start, String end, int maxDepth, int limit) {
        PathQueryResult result = new PathQueryResult();
        long startTime = System.currentTimeMillis();

        if (index == null || start == null || end == null) {
            result.searchTimeMs = System.currentTimeMillis() - startTime;
            return result;
        }

        List<List<String>> paths = findAlternativePathsBFS(index, start, end, maxDepth, limit);
        Set<String> uniqueEntities = new HashSet<>();
        for (List<String> pathNodes : paths) {
            result.paths.add(new EntityPath(pathNodes, new ArrayList<>()));
            uniqueEntities.addAll(pathNodes);
        }

        if (!paths.isEmpty()) {
            result.connected = true;
            result.totalEntitiesTraversed = uniqueEntities.size();
        }

        result.circularCount = detectCycles(index, start);
        result.searchTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }

    /**
     * Executes deepest dependency chain query using SCC Condensed DAG.
     */
    public PathQueryResult queryDependencyChain(SemanticGraphIndex index, AnalysisContext context, String start) {
        PathQueryResult result = new PathQueryResult();
        long startTime = System.currentTimeMillis();

        if (index == null || start == null) {
            result.searchTimeMs = System.currentTimeMillis() - startTime;
            return result;
        }

        List<String> chainNodes = findDeepestChainSCC(index, start);
        if (!chainNodes.isEmpty()) {
            result.paths.add(new EntityPath(chainNodes, new ArrayList<>()));
            result.connected = true;
            result.totalEntitiesTraversed = chainNodes.size();
        }

        result.circularCount = detectCycles(index, start);
        result.searchTimeMs = System.currentTimeMillis() - startTime;
        return result;
    }

    private List<String> findShortestPathBidirectional(SemanticGraphIndex index, String start, String end) {
        int startId = index.getEntityId(start);
        int endId = index.getEntityId(end);
        
        if (startId == -1 || endId == -1) return Collections.emptyList();
        if (startId == endId) return Collections.singletonList(start);
        
        Queue<Integer> forwardQueue = new LinkedList<>();
        Queue<Integer> backwardQueue = new LinkedList<>();
        
        Map<Integer, Integer> forwardParent = new HashMap<>();
        Map<Integer, Integer> backwardParent = new HashMap<>();
        
        forwardQueue.add(startId);
        forwardParent.put(startId, -1);
        
        backwardQueue.add(endId);
        backwardParent.put(endId, -1);
        
        int collisionNode = -1;
        
        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            // Expand forward
            int currF = forwardQueue.poll();
            int[] neighborsF = index.getForwardEdges(currF);
            if (neighborsF != null) {
                for (int next : neighborsF) {
                    if (!forwardParent.containsKey(next)) {
                        forwardParent.put(next, currF);
                        forwardQueue.add(next);
                    }
                    if (backwardParent.containsKey(next)) {
                        collisionNode = next;
                        break;
                    }
                }
            }
            if (collisionNode != -1) break;
            
            // Expand backward
            int currB = backwardQueue.poll();
            int[] neighborsB = index.getReverseEdges(currB);
            if (neighborsB != null) {
                for (int next : neighborsB) {
                    if (!backwardParent.containsKey(next)) {
                        backwardParent.put(next, currB);
                        backwardQueue.add(next);
                    }
                    if (forwardParent.containsKey(next)) {
                        collisionNode = next;
                        break;
                    }
                }
            }
            if (collisionNode != -1) break;
        }
        
        if (collisionNode == -1) {
            return Collections.emptyList();
        }
        
        // Reconstruct path
        List<String> path = new ArrayList<>();
        int curr = collisionNode;
        while (curr != -1) {
            path.add(0, index.getEntityName(curr));
            curr = forwardParent.get(curr);
        }
        
        curr = backwardParent.get(collisionNode);
        while (curr != -1) {
            if (curr != collisionNode) {
                path.add(index.getEntityName(curr));
            }
            curr = backwardParent.get(curr);
        }
        
        return path;
    }

    private List<List<String>> findAlternativePathsBFS(SemanticGraphIndex index, String start, String end, int maxDepth, int limit) {
        List<List<String>> results = new ArrayList<>();
        int startId = index.getEntityId(start);
        int endId = index.getEntityId(end);
        
        if (startId == -1 || endId == -1) return results;
        if (startId == endId) {
            results.add(Collections.singletonList(start));
            return results;
        }

        Queue<List<Integer>> queue = new LinkedList<>();
        queue.add(Collections.singletonList(startId));

        int explored = 0;
        int maxExplored = 50000;

        while (!queue.isEmpty() && results.size() < limit && explored < maxExplored) {
            explored++;
            List<Integer> path = queue.poll();
            if (path.size() > maxDepth) continue;

            int last = path.get(path.size() - 1);
            if (last == endId) {
                List<String> stringPath = new ArrayList<>();
                for (int id : path) {
                    stringPath.add(index.getEntityName(id));
                }
                results.add(stringPath);
                continue;
            }

            int[] neighbors = index.getForwardEdges(last);
            if (neighbors != null) {
                for (int next : neighbors) {
                    if (!path.contains(next)) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(next);
                        queue.add(newPath);
                    }
                }
            }
        }
        return results;
    }

    private List<String> findDeepestChainSCC(SemanticGraphIndex index, String start) {
        int startId = index.getEntityId(start);
        if (startId == -1) return Collections.emptyList();
        
        int startScc = index.getSccId(startId);
        if (startScc == -1) return Collections.emptyList();

        Set<Integer> reachableSccs = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startScc);
        reachableSccs.add(startScc);

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            int[] neighbors = index.getSccForwardEdges(curr);
            if (neighbors != null) {
                for (int next : neighbors) {
                    if (!reachableSccs.contains(next)) {
                        reachableSccs.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        Map<Integer, Integer> inDegrees = new HashMap<>();
        for (int scc : reachableSccs) {
            inDegrees.put(scc, 0);
        }

        for (int scc : reachableSccs) {
            int[] neighbors = index.getSccForwardEdges(scc);
            if (neighbors != null) {
                for (int next : neighbors) {
                    if (reachableSccs.contains(next)) {
                        inDegrees.put(next, inDegrees.get(next) + 1);
                    }
                }
            }
        }

        Queue<Integer> topoQueue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegrees.entrySet()) {
            if (entry.getValue() == 0) {
                topoQueue.add(entry.getKey());
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!topoQueue.isEmpty()) {
            int curr = topoQueue.poll();
            topoOrder.add(curr);

            int[] neighbors = index.getSccForwardEdges(curr);
            if (neighbors != null) {
                for (int next : neighbors) {
                    if (reachableSccs.contains(next)) {
                        int deg = inDegrees.get(next) - 1;
                        inDegrees.put(next, deg);
                        if (deg == 0) {
                            topoQueue.add(next);
                        }
                    }
                }
            }
        }

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        for (int scc : reachableSccs) {
            dist.put(scc, 1);
            parent.put(scc, null);
        }

        for (int u : topoOrder) {
            int[] neighbors = index.getSccForwardEdges(u);
            if (neighbors != null) {
                for (int v : neighbors) {
                    if (reachableSccs.contains(v)) {
                        if (dist.get(v) < dist.get(u) + 1) {
                            dist.put(v, dist.get(u) + 1);
                            parent.put(v, u);
                        }
                    }
                }
            }
        }

        int maxScc = startScc;
        int maxDist = 1;
        for (Map.Entry<Integer, Integer> entry : dist.entrySet()) {
            if (entry.getValue() > maxDist) {
                maxDist = entry.getValue();
                maxScc = entry.getKey();
            }
        }

        LinkedList<Integer> sccChain = new LinkedList<>();
        Integer curr = maxScc;
        while (curr != null) {
            sccChain.addFirst(curr);
            curr = parent.get(curr);
        }

        List<String> pathNodes = new ArrayList<>();
        for (int scc : sccChain) {
            int[] entities = index.getSccEntities(scc);
            if (entities != null && entities.length > 0) {
                if (scc == startScc) {
                    pathNodes.add(start);
                    for (int entId : entities) {
                        if (entId != startId) {
                            pathNodes.add(index.getEntityName(entId));
                        }
                    }
                } else {
                    for (int entId : entities) {
                        pathNodes.add(index.getEntityName(entId));
                    }
                }
            }
        }

        return pathNodes;
    }

    private int detectCycles(SemanticGraphIndex index, String startNode) {
        int startId = index.getEntityId(startNode);
        if (startId == -1) return 0;
        
        int numEntities = index.getEntityCount();
        boolean[] visited = new boolean[numEntities];
        boolean[] recStack = new boolean[numEntities];
        int cycleCount = 0;

        int[] stack = new int[numEntities];
        int stackSize = 0;
        stack[stackSize++] = startId;
        visited[startId] = true;
        recStack[startId] = true;

        int[] edgeIndex = new int[numEntities];

        while (stackSize > 0) {
            int curr = stack[stackSize - 1];
            int[] neighbors = index.getForwardEdges(curr);
            int idx = edgeIndex[curr];

            if (neighbors != null && idx < neighbors.length) {
                int next = neighbors[idx];
                edgeIndex[curr] = idx + 1;
                
                if (!visited[next]) {
                    visited[next] = true;
                    recStack[next] = true;
                    stack[stackSize++] = next;
                } else if (recStack[next]) {
                    cycleCount++;
                }
            } else {
                stackSize--;
                recStack[curr] = false;
            }
        }

        return cycleCount;
    }

    /**
     * Lazily resolves detailed connections for the single selected path.
     */
    public static List<PathConnection> resolvePathConnections(EntityPath path, AnalysisContext context) {
        List<PathConnection> connections = new ArrayList<>();
        List<String> nodes = path.getEntities();

        // Optimize by pre-filtering references in a single pass O(V + E)
        Set<String> pathEdges = new HashSet<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            pathEdges.add(nodes.get(i) + "->" + nodes.get(i + 1));
        }

        Map<String, List<SemanticReference>> edgeRefs = new HashMap<>();
        for (SemanticReference ref : context.getReferenceDatabase().getAllReferences()) {
            String src = getEntityQualifiedName(ref.sourceSymbol());
            String dst = getEntityQualifiedName(ref.targetSymbol());
            String edge = src + "->" + dst;
            if (pathEdges.contains(edge)) {
                edgeRefs.computeIfAbsent(edge, k -> new ArrayList<>()).add(ref);
            }
        }

        for (int i = 0; i < nodes.size() - 1; i++) {
            String from = nodes.get(i);
            String to = nodes.get(i + 1);
            String edge = from + "->" + to;

            List<SemanticReference> refs = edgeRefs.getOrDefault(edge, Collections.emptyList());

            String refType = refs.isEmpty() ? "IMPORT" : refs.get(0).kind().name();
            int count = refs.size();

            StringBuilder details = new StringBuilder();
            if (!refs.isEmpty()) {
                details.append("Uses ");
                if (refs.get(0).sourceSymbol() instanceof MemberSymbol) {
                    details.append(refs.get(0).sourceSymbol().name()).append("() -> ");
                }
                details.append(refs.get(0).targetSymbol().name());
                if (refs.get(0).sourceSymbol() instanceof MemberSymbol) {
                    details.append("()");
                }
                if (count > 1) {
                    details.append(" (+").append(count - 1).append(" more refs)");
                }
            } else {
                details.append("Import declaration reference");
            }

            connections.add(new PathConnection(from, to, refType, count, details.toString()));
        }
        return connections;
    }

    public static EntityPath extractCycleFromSCC(SemanticGraphIndex index, Set<String> sccNodes) {
        if (sccNodes == null || sccNodes.isEmpty()) {
            return new EntityPath(Collections.emptyList(), Collections.emptyList());
        }

        Set<Integer> sccIds = new HashSet<>();
        for (String node : sccNodes) {
            int id = index.getEntityId(node);
            if (id != -1) {
                sccIds.add(id);
            }
        }

        if (sccIds.isEmpty()) {
            return new EntityPath(Collections.emptyList(), Collections.emptyList());
        }

        int startNode = sccIds.iterator().next();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> parentMap = new HashMap<>();
        Set<Integer> recStack = new HashSet<>();
        List<Integer> cycle = findCycleDFS(startNode, index, sccIds, visited, recStack, parentMap);

        if (cycle != null && !cycle.isEmpty()) {
            List<String> entityNames = new ArrayList<>();
            for (int id : cycle) {
                entityNames.add(index.getEntityName(id));
            }
            return new EntityPath(entityNames, Collections.emptyList());
        }

        return new EntityPath(new ArrayList<>(sccNodes), Collections.emptyList());
    }

    private static List<Integer> findCycleDFS(int curr, SemanticGraphIndex index, Set<Integer> sccIds,
                                             Set<Integer> visited, Set<Integer> recStack, Map<Integer, Integer> parentMap) {
        visited.add(curr);
        recStack.add(curr);

        int[] neighbors = index.getForwardEdges(curr);
        if (neighbors != null) {
            for (int next : neighbors) {
                if (!sccIds.contains(next)) continue;

                if (!visited.contains(next)) {
                    parentMap.put(next, curr);
                    List<Integer> cycle = findCycleDFS(next, index, sccIds, visited, recStack, parentMap);
                    if (cycle != null) return cycle;
                } else if (recStack.contains(next)) {
                    List<Integer> cycle = new ArrayList<>();
                    int temp = curr;
                    cycle.add(next);
                    List<Integer> sub = new ArrayList<>();
                    while (temp != next && temp != -1) {
                        sub.add(0, temp);
                        temp = parentMap.getOrDefault(temp, -1);
                    }
                    if (temp == next) {
                        cycle.addAll(sub);
                        cycle.add(next);
                        return cycle;
                    }
                }
            }
        }

        recStack.remove(curr);
        return null;
    }
}
