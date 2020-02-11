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

import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import gyro.core.GyroCore;
import gyro.core.auditor.GyroAuditor;
import gyro.core.metadata.MetadataDirectiveProcessor;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.slf4j.LoggerFactory;

/**
 * Basic {@link GyroCommand} implementation that adds the global {@code -debug} option for more detailed logging.
 *
 * <p>Subclasses must override:</p>
 *
 * <ul>
 * <li>{@link #doExecute}</li>
 * </ul>
 */
public abstract class AbstractCommand implements GyroCommand {

    private static final String SUCCESS_FLAG = "gyro.abstractCommand.isSuccess";

    private static AbstractCommand command;

    @Option(type = OptionType.GLOBAL, name = "--debug", description = "Debug mode")
    public boolean debug;

    @Option(name = "--verbose")
    private boolean verbose;

    private List<String> unparsedArguments;

    protected abstract void doExecute() throws Exception;

    public static AbstractCommand getCommand() {
        return command;
    }

    public static void setCommand(AbstractCommand command) {
        AbstractCommand.command = command;
    }

    public List<String> getUnparsedArguments() {
        return unparsedArguments;
    }

    public void setUnparsedArguments(List<String> unparsedArguments) {
        this.unparsedArguments = unparsedArguments;
    }

    @Override
    public void execute() throws Exception {
        GyroCore.ui().setVerbose(verbose);

        if (debug || "debug".equalsIgnoreCase(System.getenv("GYRO_LOG"))) {
            System.getProperties().setProperty("org.openstack4j.core.transport.internal.HttpLoggingFilter", "true");

            ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
        } else {
            ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.OFF);

            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        }

        AbstractCommand.setCommand(this);

        if (enableAuditor()) {
            startAuditors();
        }

        doExecute();

        MetadataDirectiveProcessor.putMetadata(SUCCESS_FLAG, true);
    }

    public boolean isDebug() {
        return debug;
    }

    // TODO: abstract?
    public boolean enableAuditor() {
        return false;
    }

    private void startAuditors() {
        GyroAuditor.AUDITOR_BY_NAME.values().stream()
            .parallel()
            .filter(auditor -> !auditor.isStarted())
            .forEach(auditor -> {
                try {
                    auditor.start(MetadataDirectiveProcessor.getMetadataMap());
                } catch (Exception e) {
                    // TODO: error message
                    System.err.print(e.getMessage());
                }
            });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> finishAuditors()));
    }

    private void finishAuditors() {
        GyroAuditor.AUDITOR_BY_NAME.values().stream()
            .parallel()
            .filter(GyroAuditor::isStarted)
            .filter(auditor -> !auditor.isFinished())
            .forEach(auditor -> {
                try {
                    auditor.finish(
                        MetadataDirectiveProcessor.getMetadataMap(),
                        Optional.ofNullable(MetadataDirectiveProcessor.removeMetadata(SUCCESS_FLAG))
                            .filter(boolean.class::isInstance)
                            .map(boolean.class::cast)
                            .orElse(false));
                } catch (Exception e) {
                    // TODO: error message
                    System.err.print(e.getMessage());
                }
            });
    }
}
