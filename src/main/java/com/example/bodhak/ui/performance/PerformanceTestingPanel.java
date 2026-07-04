package com.example.bodhak.ui.performance;

import com.example.bodhak.context.ApiSurface;
import com.example.bodhak.model.endpoint.ServiceEndpoint;
import com.example.bodhak.analyzer.gatling.model.LoadTestConfiguration;
import com.example.bodhak.analyzer.gatling.model.LoadTestRequest;
import com.example.bodhak.analyzer.gatling.model.PerformanceReport;
import com.example.bodhak.analyzer.gatling.orchestration.PerformanceTestService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

/**
 * JavaFX panel for the Performance Testing feature.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────┐
 *   │  ⚡  Performance Testing                     │
 *   ├──────────────────────────────────────────────┤
 *   │  Base URL:      [ http://localhost:8080     ] │
 *   │  Endpoint:      [ ComboBox ▼               ] │
 *   │  Virtual Users: [ 10  ]                       │
 *   │  Ramp-Up (s):   [ 5   ]                       │
 *   │  Duration (s):  [ 30  ]                       │
 *   │  [ Run Load Test ]                            │
 *   ├──────────────────────────────────────────────┤
 *   │  Output:                                      │
 *   │  ┌────────────────────────────────────────┐  │
 *   │  │  (live console output appears here)    │  │
 *   │  └────────────────────────────────────────┘  │
 *   └──────────────────────────────────────────────┘
 */
public class PerformanceTestingPanel {

    private final PerformanceTestService service;
    private ComboBox<ServiceEndpoint>   endpointCombo;
    private TextField                   baseUrlField;
    private TextField                   virtualUsersField;
    private TextField                   rampUpField;
    private TextField                   durationField;
    private TextArea                    outputArea;
    private Button                      runButton;

    public PerformanceTestingPanel() {
        this.service = new PerformanceTestService();
    }

    // ── Public builder ─────────────────────────────────────────────────────────

    /**
     * Builds and returns the complete JavaFX panel node.
     *
     * @param apiSurface The endpoints discovered by Bodhak's endpoint discovery pipeline.
     *                   May be null if no project is loaded yet.
     */
    public Node build(ApiSurface apiSurface) {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #1e1e2e;");

        root.getChildren().addAll(
            buildHeader(),
            buildForm(apiSurface),
            buildOutputSection()
        );

        return root;
    }

    // ── Header ─────────────────────────────────────────────────────────────────

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setStyle("""
            -fx-background-color: #181825;
            -fx-border-color: #313244;
            -fx-border-width: 0 0 1 0;
        """);

        Label icon  = new Label("⚡");
        icon.setStyle("-fx-font-size: 18;");

        Label title = new Label("Performance Testing");
        title.setStyle("""
            -fx-font-family: 'Segoe UI';
            -fx-font-size: 16;
            -fx-font-weight: bold;
            -fx-text-fill: #cdd6f4;
        """);

        Label badge = new Label("V1 · GET endpoints");
        badge.setStyle("""
            -fx-background-color: #313244;
            -fx-text-fill: #89b4fa;
            -fx-font-size: 10;
            -fx-padding: 3 8;
            -fx-background-radius: 10;
        """);

        header.getChildren().addAll(icon, title, badge);
        return header;
    }

    // ── Form ───────────────────────────────────────────────────────────────────

    private Node buildForm(ApiSurface apiSurface) {
        VBox form = new VBox(14);
        form.setPadding(new Insets(20, 20, 16, 20));
        form.setStyle("-fx-background-color: #1e1e2e;");

        // Base URL
        baseUrlField = styledTextField("http://localhost:8080");
        form.getChildren().add(formRow("Base URL", baseUrlField));

        // Endpoint ComboBox
        endpointCombo = new ComboBox<>();
        endpointCombo.setMaxWidth(Double.MAX_VALUE);
        endpointCombo.setStyle(comboStyle());
        endpointCombo.setCellFactory(lv -> endpointCell());
        endpointCombo.setButtonCell(endpointCell());

        if (apiSurface != null && !apiSurface.isEmpty()) {
            List<ServiceEndpoint> gets = apiSurface.getEndpoints().stream()
                .filter(e -> "GET".equalsIgnoreCase(e.httpMethod()))
                .toList();
            endpointCombo.getItems().addAll(gets);
            if (!gets.isEmpty()) endpointCombo.getSelectionModel().selectFirst();
        }

        if (endpointCombo.getItems().isEmpty()) {
            endpointCombo.setPromptText("No GET endpoints discovered — check endpoint discovery");
        }

        form.getChildren().add(formRow("Endpoint", endpointCombo));

        // Numeric fields
        virtualUsersField = styledTextField("10");
        rampUpField       = styledTextField("5");
        durationField     = styledTextField("30");

        HBox numericRow = new HBox(12);
        numericRow.getChildren().addAll(
            formColumn("Virtual Users", virtualUsersField),
            formColumn("Ramp-Up (s)", rampUpField),
            formColumn("Duration (s)", durationField)
        );
        form.getChildren().add(numericRow);

        // Run button
        runButton = new Button("▶  Run Load Test");
        runButton.setMaxWidth(Double.MAX_VALUE);
        runButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #89b4fa, #74c7ec);
            -fx-text-fill: #1e1e2e;
            -fx-font-weight: bold;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-cursor: hand;
        """);
        runButton.setOnMouseEntered(e -> runButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #74c7ec, #89dceb);
            -fx-text-fill: #1e1e2e;
            -fx-font-weight: bold;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-cursor: hand;
        """));
        runButton.setOnMouseExited(e -> runButton.setStyle("""
            -fx-background-color: linear-gradient(to right, #89b4fa, #74c7ec);
            -fx-text-fill: #1e1e2e;
            -fx-font-weight: bold;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-cursor: hand;
        """));
        runButton.setOnAction(e -> handleRunTest());
        form.getChildren().add(runButton);

        return form;
    }

    // ── Output Section ─────────────────────────────────────────────────────────

    private Node buildOutputSection() {
        VBox section = new VBox(8);
        section.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(section, Priority.ALWAYS);

        Label label = new Label("Output");
        label.setStyle("""
            -fx-text-fill: #6c7086;
            -fx-font-size: 11;
            -fx-font-weight: bold;
        """);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("""
            -fx-font-family: 'JetBrains Mono', 'Cascadia Code', monospace;
            -fx-font-size: 12;
            -fx-background-color: #181825;
            -fx-text-fill: #cdd6f4;
            -fx-control-inner-background: #181825;
            -fx-border-color: #313244;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
        """);
        outputArea.setPromptText("Test output will appear here...");
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        section.getChildren().addAll(label, outputArea);
        return section;
    }

    // ── Event Handler ──────────────────────────────────────────────────────────

    private void handleRunTest() {
        ServiceEndpoint selected = endpointCombo.getValue();
        if (selected == null) {
            appendOutput("❌ Please select an endpoint first.");
            return;
        }

        String baseUrl = baseUrlField.getText().strip();
        if (baseUrl.isBlank()) {
            appendOutput("❌ Please enter a base URL (e.g. http://localhost:8080).");
            return;
        }

        int virtualUsers, rampUp, duration;
        try {
            virtualUsers = Integer.parseInt(virtualUsersField.getText().strip());
            rampUp       = Integer.parseInt(rampUpField.getText().strip());
            duration     = Integer.parseInt(durationField.getText().strip());
        } catch (NumberFormatException ex) {
            appendOutput("❌ Virtual Users, Ramp-Up, and Duration must be integers.");
            return;
        }

        // Disable button during run
        runButton.setDisable(true);
        runButton.setText("⏳ Running...");
        outputArea.clear();

        LoadTestConfiguration config  = new LoadTestConfiguration(virtualUsers, rampUp, duration);
        LoadTestRequest request = new LoadTestRequest(selected, config);

        Task<PerformanceReport> task = new Task<>() {
            @Override
            protected PerformanceReport call() throws Exception {
                return service.runLoadTest(request, baseUrl,
                    line -> Platform.runLater(() -> appendOutput(line)));
            }
        };

        task.setOnSucceeded(e -> {
            runButton.setDisable(false);
            runButton.setText("▶  Run Load Test");
        });

        task.setOnFailed(e -> {
            runButton.setDisable(false);
            runButton.setText("▶  Run Load Test");
            Throwable ex = task.getException();
            appendOutput("❌ Test failed: " + ex.getMessage());
            ex.printStackTrace();
        });

        new Thread(task, "Bodhak-Gatling-Thread").start();
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────

    private void appendOutput(String line) {
        outputArea.appendText(line + "\n");
    }

    private Node formRow(String labelText, Node control) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12;");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private Node formColumn(String labelText, Node control) {
        VBox box = new VBox(5);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12;");
        ((Region) control).setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private TextField styledTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setStyle("""
            -fx-background-color: #313244;
            -fx-text-fill: #cdd6f4;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-padding: 8 10;
            -fx-border-color: #45475a;
            -fx-border-radius: 8;
        """);
        return tf;
    }

    private String comboStyle() {
        return """
            -fx-background-color: #313244;
            -fx-text-fill: #cdd6f4;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-border-color: #45475a;
            -fx-border-radius: 8;
        """;
    }

    private ListCell<ServiceEndpoint> endpointCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ServiceEndpoint item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.httpMethod() + "  " + item.fullPath());
                    setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 12;");
                }
            }
        };
    }
}
