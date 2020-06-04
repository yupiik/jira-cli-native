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

import io.yupiik.jira.cli.command.api.ExecutableCommand;
import io.yupiik.jira.cli.config.HomeConfigSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Abstract command to set generic and very common configuration in a rc file.
 */
@Slf4j
public abstract class ConfigCommandBase implements ExecutableCommand {

    @Override
    public void run() {
        final var properties = new Properties();
        final var file = Path.of(System.getProperty("user.home"), HomeConfigSource.RC_FILE);
        if (Files.exists(file)) {
            try {
                properties.load(new StringReader(Files.readString(file, StandardCharsets.UTF_8)));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        properties.put(getKey(), getValue());
        try (final Writer path = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(path, getClass().getName());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        log.info("Updated '{}'", file);
    }

    protected abstract String getKey();

    protected abstract String getValue();
}
