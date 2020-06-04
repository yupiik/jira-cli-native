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

import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.json.bind.Jsonb;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
@RegisterClass(all = true)
public class JiraClient {
    @Inject
    private HttpClient client;

    @Inject
    private Jsonb jsonb;

    @Inject
    @ConfigProperty(name = "jira.base")
    private Provider<String> base;

    @Inject
    @ConfigProperty(name = "jira.token")
    private Provider<String> token;

    @Inject
    @ConfigProperty(name = "jira.api-path", defaultValue = "/rest/api/latest")
    private String apiPath;

    public <T> T getJson(final Class<T> expected, final String path) {
        try {
            var response = client.send(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(base.get() + apiPath + path))
                    .header("authorization", token.get())
                    .header("accept", "application/json")
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            final var body = response.body();
            if (body.isEmpty()) {
                throw new IllegalStateException("No payload, status=" + response.statusCode());
            }
            return jsonb.fromJson(body, expected);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public String queryEncode(final String value) {
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder bufferToEncode = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char currentChar = value.charAt(i);
            if ("?/,".indexOf(currentChar) != -1) {
                if (bufferToEncode.length() > 0) {
                    buffer.append(urlEncode(bufferToEncode.toString()));
                    bufferToEncode.setLength(0);
                }
                buffer.append(currentChar);
            } else {
                bufferToEncode.append(currentChar);
            }
        }
        if (bufferToEncode.length() > 0) {
            buffer.append(urlEncode(bufferToEncode.toString()));
        }
        return buffer.toString();
    }

    private static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
