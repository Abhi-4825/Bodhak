package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.classification.Capability;
import com.example.bodhak.context.AnalysisContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectSurfacesState implements AnalysisReportSection {

    public record SurfaceEntry(String name, String icon, String detail) {}

    private final ObservableList<SurfaceEntry> surfaces = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        surfaces.clear();
        if (context == null) return;
        
        var classificationResult = context.getClassificationResult();
        if (classificationResult != null && classificationResult.capabilityProfile() != null) {
            var profile = classificationResult.capabilityProfile();
            
            for (Capability cap : profile.detectedCapabilities()) {
                surfaces.add(new SurfaceEntry(
                    formatName(cap.name()), 
                    getIconForCapability(cap), 
                    "Detected" // Placeholder for detailed count
                ));
            }
        }
        
        // Add placeholders for Executable, Library Export, Documentation if not present
        surfaces.add(new SurfaceEntry("Executable", "▶️", "Placeholder"));
        surfaces.add(new SurfaceEntry("Documentation", "📄", "Placeholder"));
    }

    private String formatName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String getIconForCapability(Capability capability) {
        return switch (capability) {
            case HTTP_ENDPOINT -> "🌐";
            case WEB_UI -> "💻";
            case DESKTOP_UI -> "🖥️";
            case MOBILE_UI -> "📱";
            case CLI -> "⌨️";
            case DATABASE_ACCESS -> "🗄️";
            case SECURITY -> "🔒";
            case MESSAGE_QUEUE -> "📨";
            case WEBSOCKET -> "🔌";
            case GRAPHQL -> "🕸️";
            case SERVERLESS_FUNCTION -> "⚡";
            case BACKGROUND_JOB -> "⚙️";
            case TEMPLATE_RENDERING -> "🖼️";
            case STATIC_SITE_GENERATION -> "📄";
            case FILE_IO -> "📁";
            case GRPC -> "📞";
            case SCHEDULED_TASK -> "⏱️";
            case DEPENDENCY_INJECTION -> "💉";
            case ORM -> "🗃️";
            case TESTING -> "🧪";
            case CONFIGURATION_MANAGEMENT -> "🔧";
            default -> "🧩";
        };
    }

    public ObservableList<SurfaceEntry> getSurfaces() {
        return surfaces;
    }
}
