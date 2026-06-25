package com.example.bodhakfrontend.core.analysis.entityflag;

public record ProjectBaselines(

        int p90Loc,

        int p90Methods,

        int p90Fields,

        int p90FanIn,

        int p90FanOut

) {}
