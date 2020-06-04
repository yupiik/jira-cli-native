package io.yupiik.jira.cli;

import io.yupiik.jira.cli.command.impl.config.SetBase;
import io.yupiik.jira.cli.command.impl.help.Help;
import io.yupiik.jira.cli.command.impl.internal.debug.Debug;
import io.yupiik.jira.cli.command.impl.internal.dump.Dump;
import io.yupiik.jira.cli.command.impl.issues.ListIssues;
import io.yupiik.jira.cli.command.impl.config.Login;
import io.yupiik.jira.cli.command.service.HttpClientProducer;
import io.yupiik.jira.cli.command.service.JiraClient;
import io.yupiik.jira.cli.command.service.JsonbProducer;
import io.yupiik.jira.cli.launcher.Launcher;
import lombok.NoArgsConstructor;
import org.apache.openwebbeans.slf4j.Slf4jLoggerFactory;
import org.apache.webbeans.config.BeansDeployer;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.service.ClassLoaderProxyService;
import org.apache.webbeans.spi.DefiningClassService;
import org.slf4j.impl.SimpleLogger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Cli {
    public static void main(final String... args) {
        System.setProperty(WebBeansLoggerFacade.OPENWEBBEANS_LOGGING_FACTORY_PROP, Slf4jLoggerFactory.class.getName());
        System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "org.apache.webbeans", "warn");
        System.setProperty(BeansDeployer.class.getName() + ".level", "warn");
        try (final SeContainer container = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addBeanClasses(classes().toArray(Class<?>[]::new))
                .addProperty(DefiningClassService.class.getName(), findProxyService()) // no unsafe usage
                .addProperty("org.apache.webbeans.proxy.useStaticNames", "true") // a bit unsafe but otherwise no way to get pregenerated proxies
                .initialize()) {
            container.select(Launcher.class).get().launch(args);
        }
    }

    private static String findProxyService() {
        if (Boolean.getBoolean("cli.forceLoadFirst") || Cli.class.getMethods().length == 0) { // in the image we didn't configure the reflection for Cli so will be 0
            return ClassLoaderProxyService.LoadFirst.class.getName();
        }
        return ClassLoaderProxyService.Spy.class.getName();
    }

    // CDI classes to register
    private static Stream<Class<?>> classes() {
        return Stream.of(
                // command init
                Launcher.class,
                // stack
                HttpClientProducer.class, JsonbProducer.class, JiraClient.class,
                // commands
                Help.class, Login.class, SetBase.class, ListIssues.class,
                // internal commands
                Debug.class, Dump.class);
    }
}
