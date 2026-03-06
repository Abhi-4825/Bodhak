package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.GAResult;
import com.example.bodhakfrontend.Nic.GAloopRunner;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import javafx.concurrent.Task;

public class OptimizationController {
private final RightPanelTabManager  rightPanelTabManager;
private final ProjectInfo projectInfo;
private OptimizationPanel optimizationPanel;


    public OptimizationController(RightPanelTabManager rightPanelTabManager, ProjectInfo projectInfo) {
        this.rightPanelTabManager = rightPanelTabManager;
        this.projectInfo = projectInfo;
    }

    public void startOptimization(){
        optimizationPanel=new OptimizationPanel();
        rightPanelTabManager.openOptimizationTab(()->optimizationPanel.getRoot());
        GAloopRunner runner = new GAloopRunner();

        Task<GAResult> task = runner.createTask(
                projectInfo,
                message -> optimizationPanel.appendMessage(message)
        );

        task.setOnSucceeded(event -> {
            GAResult result = task.getValue();
            optimizationPanel.setOnTypingFinished(()->showFinalReport(result));

        });

        new Thread(task, "GA-Thread").start();
    }

    private void showFinalReport(GAResult result) {

        OptimizationReportPanel reportPanel =
                new OptimizationReportPanel(result);

        optimizationPanel.replaceContent(
                reportPanel.getRoot()
        );
    }




}
