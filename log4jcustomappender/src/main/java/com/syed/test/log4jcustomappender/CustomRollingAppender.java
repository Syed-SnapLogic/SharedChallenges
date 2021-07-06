/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syed.test.log4jcustomappender;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**
 *
 * @author gaian
 */
public class CustomRollingAppender {

    public static void main(String str[]) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (!tmpDir.endsWith(File.separator)) {
            tmpDir += File.separator;
        }

        String logFileName = tmpDir + getFileName();
        System.out.println("logFileName: "+ logFileName);

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory
                .newConfigurationBuilder();

        LayoutComponentBuilder layoutBuilder = builder
                .newLayout("PatternLayout")
                .addAttribute("pattern", "%d [%t] %-5level: %msg%n");

        ComponentBuilder triggeringPolicy = builder
                .newComponent("Policies")
                .addComponent(builder.newComponent("CronTriggeringPolicy")
                        .addAttribute("schedule", "0 0 0 * * ?"))
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                        .addAttribute("size", "1M"));

        AppenderComponentBuilder appenderBuilder = builder
                .newAppender("rolling", "RollingFile")
                .addAttribute("fileName", logFileName)
                .addAttribute("filePattern", tmpDir + "/rolling-%d{MM-dd-yy}.log.gz")
                .add(layoutBuilder)
                .addComponent(triggeringPolicy);

        builder.add(appenderBuilder);
        builder
                .add(builder.newLogger("TestLogger", Level.DEBUG)
                        .add(builder.newAppenderRef("rolling"))
                        .addAttribute("additivity", false));

        LoggerContext ctx = Configurator.initialize(builder.build());
        Logger logger = ctx.getLogger("TestLogger");

        System.out.println("logging starts");
        for (int i = 0; i < 2000000; i++) {
            logger.debug("test logger message to see rolling file appender");
        }
        System.out.println("loggin ended");
    }

    private static String getFileName() {
        return "testrollinglog.log";
    }
}
