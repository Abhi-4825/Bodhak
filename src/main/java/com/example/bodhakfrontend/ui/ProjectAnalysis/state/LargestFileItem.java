package com.example.bodhakfrontend.ui.ProjectAnalysis.state;


import java.io.File;

public record LargestFileItem(

        String name,

        File sourceFile,

        int loc

) {}
