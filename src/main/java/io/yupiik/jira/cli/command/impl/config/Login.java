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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(
        name = "login",
        help = "Creates a ~/.yupiikjiraclirc file with authentication information for future commands.")
public class Login extends ConfigCommandBase {
    @Inject
    @ConfigProperty(name = "jira.username")
    private Provider<String> user;

    @Inject
    @ConfigProperty(name = "jira.cloud.token")
    private Provider<String> token;

    @Override
    protected String getKey() {
        return "jira.token";
    }

    @Override
    protected String getValue() {
        return "Basic " + Base64.getEncoder().encodeToString((user.get() + ':' + token.get()).getBytes(StandardCharsets.UTF_8));
    }
}
