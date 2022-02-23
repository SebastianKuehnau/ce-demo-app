package com.example.application;

import com.vaadin.collaborationengine.CollaborationEngineConfiguration;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Push
@Theme(value = "cedemoapp")
@PWA(name = "CE Demo App", shortName = "CE Demo App", offlineResources = {"images/logo.png"})
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static AtomicInteger userCounter = new AtomicInteger(0) ;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CollaborationEngineConfiguration ceConfigBean() {
        CollaborationEngineConfiguration configuration = new CollaborationEngineConfiguration(
                licenseEvent -> {
                    // See <<ce.production.license-events>>
                    switch (licenseEvent.getType()) {
                        case GRACE_PERIOD_STARTED:
                        case LICENSE_EXPIRES_SOON:
                            logger.warn(licenseEvent.getMessage());
                            break;
                        case GRACE_PERIOD_ENDED:
                        case LICENSE_EXPIRED:
                            logger.fatal(licenseEvent.getMessage());
                            break;
                        default:
                            logger.fatal("Unknown error: " + licenseEvent.getMessage());
                    }
                });
        return configuration;
    }

}
