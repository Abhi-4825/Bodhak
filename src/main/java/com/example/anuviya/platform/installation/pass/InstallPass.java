package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;

public interface InstallPass {
    enum PassResult {
        SUCCESS,
        SKIPPED,
        FAILED
    }

    String name();
    PassResult execute(InstallRequest request, InstallContext ctx);
}
