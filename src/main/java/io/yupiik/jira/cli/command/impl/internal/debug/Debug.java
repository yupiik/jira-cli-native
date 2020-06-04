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
package io.yupiik.jira.cli.command.impl.internal.debug;

import io.yupiik.jira.cli.command.api.Command;
import io.yupiik.jira.cli.command.api.ExecutableCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import static java.util.stream.Collectors.joining;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(name = "debug", help = "Debug command, used to investigate issues in native binary.", hidden = true)
public class Debug implements ExecutableCommand {
    @Inject
    private BeanManager beanManager;

    @Override
    public void run() {
        log.info(">\nBeans:\n\n" + beanManager.getBeans(Object.class, Any.Literal.INSTANCE).stream()
                .map(Object::toString)
                .map(String::trim)
                .sorted()
                .collect(joining("\n")));
        log.info(">\nConfigProperty beans:\n\n" + beanManager.getBeans(String.class, new ConfigPropertyLiteral()).stream()
                .map(Object::toString)
                .map(String::trim)
                .sorted()
                .collect(joining("\n")));
    }

    private static class ConfigPropertyLiteral extends AnnotationLiteral<ConfigProperty> implements ConfigProperty {
        @Override
        public String name() {
            return "";
        }

        @Override
        public String defaultValue() {
            return "";
        }
    }
}
