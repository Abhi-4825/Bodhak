package com.example.anuviya.event.handler;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.EntityViewModel;
import com.example.anuviya.event.UiUpdateEvent;
import com.example.anuviya.event.domain.EntityListChangedEvent;
import com.example.anuviya.event.store.UIStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles {@link EntityListChangedEvent} — applies granular add/remove/update
 * deltas to the {@link UIStore} entity list.
 *
 * <p>Uses the diff carried in the event rather than replacing the entire list,
 * so JavaFX only re-renders the changed rows — not the full list.
 *
 * <p>Runs on the JavaFX Application Thread.
 */
public final class EntityListHandler implements UiUpdateHandler {

    private final UIStore store;

    public EntityListHandler(UIStore store) {
        this.store = store;
    }

    @Override
    public boolean canHandle(UiUpdateEvent event) {
        return event instanceof EntityListChangedEvent;
    }

    @Override
    public void apply(UiUpdateEvent event) {
        EntityListChangedEvent e = (EntityListChangedEvent) event;

        // 1. Remove deleted entities
        if (!e.removed().isEmpty()) {
            List<String> removedNames = new ArrayList<>();
            for (EntityInfo info : e.removed()) removedNames.add(info.getEntityName());
            store.removeEntities(removedNames);
        }

        // 2. Add new entities
        if (!e.added().isEmpty()) {
            List<EntityViewModel> newVms = new ArrayList<>();
            for (EntityInfo info : e.added()) newVms.add(new EntityViewModel(info));
            store.addEntities(newVms);
        }

        // 3. Update changed entities in-place (observable properties fire automatically)
        for (EntityInfo info : e.updated()) {
            EntityViewModel vm = store.getEntityMap().get(info.getEntityName());
            if (vm != null) {
                vm.update(info);
                store.updateEntity(vm);
            } else {
                // Entity wasn't in store — treat as add
                store.addEntities(List.of(new EntityViewModel(info)));
            }
        }
    }
}
