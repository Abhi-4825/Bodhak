package com.example.anuviya.ui.ProjectAnalysis.state;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.EntityKind;

import java.io.File;

public record EntityMetricItem(

        String qualifiedName,

        String simpleName,

        String namespace,

        int methods,

        int fields,

        int constructors,

        int loc,

        boolean isAbstract,

        boolean isFinal,

        boolean isPublic,

        EntityKind kind,

        File sourceFile,

        int beginLine,

        int beginColumn

) {

    public EntityMetricItem(EntityInfo entity) {

        this(

                entity.getEntityName(),

                entity.getSimpleName(),

                entity.getNamespaceName(),

                (int) entity.getMethodCount(),

                entity.getFields().size(),

                (int) entity.getConstructorCount(),

                entity.getLinesOfCode(),

                entity.isAbstract(),

                entity.isFinal(),

                entity.isPublic(),

                entity.getKind(),

                entity.getSourceFile(),

                entity.getBeginLine(),

                entity.getBeginColumn()

        );

    }

}
