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
package io.yupiik.jira.cli.config;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

// todo: make it reloadable if we create a cli able to run multiple commands
public class HomeConfigSource implements ConfigSource {
    public static final String RC_FILE = ".yupiikjiraclirc";

    private final Map<String, String> values;

    public HomeConfigSource() {
        final Properties properties = new Properties();
        try (final Reader path = Files.newBufferedReader(Path.of(System.getProperty("user.home"), RC_FILE), StandardCharsets.UTF_8)) {
            properties.load(path);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        values = properties.stringPropertyNames().stream()
                .collect(toMap(identity(), properties::getProperty));
    }

    @Override
    public Map<String, String> getProperties() {
        return values;
    }

    @Override
    public String getValue(final String propertyName) {
        return values.get(propertyName);
    }

    @Override
    public String getName() {
        return "yupiikjiraclirc";
    }
}
