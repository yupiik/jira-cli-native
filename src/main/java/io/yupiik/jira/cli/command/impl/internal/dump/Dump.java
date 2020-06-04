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
package io.yupiik.jira.cli.command.impl.internal.dump;

import io.yupiik.jira.cli.command.api.Command;
import io.yupiik.jira.cli.command.api.ExecutableCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.geronimo.arthur.api.RegisterClass;
import org.apache.webbeans.component.ExtensionBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.service.ClassLoaderProxyService;
import org.apache.webbeans.spi.DefiningClassService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;

@Slf4j
@Dependent
@RegisterClass(all = true)
@Command(name = "dump", help = "Build command to dump the CDI proxies.", hidden = true)
public class Dump implements ExecutableCommand {
    @Inject
    private BeanManager beanManager;

    @Inject
    @ConfigProperty(name = "dump.output")
    private Provider<String> output;

    @Inject
    @ConfigProperty(name = "dump.logArthurConfig", defaultValue = "false")
    private boolean logArthurConfig;

    @Override
    public void run() {
        final Path out = Paths.get(output.get());
        if (!Files.exists(out)) {
            throw new IllegalStateException("You should run compile before this task, missing: " + out);
        }
        beanManager.getBeans(Object.class).stream()
                .filter(b -> beanManager.isNormalScope(b.getScope()) && !isIgnoredBean(b)) // todo: do it also for interception
                .forEach(it -> {
                    try { // triggers the proxy creation
                        beanManager.getReference(it, Object.class, beanManager.createCreationalContext(null));
                    } catch (final Exception e) {
                        log.warn(e.getMessage());
                    }
                });
        final var config = ClassLoaderProxyService.Spy.class.cast(WebBeansContext.currentInstance().getService(DefiningClassService.class))
                .getProxies().entrySet().stream()
                .map(e -> {
                    final Path target = out.resolve(e.getKey().replace('.', '/') + ".class");
                    try {
                        Files.createDirectories(target.getParent());
                        Files.write(target, e.getValue());
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    log.info("Created proxy '{}'", e.getKey());
                    return "" +
                            "                <reflection>\n" +
                            "                  <name>" + e.getKey().replace("$$", "$$$$") + "</name>\n" +
                            "                  <allDeclaredConstructors>true</allDeclaredConstructors>\n" +
                            "                  <allDeclaredMethods>true</allDeclaredMethods>\n" +
                            "                  <allDeclaredFields>true</allDeclaredFields>\n" +
                            "                </reflection>";
                })
                .collect(joining("\n"));
        if (logArthurConfig) {
            log.info("Ensure to add to Geronimo Arthur Maven plugin the following reflection config:\n\n{}", config);
        }
    }

    private boolean isIgnoredBean(final Bean<?> b) { // we don't want a proxy for java.util.Set
        return (PassivationCapable.class.isInstance(b) && "apache.openwebbeans.OwbInternalConversationStorageBean".equals(PassivationCapable.class.cast(b).getId())) ||
                ExtensionBean.class.isInstance(b) /*not needed*/;
    }
}
