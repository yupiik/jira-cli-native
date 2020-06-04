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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArgsConfigSource implements ConfigSource {
    private final Map<String, String> args = new HashMap<>();

    @Override
    public Map<String, String> getProperties() {
        return args;
    }

    @Override
    public String getValue(final String key) {
        return args.get(key);
    }

    @Override
    public String getName() {
        return "args";
    }

    public void setArgs(final String[] args) {
        final Collection<String> defaults = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                this.args.put(args[i].substring(2).replace('-', '.'), args[++i]);
            } else {
                defaults.add(args[i]);
            }
        }
        if (!defaults.isEmpty()) {
            this.args.put("io.yupiik.jira-cli.arguments", String.join(",", defaults));
        }
    }

    @Override
    public int getOrdinal() {
        return DEFAULT_ORDINAL + 100;
    }
}
