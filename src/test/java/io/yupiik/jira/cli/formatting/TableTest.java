package io.yupiik.jira.cli.formatting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest {
    @Test
    void createTable() {
        final var table = new Table();
        table.add(new Table.Line("Title"));
        table.add(new Table.Line("Value"));
        assertEquals("=========\n" +
                "| Title |\n" +
                "=========\n" +
                "| Value |\n" +
                "---------", table.toString().replace(System.lineSeparator(), "\n"));
        table.add(new Table.Line("Value2"));
        assertEquals("==========\n" +
                "| Title  |\n" +
                "==========\n" +
                "| Value  |\n" +
                "----------\n" +
                "| Value2 |\n" +
                "----------", table.toString().replace(System.lineSeparator(), "\n"));
    }
}
