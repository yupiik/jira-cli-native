/*
 * Copyright 2020Yupiik SAS - https://www.yupiik.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yupiik.jira.cli.launcher;

import io.yupiik.jira.cli.command.api.Command;
import io.yupiik.jira.cli.command.api.ExecutableCommand;
import io.yupiik.jira.cli.config.ArgsConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Dependent
@RegisterClass(all = true)
public class Launcher {
    @Any
    @Inject
    private Instance<ExecutableCommand> commands;

    public void launch(final String... args) {
        if (args.length == 0) {
            launch("help");
            return;
        }
        bindArgs(Stream.of(args).skip(1).toArray(String[]::new));
        try {
            commands.select(new Command.Literal(args[0])).get().run();
        } catch (final UnsatisfiedResolutionException ure) {
            launch("help");
            throw ure;
        }
    }

    private void bindArgs(final String[] args) {
        StreamSupport.stream(ConfigProvider.getConfig().getConfigSources().spliterator(), false)
                .filter(ArgsConfigSource.class::isInstance)
                .findFirst()
                .map(ArgsConfigSource.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Missing args config source"))
                .setArgs(args);
    }
}
