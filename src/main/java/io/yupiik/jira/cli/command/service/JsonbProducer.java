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
package io.yupiik.jira.cli.command.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@Slf4j
@ApplicationScoped
@RegisterClass(all = true)
public class JsonbProducer {
    @Produces
    @ApplicationScoped
    public Jsonb create() {
        return JsonbBuilder.create();
    }

    public void release(@Disposes final Jsonb jsonb) {
        try {
            jsonb.close();
        } catch (final Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
