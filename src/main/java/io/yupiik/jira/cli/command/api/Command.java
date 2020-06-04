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
package io.yupiik.jira.cli.command.api;

import lombok.RequiredArgsConstructor;
import org.apache.geronimo.arthur.api.RegisterClass;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@RegisterClass(all = true)
public @interface Command {
    String name();

    @Nonbinding
    String help();

    @Nonbinding
    boolean hidden() default false;

    @Nonbinding
    Class<?>[] importedConfig() default {};

    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<Command> implements Command {
        private final String name;

        @Override
        public String name() {
            return name;
        }

        @Override
        public String help() {
            return "";
        }

        @Override
        public boolean hidden() {
            return false;
        }

        @Override
        public Class<?>[] importedConfig() {
            return new Class[0];
        }
    }
}
