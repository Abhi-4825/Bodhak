package com.example.anuviya.analyzer.ai.ui;

import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallationService;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class NoProviderPane extends VBox {
    private final Label titleLabel = new Label("AI Model Required");
    private final Label detailLabel = new Label("An AI model is required to perform this analysis.");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Button downloadBtn = new Button("Download Model");
    private final Button cancelBtn = new Button("Cancel");
    private final HBox btnBox = new HBox(12);

    private final String targetModelId;
    private final Runnable onCompleteCallback;
    private final Runnable onCancelCallback;

    public NoProviderPane(String targetModelId, Runnable onCompleteCallback, Runnable onCancelCallback) {
        this.targetModelId = targetModelId;
        this.onCompleteCallback = onCompleteCallback;
        this.onCancelCallback = onCancelCallback;

        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #12191a; -fx-background-radius: 8; -fx-border-color: #2f3637; -fx-border-radius: 8;");
        setMaxWidth(500);
        setMaxHeight(250);

        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(targetModelId);
        if (pkgOpt.isPresent()) {
            detailLabel.setText("Analysis requires the model: " + pkgOpt.get().displayName() + " (" + pkgOpt.get().sizeGb() + " GB).");
        } else {
            detailLabel.setText("Analysis requires a compatible model (" + targetModelId + ").");
        }
        detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        detailLabel.setWrapText(true);

        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #22c55e;");
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        downloadBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        downloadBtn.setOnAction(e -> startDownload());

        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            if (onCancelCallback != null) onCancelCallback.run();
        });

        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(downloadBtn, cancelBtn);

        getChildren().addAll(titleLabel, detailLabel, progressBar, btnBox);
    }

    private void startDownload() {
        btnBox.setVisible(false);
        btnBox.setManaged(false);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressBar.setProgress(0);

        // Run download
        InstallationService.getInstance().install(targetModelId,
            progress -> {
                Platform.runLater(() -> progressBar.setProgress(progress));
            },
            complete -> {
                Platform.runLater(() -> {
                    if (onCompleteCallback != null) onCompleteCallback.run();
                });
            },
            fail -> {
                Platform.runLater(() -> {
                    detailLabel.setText("Download failed: " + fail);
                    btnBox.setVisible(true);
                    btnBox.setManaged(true);
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                });
            }
        );
    }
}
