package com.example.bodhakfrontend.projectAnalysis.fixes;

import com.example.bodhakfrontend.Models.FixSuggestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FixSuggestionStore {

        private final List<FixSuggestion> fixes = new ArrayList<>();

        public FixSuggestionStore() {
            load();
        }

        private void load() {
            try (InputStream is =
                         FixSuggestionStore.class
                                 .getResourceAsStream("/fixes/class_fixes.json")) {

                if (is == null) {
                    System.out.println("❌ Fix suggestions JSON not found");
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                fixes.addAll(
                        mapper.readValue(is, new TypeReference<List<FixSuggestion>>() {})
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<FixSuggestion> getAll() {
            return fixes;
        }
}


