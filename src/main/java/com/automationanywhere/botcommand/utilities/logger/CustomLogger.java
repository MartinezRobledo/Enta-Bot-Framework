package com.automationanywhere.botcommand.utilities.logger;

import org.apache.commons.io.FilenameUtils;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;

import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomLogger implements CloseableSessionObject {

    // Map público para reutilización por archivo
    public static final Map<String, CustomLogger> LOGGER_BY_FILE = new ConcurrentHashMap<>();

    private final Logger logger;
    private final String loggerId = UUID.randomUUID().toString();
    private final LoggerContext loggerContext;
    private final Map<Level, String> screenshotFolderPaths = new HashMap<>();
    private final Map<Level, String> variablesFolderPaths = new HashMap<>();

    // Constructor para un solo archivo
    public CustomLogger(String loggerName, String logFilePath, long sizeLimitMB) throws IOException {
        String baseDir = FilenameUtils.getFullPath(logFilePath);
        String screenshotFolder = baseDir + "screenshots";
        String variablesFolder = baseDir + "variables";
        this.screenshotFolderPaths.put(Level.INFO, screenshotFolder);
        this.screenshotFolderPaths.put(Level.WARN, screenshotFolder);
        this.screenshotFolderPaths.put(Level.ERROR, screenshotFolder);
        this.variablesFolderPaths.put(Level.INFO, variablesFolder);
        this.variablesFolderPaths.put(Level.WARN, variablesFolder);
        this.variablesFolderPaths.put(Level.ERROR, variablesFolder);
        this.createDirectories();
        LoggerContext context = this.createNewLoggerContext();
        DefaultConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<BuiltConfiguration>();
        this.setupLoggerConfiguration(builder);
        AppenderComponentBuilder appenderBuilder = this.getCustomAppenderBuilder(builder, "COMBINED_" + this.loggerId, logFilePath, sizeLimitMB);
        builder.add(appenderBuilder);
        builder.add((LoggerComponentBuilder)builder.newLogger(loggerName, Level.INFO).add(builder.newAppenderRef("COMBINED_" + this.loggerId)));
        builder.add(builder.newRootLogger(Level.INFO));
        BuiltConfiguration config = (BuiltConfiguration)builder.build();
        context.start(config);
        this.loggerContext = context;
        this.logger = context.getLogger(loggerName);
        LOGGER_BY_FILE.put(logFilePath, this);
    }

    // Constructor para múltiples archivos por nivel
    public CustomLogger(String loggerName, Map<Level, String> levelFilePathMap, long sizeLimitMB) throws IOException {
        for (Map.Entry<Level, String> entry : levelFilePathMap.entrySet()) {
            Level level = entry.getKey();
            String string = FilenameUtils.getFullPath(entry.getValue());
            this.screenshotFolderPaths.put(level, string + "screenshots");
            this.variablesFolderPaths.put(level, string + "variables");
        }
        this.createDirectories();
        LoggerContext context = this.createNewLoggerContext();
        DefaultConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<BuiltConfiguration>();
        this.setupLoggerConfiguration(builder);
        for (Map.Entry entry : levelFilePathMap.entrySet()) {
            Level level = (Level)entry.getKey();
            String filePath = (String)entry.getValue();
            String appenderName = level.name() + "_" + this.loggerId;
            AppenderComponentBuilder appenderBuilder = this.getCustomAppenderBuilder(builder, appenderName, filePath, sizeLimitMB);
            appenderBuilder.add((FilterComponentBuilder)builder.newFilter("LevelMatchFilter", "ACCEPT", "DENY").addAttribute("level", level));
            builder.add(appenderBuilder);
            LOGGER_BY_FILE.put(filePath, this);
        }
        builder.add((LoggerComponentBuilder)((LoggerComponentBuilder)((LoggerComponentBuilder)builder.newLogger(loggerName, Level.INFO).add(builder.newAppenderRef(Level.INFO.name() + "_" + this.loggerId))).add(builder.newAppenderRef(Level.WARN.name() + "_" + this.loggerId))).add(builder.newAppenderRef(Level.ERROR.name() + "_" + this.loggerId)));
        builder.add(builder.newRootLogger(Level.INFO));
        BuiltConfiguration config = (BuiltConfiguration)builder.build();
        context.start(config);
        this.loggerContext = context;
        this.logger = context.getLogger(loggerName);
    }

    private void createDirectories() throws IOException {
        for (String path : this.screenshotFolderPaths.values()) {
            Files.createDirectories(Paths.get(path, new String[0]), new FileAttribute[0]);
        }
        for (String path : this.variablesFolderPaths.values()) {
            Files.createDirectories(Paths.get(path, new String[0]), new FileAttribute[0]);
        }
    }

    private LoggerContext createNewLoggerContext() {
        return new LoggerContext("Context-" + this.loggerId);
    }

    private void setupLoggerConfiguration(ConfigurationBuilder<BuiltConfiguration> builder) {
        builder.setConfigurationName("CustomLogger-" + this.loggerId);
        builder.setPackages("com.automationanywhere.botcommand.utilities.logger");
        builder.setMonitorInterval("30");
        builder.setStatusLevel(Level.ERROR);
    }

    private AppenderComponentBuilder getCustomAppenderBuilder(ConfigurationBuilder<BuiltConfiguration> builder,
                                                              String appenderName, String filePath,
                                                              long sizeLimitMB) {
        LayoutComponentBuilder layoutBuilder = (LayoutComponentBuilder)builder.newLayout("CustomHTMLLayout").addAttribute("charset", "UTF-8");
        String filePattern = FilenameUtils.getFullPath(filePath) + FilenameUtils.getBaseName(filePath) + "_%i." + FilenameUtils.getExtension(filePath);
        return (AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)builder.newAppender(appenderName, "RollingFile").addAttribute("fileName", filePath)).addAttribute("filePattern", filePattern)).addAttribute("immediateFlush", true)).addAttribute("append", true)).addComponent(layoutBuilder)).addComponent((ComponentBuilder<?>)builder.newComponent("Policies").addComponent((ComponentBuilder<?>)builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", sizeLimitMB + "MB")))).addComponent((ComponentBuilder<?>)builder.newComponent("DefaultRolloverStrategy").addAttribute("fileIndex", "nomax"));
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public void close() {
        if (!this.isClosed()) {
            this.loggerContext.stop();
        }
    }

    @Override
    public boolean isClosed() {
        return this.loggerContext.isStopped();
    }

    public String getScreenshotFolderPath(Level level) {
        return this.screenshotFolderPaths.getOrDefault(level, this.screenshotFolderPaths.get(Level.INFO));
    }

    public String getVariablesFolderPath(Level level) {
        return this.variablesFolderPaths.getOrDefault(level, this.variablesFolderPaths.get(Level.INFO));
    }
}
