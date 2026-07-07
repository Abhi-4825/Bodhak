package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.bodhak.ui.dependencyExplorer.state.SelectedEntityState;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class SelectedEntityPanel extends ScrollPane {

    private final DependencyExplorerState state;
    private final SelectedEntityState detailsState;
    private final VBox content = new VBox(16);
    private VBox emptyPlaceholder;

    public SelectedEntityPanel(DependencyExplorerState state) {
        this.state = state;
        this.detailsState = state.getSelectedEntityState();
        initialise();
    }

    private void initialise() {
        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("dd-scroll-pane");
        setStyle("-fx-background: #151c23; -fx-background-color: #151c23; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(132, 147, 150, 0.08);");

        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #151c23;");
        setContent(content);

        emptyPlaceholder = new VBox(8);
        emptyPlaceholder.setAlignment(Pos.CENTER);
        emptyPlaceholder.setPadding(new Insets(60, 20, 60, 20));
        Label emptyLbl = new Label("Select an entity from the Entity Browser");
        emptyLbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 12px; -fx-font-family: 'JetBrains Mono';");
        emptyPlaceholder.getChildren().add(emptyLbl);

        // Bind panel rendering to the selectedEntityProperty
        state.selectedEntityProperty().addListener((obs, old, entity) -> {
            content.getChildren().clear();
            if (entity == null) {
                content.getChildren().add(emptyPlaceholder);
            } else {
                renderFullDetails();
            }
        });

        // Trigger initial view
        if (state.selectedEntityProperty().get() == null) {
            content.getChildren().add(emptyPlaceholder);
        } else {
            renderFullDetails();
        }
    }

    private void renderFullDetails() {
        // 1. Section Title
        Label sectionTitle = new Label("SELECTED ENTITY");
        sectionTitle.getStyleClass().add("dd-card-title");

        // 2. Identity Block (Entity Name + Namespace + Kind Badge)
        VBox identityBox = new VBox(6);
        Label nameLbl = new Label();
        nameLbl.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    String full = detailsState.nameProperty().get();
                    if (full == null) return "";
                    int lastDot = full.lastIndexOf('.');
                    return lastDot == -1 ? full : full.substring(lastDot + 1);
                },
                detailsState.nameProperty()
        ));
        nameLbl.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label namespaceLbl = new Label();
        namespaceLbl.textProperty().bind(detailsState.namespaceProperty());
        namespaceLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #849396;");
        Label kindChip = new Label();
        kindChip.textProperty().bind(detailsState.kindProperty());
        kindChip.setStyle("-fx-font-size: 9px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0,218,243,0.1); -fx-padding: 2 6; -fx-background-radius: 4;");
        metaRow.getChildren().addAll(namespaceLbl, kindChip);
        identityBox.getChildren().addAll(nameLbl, metaRow);

        // 3. Metrics Grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(12);

        metricsGrid.add(createMetricCell("LOC", detailsState.locProperty().asString()), 0, 0);
        metricsGrid.add(createMetricCell("FAN-OUT", detailsState.fanOutProperty().asString()), 1, 0);
        metricsGrid.add(createMetricCell("FAN-IN", detailsState.fanInProperty().asString()), 2, 0);

        metricsGrid.add(createMetricCell("DEPTH", detailsState.depthProperty().asString()), 0, 1);
        metricsGrid.add(createMetricCell("REFERENCES", detailsState.referenceCountProperty().asString()), 1, 1);
        metricsGrid.add(createMetricCell("INSTABILITY", Bindings.format("%.2f", detailsState.instabilityProperty())), 2, 1);

        // 4. Depends On List
        VBox dependsOnBox = buildDependencyList("DEPENDS ON", detailsState.getDependsOn(), true);

        // 5. Used By List
        VBox usedByBox = buildDependencyList("USED BY", detailsState.getUsedBy(), false);

        content.getChildren().addAll(
                sectionTitle,
                identityBox,
                createSeparator(),
                metricsGrid,
                createSeparator(),
                dependsOnBox,
                createSeparator(),
                usedByBox
        );
    }

    private Separator createSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: rgba(132, 147, 150, 0.08); -fx-border-width: 0 0 1 0;");
        return sep;
    }

    private VBox createMetricCell(String labelStr, javafx.beans.value.ObservableValue<String> valProp) {
        VBox cell = new VBox(2);
        Label lbl = new Label(labelStr);
        lbl.getStyleClass().add("dd-metric-label");
        Label val = new Label();
        val.textProperty().bind(valProp);
        val.getStyleClass().add("dd-metric-val");
        cell.getChildren().addAll(lbl, val);
        return cell;
    }

    private VBox buildDependencyList(String title, javafx.collections.ObservableList<String> list, boolean isDependsOn) {
        VBox box = new VBox(8);
        Label header = new Label(title);
        header.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849396;");
        
        VBox listRows = new VBox(4);
        listRows.setStyle("-fx-background-color: transparent;");

        list.addListener((javafx.collections.ListChangeListener<String>) c -> {
            listRows.getChildren().clear();
            for (String item : list) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                
                DependencyRowData rowData = resolveDependencyInfo(
                        isDependsOn ? detailsState.nameProperty().get() : item,
                        isDependsOn ? item : detailsState.nameProperty().get()
                );

                Label dirIcon = new Label(isDependsOn ? "→" : "←");
                dirIcon.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + resolveColor(rowData.type) + ";");
                dirIcon.setMinWidth(Region.USE_PREF_SIZE);
                
                int lastDot = item.lastIndexOf('.');
                String simpleName = lastDot == -1 ? item : item.substring(lastDot + 1);
                Label name = new Label(simpleName);
                name.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 12px; -fx-font-weight: bold;");

                Label refType = new Label(rowData.type);
                refType.setStyle("-fx-text-fill: " + resolveColor(rowData.type) + "; -fx-font-size: 9px; -fx-background-color: rgba(132, 147, 150, 0.08); -fx-padding: 1 4; -fx-background-radius: 3;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label count = new Label(rowData.count > 0 ? rowData.count + " refs" : "");
                count.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px; -fx-font-family: 'JetBrains Mono';");

                Label arrow = new Label("→");
                arrow.setStyle("-fx-text-fill: " + resolveColor(rowData.type) + "; -fx-font-size: 12px;");

                row.getChildren().addAll(dirIcon, name, refType, spacer, count, arrow);
                
                row.setOnMouseEntered(e -> {
                    row.setStyle("-fx-padding: 6 8; -fx-background-color: rgba(0,218,243,0.08); -fx-background-radius: 4; -fx-cursor: hand;");
                    if (isDependsOn) {
                        state.getDependencyGraphState().hoveredEdgeSourceProperty().set(detailsState.nameProperty().get());
                        state.getDependencyGraphState().hoveredEdgeTargetProperty().set(item);
                    } else {
                        state.getDependencyGraphState().hoveredEdgeSourceProperty().set(item);
                        state.getDependencyGraphState().hoveredEdgeTargetProperty().set(detailsState.nameProperty().get());
                    }
                });
                
                row.setOnMouseExited(e -> {
                    row.setStyle("-fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                    state.getDependencyGraphState().hoveredEdgeSourceProperty().set("");
                    state.getDependencyGraphState().hoveredEdgeTargetProperty().set("");
                });
                
                row.setOnMouseClicked(e -> {
                    if (state.analysisContextProperty().get() != null) {
                        state.analysisContextProperty().get().findEntity(item)
                             .ifPresent(entity -> state.selectedEntityProperty().set(entity));
                    }
                });

                listRows.getChildren().add(row);
            }
            if (list.isEmpty()) {
                Label empty = new Label("None");
                empty.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-padding: 4;");
                listRows.getChildren().add(empty);
            }
        });

        // Trigger initial population of lists
        listRows.getChildren().clear();
        for (String item : list) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
            
            DependencyRowData rowData = resolveDependencyInfo(
                    isDependsOn ? detailsState.nameProperty().get() : item,
                    isDependsOn ? item : detailsState.nameProperty().get()
            );

            Label dirIcon = new Label(isDependsOn ? "→" : "←");
            dirIcon.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + resolveColor(rowData.type) + ";");
            dirIcon.setMinWidth(Region.USE_PREF_SIZE);
            
            int lastDot = item.lastIndexOf('.');
            String simpleName = lastDot == -1 ? item : item.substring(lastDot + 1);
            Label name = new Label(simpleName);
            name.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 12px; -fx-font-weight: bold;");

            Label refType = new Label(rowData.type);
            refType.setStyle("-fx-text-fill: " + resolveColor(rowData.type) + "; -fx-font-size: 9px; -fx-background-color: rgba(132, 147, 150, 0.08); -fx-padding: 1 4; -fx-background-radius: 3;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label count = new Label(rowData.count > 0 ? rowData.count + " refs" : "");
            count.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px; -fx-font-family: 'JetBrains Mono';");

            Label arrow = new Label("→");
            arrow.setStyle("-fx-text-fill: " + resolveColor(rowData.type) + "; -fx-font-size: 12px;");

            row.getChildren().addAll(dirIcon, name, refType, spacer, count, arrow);
            
            row.setOnMouseEntered(e -> {
                row.setStyle("-fx-padding: 6 8; -fx-background-color: rgba(0,218,243,0.08); -fx-background-radius: 4; -fx-cursor: hand;");
                if (isDependsOn) {
                    state.getDependencyGraphState().hoveredEdgeSourceProperty().set(detailsState.nameProperty().get());
                    state.getDependencyGraphState().hoveredEdgeTargetProperty().set(item);
                } else {
                    state.getDependencyGraphState().hoveredEdgeSourceProperty().set(item);
                    state.getDependencyGraphState().hoveredEdgeTargetProperty().set(detailsState.nameProperty().get());
                }
            });
            
            row.setOnMouseExited(e -> {
                row.setStyle("-fx-padding: 6 8; -fx-background-radius: 4; -fx-cursor: hand;");
                state.getDependencyGraphState().hoveredEdgeSourceProperty().set("");
                state.getDependencyGraphState().hoveredEdgeTargetProperty().set("");
            });
            
            row.setOnMouseClicked(e -> {
                if (state.analysisContextProperty().get() != null) {
                    state.analysisContextProperty().get().findEntity(item)
                         .ifPresent(entity -> state.selectedEntityProperty().set(entity));
                }
            });

            listRows.getChildren().add(row);
        }
        if (list.isEmpty()) {
            Label empty = new Label("None");
            empty.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-padding: 4;");
            listRows.getChildren().add(empty);
        }

        box.getChildren().addAll(header, listRows);
        return box;
    }

    private DependencyRowData resolveDependencyInfo(String src, String dst) {
        if (state.analysisContextProperty().get() == null || state.analysisContextProperty().get().getReferenceDatabase() == null) {
            return new DependencyRowData("IMPORT", 0);
        }
        var refs = state.analysisContextProperty().get().getReferenceDatabase().getAllReferences().stream()
                .filter(r -> r.sourceSymbol().name().startsWith(src) && r.targetSymbol().name().startsWith(dst))
                .toList();
        if (refs.isEmpty()) {
            return new DependencyRowData("IMPORT", 0);
        }
        String kind = refs.get(0).kind().name();
        return new DependencyRowData(kind, refs.size());
    }

    private String resolveColor(String type) {
        return switch (type.toUpperCase()) {
            case "CALL" -> "#00daf3";       // Bright Blue (Method Call)
            case "MEMBER" -> "#00e676";     // Green (Field Reference)
            case "TYPE" -> "#ffa726";       // Orange (Type Reference)
            case "ANNOTATION" -> "#ec407a"; // Purple/Pink (Annotation)
            case "FRAMEWORK" -> "#4bf6ff";  // Cyan (Framework)
            default -> "#849396";           // Gray (Import / Other)
        };
    }

    private static class DependencyRowData {
        String type;
        int count;
        public DependencyRowData(String type, int count) {
            this.type = type;
            this.count = count;
        }
    }
}
