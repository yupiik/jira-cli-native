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
package io.yupiik.jira.cli.command.impl.config;

import io.yupiik.jira.cli.command.api.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Provider;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(
        name = "set-base",
        help = "Add jira base to ~/.yupiikjiraclirc file.")
public class SetBase extends ConfigCommandBase {
    @Inject
    @ConfigProperty(name = "jira.base")
    private Provider<String> base;

    @Override
    protected String getKey() {
        return "jira.base";
    }

    @Override
    protected String getValue() {
        return base.get();
    }
}
