package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.Model.GAResult;
import com.example.bodhakfrontend.Nic.GAloopRunner;
import com.example.bodhakfrontend.Nic.Model.OptimizationReport;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.concurrent.Task;

public class OptimizationController {
private final RightPanelTabManager  rightPanelTabManager;
private final ProjectInfo projectInfo;
private OptimizationPanel optimizationPanel;
private final UiFeatures uiFeatures;


    public OptimizationController(RightPanelTabManager rightPanelTabManager, ProjectInfo projectInfo, UiFeatures uiFeatures) {
        this.rightPanelTabManager = rightPanelTabManager;
        this.projectInfo = projectInfo;
        this.uiFeatures = uiFeatures;
    }

    public void startOptimization(){
        optimizationPanel=new OptimizationPanel();
        rightPanelTabManager.openOptimizationTab(()->optimizationPanel.getRoot());
        GAloopRunner runner = new GAloopRunner();

        Task<OptimizationReport> task = runner.createTask(
                projectInfo,
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
