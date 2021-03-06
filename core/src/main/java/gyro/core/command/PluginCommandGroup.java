/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.core.command;

import java.util.Arrays;
import java.util.List;

public class PluginCommandGroup implements GyroCommandGroup {

    @Override
    public String getName() {
        return "plugin";
    }

    @Override
    public String getDescription() {
        return "Add, remove, or list plugins.";
    }

    @Override
    public List<Class<?>> getCommands() {
        return Arrays.asList(PluginAddCommand.class, PluginRemoveCommand.class, PluginListCommand.class);
    }

    @Override
    public Class<?> getDefaultCommand() {
        return PluginHelp.class;
    }

}
