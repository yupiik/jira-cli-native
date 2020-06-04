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
package io.yupiik.jira.cli.formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class Table {
    private static final String COL_SEP = "|";
    private static final String HEADER_CHAR = "=";
    private static final String LINE_CHAR = "-";
    private static final char EMPTY_CHAR = ' ';

    private final List<Line> lines = new ArrayList<>();
    private final String cr = System.lineSeparator();

    public void add(final Line line) {
        if (!lines.isEmpty() && lines.iterator().next().columns.length != line.columns.length) {
            throw new IllegalArgumentException("columns should have all the same size");
        }
        line.cr = cr;
        lines.add(line);
    }

    public void addAll(final Collection<Line> lines) {
        lines.forEach(this::add);
    }

    @Override
    public String toString() {
        return build(true);
    }

    public String build(final boolean headers) {
        final Iterator<Line> it = lines.iterator();
        if (!it.hasNext()) {
            return null;
        }

        final int[] max = max(lines);
        return Stream.concat(
                lines.stream().limit(1).map(l -> l.build(max, headers)),
                lines.stream().skip(1).map(l -> l.build(max, false)))
                .collect(joining(cr));
    }

    private static int[] max(final List<Line> lines) {
        final int[] max = new int[lines.iterator().next().columns.length];
        for (final Line line : lines) {
            for (int i = 0; i < max.length; i++) {
                final int ll = line.columns[i].length();
                if (max[i] == 0) { // init
                    max[i] = ll;
                } else if (max[i] < ll) {
                    max[i] = ll;
                }
            }
        }
        return max;
    }

    public static class Line {
        private final String[] columns;
        private String cr;

        public Line(final String... columns) {
            this.columns = columns;
        }

        public Line(final Collection<String> columns) {
            this.columns = columns.toArray(new String[0]);
        }

        private String build(final int[] max, final boolean header) {
            final StringBuilder sb = new StringBuilder(COL_SEP);
            for (int i = 0; i < max.length; i++) {
                sb.append(EMPTY_CHAR);
                final int spaces = max[i] - columns[i].length();
                sb.append(String.valueOf(EMPTY_CHAR).repeat(Math.max(0, spaces / 2)));
                sb.append(columns[i]);
                sb.append(String.valueOf(EMPTY_CHAR).repeat(Math.max(0, spaces - spaces / 2)));
                sb.append(EMPTY_CHAR).append(COL_SEP);
            }

            final String lineStr = sb.toString();

            final StringBuilder sep = new StringBuilder("");
            final String s;
            if (header) {
                s = HEADER_CHAR;
            } else {
                s = LINE_CHAR;
            }
            sep.append(s.repeat(lineStr.length()));

            return (header ? sep.toString() + cr : "") + lineStr + cr + sep.toString();
        }
    }
}
