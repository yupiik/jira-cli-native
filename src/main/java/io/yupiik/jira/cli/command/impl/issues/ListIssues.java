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
package io.yupiik.jira.cli.command.impl.issues;

import io.yupiik.jira.cli.command.api.Command;
import io.yupiik.jira.cli.command.api.ExecutableCommand;
import io.yupiik.jira.cli.command.service.JiraClient;
import io.yupiik.jira.cli.formatting.Table;
import io.yupiik.jira.cli.model.Fields;
import io.yupiik.jira.cli.model.Issue;
import io.yupiik.jira.cli.model.Issues;
import io.yupiik.jira.cli.model.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(
        name = "list",
        help = "List issues with the provided filer.",
        importedConfig = JiraClient.class)
public class ListIssues implements ExecutableCommand {
    @Inject
    // todo: better config, project=x (to have it in rc file), status= or event, labels=, config.key= (to have aliases) for ex
    @ConfigProperty(name = "jpql", defaultValue = "")
    private String jpql;

    @Inject
    @ConfigProperty(name = "issues.status", defaultValue = "-")
    private List<String> issuesStates;

    @Inject
    @ConfigProperty(name = "issues.version", defaultValue = "-")
    private List<String> issuesVersion;

    @Inject
    @ConfigProperty(name = "jira.base", defaultValue = "")
    private String base;

    @Inject
    @ConfigProperty(name = "jira.maxResults", defaultValue = "20")
    private long maxResults;

    @Inject
    @ConfigProperty(name = "jira.startAt", defaultValue = "0")
    private long startAt;

    @Inject
    @ConfigProperty(name = "jira.columns", defaultValue = "id,key,fixVersion,summary,url")
    private List<String> columns;

    @Inject
    private JiraClient client;

    @Override
    public void run() {
        final var jpql = createJpql();
        final var issues = client.getJson(Issues.class, "/search?startAt=" + startAt + "&maxResults=" + maxResults + "&jql=" + client.queryEncode(jpql));
        log.info("jpql='{}'\n{}", jpql, buildTable(issues));
        log.info("Total: {}, StartAt: {}{}", issues.getTotal(), issues.getStartAt(),
                issues.getStartAt() + issues.getMaxResults() < issues.getTotal() ?
                        ", Next page: 'list --jpql '" + this.jpql + "' --jira-startAt " + (startAt + maxResults) + "'" : "");
    }

    private String createJpql() {
        return Stream.of(
                jpql,
                hasValue(issuesStates) ? issuesVersion.stream().map(v -> "status='" + v + "'").collect(joining("OR")) : "",
                hasValue(issuesVersion) ? issuesVersion.stream().map(v -> "fixVersion='" + v + "'").collect(joining("OR")) : "")
                .filter(it -> !it.isEmpty())
                .collect(joining(") AND (", "(", ")"));
    }

    private Table buildTable(final Issues issues) {
        final var table = new Table();
        if (issues.getIssues() != null && !issues.getIssues().isEmpty()) {
            table.add(new Table.Line(columns));
            final Collection<Function<Issue, String>> extractors = columns.stream()
                    .map(this::toExtractor)
                    .collect(toList());
            table.addAll(issues.getIssues().stream()
                    .map(i -> new Table.Line(extractors.stream().map(e -> e.apply(i)).collect(toList())))
                    .collect(toList()));
        } else {
            table.add(new Table.Line("Message"));
            table.add(new Table.Line("No issue found."));
        }
        return table;
    }

    private Function<Issue, String> toExtractor(final String key) {
        switch (key) {
            case "raw":
                return Issue::toString;
            case "id":
                return Issue::getId;
            case "key":
                return Issue::getKey;
            case "self":
                return Issue::getSelf;
            case "url":
                return i -> base + "/browse/" + i.getKey();
            case "summary":
                return i -> ofNullable(i.getFields()).map(Fields::getSummary).orElse("-");
            case "fixVersion":
                return i -> ofNullable(i.getFields())
                        .map(Fields::getFixVersions)
                        .map(it -> it.stream().map(Version::getName).collect(joining(",")))
                        .orElse("-");
            default:
                throw new IllegalArgumentException("Unknown key: '" + key + "'");
        }
    }

    private boolean hasValue(final Collection<String> list) {
        return !this.issuesVersion.isEmpty() && !(list.size() == 1 && "-".equals(list.iterator().next()));
    }
}
