package io.yupiik.jira.cli;

import io.yupiik.jira.cli.launcher.Launcher;
import lombok.NoArgsConstructor;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Cli {
    public static void main(final String... args) {
        setLogLevels();
        try (final SeContainer container = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .initialize()) {
            container.select(Launcher.class).get().launch(args);
        }
    }

    // only log errors otherwise we just want the stdout/stderr of the commands for this CLI
    private static void setLogLevels() {
        System.setProperty("org.apache.level", "SEVERE");
        System.setProperty("io.yupiik.level", "SEVERE");
        System.setProperty("io.yupiik.jira.cli.command.impl.level", "INFO");
    }
}
