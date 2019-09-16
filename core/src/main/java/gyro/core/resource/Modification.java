package gyro.core.resource;

import gyro.core.GyroUI;
import gyro.core.scope.State;

import java.util.Set;

public abstract class Modification<T extends Diffable> extends Diffable {

    public void refresh(T current) {
    }

    public void beforeCreate(GyroUI ui, State state, T pending) throws Exception {
    }

    public void afterCreate(GyroUI ui, State state, T pending) throws Exception {
    }

    public void beforeUpdate(GyroUI ui, State state, T current, T pending, Set<DiffableField> changedFields) throws Exception {
    }

    public void afterUpdate(GyroUI ui, State state, T current, T pending, Set<DiffableField> changedFields) throws Exception {
    }

    public void beforeDelete(GyroUI ui, State state, T current) throws Exception {
    }

    public void afterDelete(GyroUI ui, State state, T current) throws Exception {
    }

}