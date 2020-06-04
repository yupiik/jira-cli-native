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
package io.yupiik.jira.cli.model;

import lombok.Data;
import org.apache.geronimo.arthur.api.RegisterClass;

import java.util.Collection;

@Data
@RegisterClass(all = true)
public class Issues {
    private String expand;
    private long startAt;
    private long total;
    private long maxResults;
    private Collection<Issue> issues;
}
