package gyro.core.plugin;

import gyro.core.resource.RootScope;

public abstract class Plugin {

    public abstract void onEachClass(RootScope root, Class<?> aClass) throws Exception;

}