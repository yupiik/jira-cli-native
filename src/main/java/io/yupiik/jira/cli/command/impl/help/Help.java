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
package io.yupiik.jira.cli.command.impl.help;

import io.yupiik.jira.cli.command.api.Command;
import io.yupiik.jira.cli.command.api.ExecutableCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(name = "help", help = "Show help.")
public class Help implements ExecutableCommand {
    @Inject
    private BeanManager beanManager;

    @Override
    public void run() {
        log.info("\n" + beanManager.getBeans(ExecutableCommand.class, Any.Literal.INSTANCE).stream()
                .filter(this::isCommand)
                .map(b -> {
                    final var cmd = findCommand(b);
                    return "  - " + cmd.name() + ":\n\n    " + cmd.help().replace("\n", "\n    ") + "\n\n" +
                            extractConfig(b) +
                            Stream.of(cmd.importedConfig())
                                    .map(this::findBean)
                                    .map(this::extractConfig)
                                    .collect(joining("\n"));
                })
                .map(String::trim)
                .sorted()
                .collect(joining("\n\n")));
    }

    private Bean<?> findBean(final Class<?> type) {
        return beanManager.resolve(beanManager.getBeans(type));
    }

    private Command findCommand(final Bean<?> b) {
        return Command.class.cast(b.getQualifiers().stream()
                .filter(it -> it.annotationType() == Command.class)
                .findFirst()
                .orElseThrow(IllegalStateException::new));
    }

    private String extractConfig(final Bean<?> b) {
        return b.getInjectionPoints().stream()
                .filter(i -> i.getAnnotated().isAnnotationPresent(ConfigProperty.class))
                .map(i -> {
                    final ConfigProperty property = i.getAnnotated().getAnnotation(ConfigProperty.class);
                    return "    > --" + (property.name().isEmpty() ? b.getBeanClass().getName() + '.' + i.getMember().getName() : property.name()).replace('.', '-') +
                            (property.defaultValue().equals(ConfigProperty.UNCONFIGURED_VALUE) ? "" : " (default: " + property.defaultValue() + ")");
                })
                .collect(joining("\n", "", "\n"));
    }

    private boolean isCommand(final Bean<?> b) {
        return b.getTypes().contains(ExecutableCommand.class) && b.getQualifiers().stream().anyMatch(it -> it.annotationType() == Command.class)
                && !findCommand(b).hidden();
    }
}
