package com.example.bodhak.ui.ProjectAnalysis.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.namespace.NamespaceInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class NamespaceOverviewState {

    private final ObservableList<NamespaceItem> namespaces =
            FXCollections.observableArrayList();

    public void update(AnalysisContext context) {

        namespaces.clear();

        context.getSnapshot()
                .namespaces()
                .values()
                .stream()
                .sorted(Comparator.comparing(NamespaceInfo::getNamespaceName))
                .forEach(namespace ->

                        namespaces.add(

                                new NamespaceItem(

                                        namespace.getNamespaceName(),

                                        namespace.getEntities().size()

                                )
                        )
                );
    }

    public ObservableList<NamespaceItem> getNamespaces() {
        return namespaces;
    }
}
