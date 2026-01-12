package com.example.bodhakfrontend.projectAnalysis.warning;

import com.example.bodhakfrontend.Models.WarningRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WarningRuleStore {

    private final List<WarningRule> rules = new ArrayList<>();

    public WarningRuleStore() {
        load();
    }

    private void load() {
        try (InputStream is =
                     WarningRuleStore.class
                             .getResourceAsStream("/warnings/class_warnings.json")) {

            if (is == null) {
                System.out.println("❌ JSON FILE NOT FOUND");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<WarningRule> loaded =
                    mapper.readValue(is, new TypeReference<List<WarningRule>>() {});

            System.out.println("✅ Loaded warning rules: " + loaded.size());
            rules.addAll(loaded);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<WarningRule> getRules() {
        return rules;
    }


}

