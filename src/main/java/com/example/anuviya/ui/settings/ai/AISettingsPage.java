package com.example.anuviya.ui.settings.ai;

import com.example.anuviya.analyzer.ai.platform.AIPlatform;
import com.example.anuviya.analyzer.ai.platform.session.AISession;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry;
import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.PackageState;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallationService;
import com.example.anuviya.platform.registry.LocalPackageDb;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.example.anuviya.platform.state.PlatformState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class AISettingsPage extends BorderPane {
    private final TabPane tabPane = new TabPane();

    public AISettingsPage() {
        setStyle("-fx-background-color: #0e1415;");

        Tab providerTab = new Tab("Provider", createProviderPane());
        Tab modelsTab = new Tab("Models", createModelsPane());
        Tab benchmarksTab = new Tab("Benchmarks", createBenchmarksPane());
        Tab systemTab = new Tab("System", createSystemPane());
        Tab logsTab = new Tab("Logs", createLogsPane());

        for (Tab tab : List.of(providerTab, modelsTab, benchmarksTab, systemTab, logsTab)) {
            tab.setClosable(false);
        }

        tabPane.getTabs().addAll(providerTab, modelsTab, benchmarksTab, systemTab, logsTab);
        tabPane.setStyle("-fx-background-color: #0e1415;");
        
        setCenter(tabPane);
    }

    private Node createProviderPane() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #0e1415;");

        Label title = new Label("AI PROVIDER CONFIGURATION");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLbl = new Label("Status:");
        statusLbl.setStyle("-fx-text-fill: #849494;");
        Label statusVal = new Label("Offline");
        statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
        statusBox.getChildren().addAll(statusLbl, statusVal);

        CheckBox autoStartCb = new CheckBox("Automatically start provider on Anuviya launch");
        autoStartCb.setStyle("-fx-text-fill: #dde4e5;");
        autoStartCb.setSelected(getAutoStartPreference());
        autoStartCb.setOnAction(e -> saveAutoStartPreference(autoStartCb.isSelected()));

        Button startStopBtn = new Button("Start Local Runtime");
        startStopBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        // Listen to state changes
        PlatformState.currentProperty().addListener((obs, oldState, newState) -> {
            if (newState != null) {
                Platform.runLater(() -> updateProviderUI(newState, statusVal, startStopBtn));
            }
        });

        if (PlatformState.getCurrent() != null) {
            updateProviderUI(PlatformState.getCurrent(), statusVal, startStopBtn);
        }

        startStopBtn.setOnAction(e -> {
            AIProvider active = ProviderRegistry.getInstance().getActive();
            if (active != null) {
                if (active.runtimeStatus() == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY) {
                    active.stop();
                } else {
                    active.start();
                }
                Platform.runLater(() -> {
                    System.out.println("Toggling active provider runtime state...");
                    try { Thread.sleep(500); } catch (Exception ex) {}
                    AIPlatform.getInstance().rebuildState();
                });
            }
        });

        VBox recBox = new VBox(8);
        recBox.setPadding(new Insets(12));
        recBox.setStyle("-fx-background-color: rgba(0, 218, 243, 0.05); -fx-border-color: rgba(0, 218, 243, 0.2); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label recTitle = new Label("RECOMMENDED LOCAL PROVIDER");
        recTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #00daf3; -fx-letter-spacing: 0.1em;");
        
        Label recDesc = new Label("Anuviya uses Ollama to run local open-source models securely on your machine. Click below to download and install it.");
        recDesc.setWrapText(true);
        recDesc.setStyle("-fx-text-fill: #849494; -fx-font-size: 12px;");

        Hyperlink downloadLink = new Hyperlink("Download & Install Ollama (ollama.com)");
        downloadLink.setStyle("-fx-text-fill: #00daf3; -fx-underline: true; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 0 0 0; -fx-cursor: hand;");
        downloadLink.setOnAction(evt -> {
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("https://ollama.com"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        recBox.getChildren().addAll(recTitle, recDesc, downloadLink);

        box.getChildren().addAll(title, statusBox, autoStartCb, startStopBtn, recBox);
        return box;
    }

    private void updateProviderUI(PlatformState state, Label statusVal, Button startStopBtn) {
        AIProvider provider = state.getPreferredProvider();
        if (provider == null) {
            statusVal.setText("No Provider Installed");
            statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
            startStopBtn.setDisable(true);
            startStopBtn.setText("Start Runtime");
        } else {
            startStopBtn.setDisable(false);
            var status = provider.runtimeStatus();
            if (status == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY) {
                statusVal.setText("Running");
                statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;");
                startStopBtn.setText("Stop Runtime");
                startStopBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            } else if (status == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.STARTING) {
                statusVal.setText("Starting...");
                statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #eab308;");
                startStopBtn.setDisable(true);
            } else {
                statusVal.setText("Offline");
                statusVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
                startStopBtn.setText("Start Runtime");
                startStopBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        }
    }

    private Node createModelsPane() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #0e1415;");

        Label title = new Label("MODEL CATALOG");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        VBox catalogList = new VBox(10);
        ScrollPane scroll = new ScrollPane(catalogList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #0e1415;");

        // Populate catalog from registry
        List<ServicePackage> models = PackageRegistry.getInstance().byCategory(PackageCategory.AI_MODEL);
        for (ServicePackage model : models) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8));
            row.setStyle("-fx-background-color: #12191a; -fx-background-radius: 6; -fx-border-color: #242b2c; -fx-border-radius: 6;");

            VBox meta = new VBox(4);
            Label name = new Label(model.displayName() + " (" + model.sizeGb() + " GB)");
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #dde4e5;");
            Label desc = new Label(model.description());
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494;");
            meta.getChildren().addAll(name, desc);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button actionBtn = new Button();
            actionBtn.setStyle("-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            
            ProgressBar pBar = new ProgressBar(0);
            pBar.setPrefWidth(120);
            pBar.setVisible(false);
            pBar.setManaged(false);

            // Bind/Update action button based on state
            updateModelRowState(model.id(), actionBtn, pBar);
            
            PlatformState.currentProperty().addListener((obs, oldState, newState) -> {
                Platform.runLater(() -> updateModelRowState(model.id(), actionBtn, pBar));
            });

            actionBtn.setOnAction(e -> {
                boolean isInstalled = LocalPackageDb.isInstalled(model.id());
                if (isInstalled) {
                    LocalPackageDb.unregister(model.id());
                    AIPlatform.getInstance().rebuildState();
                } else {
                    actionBtn.setVisible(false);
                    actionBtn.setManaged(false);
                    pBar.setVisible(true);
                    pBar.setManaged(true);
                    
                    InstallationService.getInstance().install(model.id(),
                        progress -> {
                            Platform.runLater(() -> pBar.setProgress(progress));
                        },
                        complete -> {
                            Platform.runLater(() -> {
                                pBar.setVisible(false);
                                pBar.setManaged(false);
                                actionBtn.setVisible(true);
                                actionBtn.setManaged(true);
                                AIPlatform.getInstance().rebuildState();
                            });
                        },
                        fail -> {
                            Platform.runLater(() -> {
                                pBar.setVisible(false);
                                pBar.setManaged(false);
                                actionBtn.setVisible(true);
                                actionBtn.setManaged(true);
                                System.err.println("Failed model install: " + fail);
                            });
                        }
                    );
                }
            });

            row.getChildren().addAll(meta, spacer, pBar, actionBtn);
            catalogList.getChildren().add(row);
        }

        Hyperlink ollamaLibraryLink = new Hyperlink("Browse Ollama Model Library (ollama.com/library)");
        ollamaLibraryLink.setStyle("-fx-text-fill: #00daf3; -fx-underline: true; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 0 0 0; -fx-cursor: hand;");
        ollamaLibraryLink.setOnAction(evt -> {
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("https://ollama.com/library"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(title, scroll, ollamaLibraryLink);
        return box;
    }

    private void updateModelRowState(String packageId, Button actionBtn, ProgressBar pBar) {
        boolean isInstalled = LocalPackageDb.isInstalled(packageId);
        PackageState activeState = InstallationService.getInstance().getPackageState(packageId);

        if (activeState == PackageState.DOWNLOADING || activeState == PackageState.VERIFYING || activeState == PackageState.INSTALLING) {
            actionBtn.setVisible(false);
            actionBtn.setManaged(false);
            pBar.setVisible(true);
            pBar.setManaged(true);
        } else {
            actionBtn.setVisible(true);
            actionBtn.setManaged(true);
            pBar.setVisible(false);
            pBar.setManaged(false);
            
            if (isInstalled) {
                actionBtn.setText("Remove");
                actionBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            } else {
                actionBtn.setText("Download");
                actionBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            }
        }
    }

    private Node createBenchmarksPane() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #0e1415;");

        Label title = new Label("AI INFERENCE BENCHMARK");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Button runBtn = new Button("Run Benchmark Test");
        runBtn.setStyle("-fx-background-color: linear-gradient(to right, #4bf6ff, #8bfd91); -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        Label resultLbl = new Label("Results: Not run yet.");
        resultLbl.setStyle("-fx-text-fill: #849494; -fx-font-size: 13px;");

        runBtn.setOnAction(e -> {
            runBtn.setDisable(true);
            resultLbl.setText("Running benchmark on active model...");
            
            Thread thread = new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    AISession session = AIPlatform.getInstance().openSession("architecture");
                    com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage testPrompt =
                        new com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage("Benchmark", "Say hello in 5 words.", null, null);
                    String resp = session.getProvider().generate(session, testPrompt);
                    long delay = System.currentTimeMillis() - start;
                    
                    int words = resp.split("\\s+").length;
                    double tokPerSec = Math.round((words * 1.3 / (delay / 1000.0)) * 10.0) / 10.0;
                    Platform.runLater(() -> {
                        resultLbl.setText("Benchmark Successful!\nModel: " + session.getModel().displayName() + "\nSpeed: " + tokPerSec + " tokens/sec\nLatency: " + delay + " ms\nResponse: " + resp);
                        runBtn.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        resultLbl.setText("Benchmark failed: " + ex.getMessage());
                        runBtn.setDisable(false);
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        box.getChildren().addAll(title, runBtn, resultLbl);
        return box;
    }

    private Node createSystemPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(16));
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #0e1415;");

        PlatformState.currentProperty().addListener((obs, oldState, newState) -> {
            if (newState != null) {
                Platform.runLater(() -> populateSystemGrid(grid, newState));
            }
        });

        if (PlatformState.getCurrent() != null) {
            populateSystemGrid(grid, PlatformState.getCurrent());
        }

        return grid;
    }

    private void populateSystemGrid(GridPane grid, PlatformState state) {
        grid.getChildren().clear();
        var snapshot = state.getEnvironment();
        if (snapshot == null) return;

        String[][] specs = {
            {"Operating System", snapshot.os()},
            {"System RAM", snapshot.hardware().ramGb() + " GB"},
            {"Processor (CPU)", snapshot.hardware().cpuName()},
            {"Graphics Card (GPU)", snapshot.hardware().gpuName()},
            {"Video RAM (VRAM)", snapshot.hardware().vramGb() + " GB"},
            {"Free Disk Space", snapshot.disk().freeGb() + " GB"}
        };

        for (int i = 0; i < specs.length; i++) {
            Label label = new Label(specs[i][0]);
            label.setStyle("-fx-text-fill: #849494; -fx-font-weight: bold;");
            Label val = new Label(specs[i][1]);
            val.setStyle("-fx-text-fill: #dde4e5;");
            grid.add(label, 0, i);
            grid.add(val, 1, i);
        }
    }

    private Node createLogsPane() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-control-inner-background: #0c1011; -fx-text-fill: #dde4e5; -fx-font-family: monospace;");
        area.setText("System logs initialized...\nScanning directories...\nBootstrap loaded successfully.\n");
        return area;
    }

    private boolean getAutoStartPreference() {
        String userHome = System.getProperty("user.home");
        File config = new File(userHome, ".gemini/antigravity/onboarding.properties");
        if (config.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(config)) {
                props.load(fis);
                String val = props.getProperty("ai.provider.autoStart");
                if (val != null) {
                    return Boolean.parseBoolean(val);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return true;
    }

    private void saveAutoStartPreference(boolean autoStart) {
        String userHome = System.getProperty("user.home");
        File config = new File(userHome, ".gemini/antigravity/onboarding.properties");
        Properties props = new Properties();
        if (config.exists()) {
            try (FileInputStream fis = new FileInputStream(config)) {
                props.load(fis);
            } catch (IOException e) {
                // ignore
            }
        }
        props.setProperty("ai.provider.autoStart", String.valueOf(autoStart));
        try (FileOutputStream fos = new FileOutputStream(config)) {
            props.store(fos, "Updated AutoStart preference");
        } catch (IOException e) {
            // ignore
        }
    }
}
