package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.optimization.GAloopRunner;
import com.example.bodhakfrontend.engine.optimization.Model.OptimizationReport;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.ui.helper.UiFeatures;
import javafx.concurrent.Task;

public class OptimizationController {
private final RightPanelTabManager  rightPanelTabManager;
private final AnalysisContext context;
private OptimizationPanel optimizationPanel;
private final UiFeatures uiFeatures;


    public OptimizationController(RightPanelTabManager rightPanelTabManager, AnalysisContext context, UiFeatures uiFeatures) {
        this.rightPanelTabManager = rightPanelTabManager;
        this.context = context;
        this.uiFeatures = uiFeatures;
    }

    public void startOptimization(){
        optimizationPanel=new OptimizationPanel();
        rightPanelTabManager.openOptimizationTab(()->optimizationPanel.getRoot());
        GAloopRunner runner = new GAloopRunner();

        Task<OptimizationReport> task = runner.createTask(
                context,
                message -> optimizationPanel.appendMessage(message)
        );

        task.setOnSucceeded(event -> {

            optimizationPanel.setOnTypingFinished(()->showFinalReport(task.getValue()));

        });

        new Thread(task, "GA-Thread").start();
    }

    private void showFinalReport(OptimizationReport result) {

        OptimizationReportPanel reportPanel =
                new OptimizationReportPanel(result,uiFeatures);

        optimizationPanel.replaceContent(
                reportPanel.getRoot()
        );
    }




}
