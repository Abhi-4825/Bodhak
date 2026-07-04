package com.example.bodhak.ui.dependencyExplorer.components;

import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyGraphLayoutEngine {

    private static final double COL_OFFSET = 260.0;
    private static final double VERTICAL_SPACING = 70.0;

    /**
     * Calculates the dynamic preferred height for the graph card based on neighbor complexity.
     */
    public double getPreferredHeight(int incomingCount, int outgoingCount) {
        int maxColCount = Math.max(incomingCount, outgoingCount);
        if (maxColCount <= 4) {
            return 500.0;
        } else if (maxColCount <= 8) {
            return 650.0;
        } else if (maxColCount <= 12) {
            return 800.0;
        } else {
            return Math.min(1200.0, 500.0 + maxColCount * VERTICAL_SPACING);
        }
    }

    /**
     * Computes the X and Y coordinates for all cells relative to the center.
     */
    public Map<String, Point2D> computeLayout(String focusName, List<String> incomingNames, List<String> outgoingNames, double width, double height) {
        Map<String, Point2D> positions = new HashMap<>();

        double cx = width / 2.0;
        double cy = height / 2.0;

        // 1. Focus Node placed exactly in the center
        positions.put(focusName, new Point2D(cx - 60, cy - 22));

        // 2. Incoming Nodes (Used By) on the Left
        int nIn = incomingNames.size();
        if (nIn > 0) {
            double startYIn = cy - ((nIn - 1) * VERTICAL_SPACING) / 2.0;
            for (int i = 0; i < nIn; i++) {
                double x = cx - COL_OFFSET - 60;
                double y = startYIn + i * VERTICAL_SPACING - 22;
                positions.put(incomingNames.get(i), new Point2D(x, y));
            }
        }

        // 3. Outgoing Nodes (Depends On) on the Right
        int nOut = outgoingNames.size();
        if (nOut > 0) {
            double startYOut = cy - ((nOut - 1) * VERTICAL_SPACING) / 2.0;
            for (int j = 0; j < nOut; j++) {
                double x = cx + COL_OFFSET - 60;
                double y = startYOut + j * VERTICAL_SPACING - 22;
                positions.put(outgoingNames.get(j), new Point2D(x, y));
            }
        }

        return positions;
    }
}
