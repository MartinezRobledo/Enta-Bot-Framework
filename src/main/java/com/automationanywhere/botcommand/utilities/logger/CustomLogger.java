package com.automationanywhere.botcommand.utilities.logger;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomLogger implements CloseableSessionObject {

    // Map público para reutilización por archivo
    public static final Map<String, CustomLogger> LOGGER_BY_FILE = new ConcurrentHashMap<>();

    private final Logger logger;
    private final String loggerId;
    private final LoggerContext loggerContext;
    private final Map<Level, String> screenshotFolderPaths;
    private final Map<Level, String> variablesFolderPaths;

    // Constructor para un solo archivo
    public CustomLogger(String loggerName, String logFilePath, long sizeLimitMB) throws IOException {
        this.loggerId = UUID.randomUUID().toString();
        this.screenshotFolderPaths = new HashMap<>();
        this.variablesFolderPaths = new HashMap<>();

        String baseDir = FilenameUtils.getFullPath(logFilePath);
        String screenshotFolder = baseDir + "screenshots";
        String variablesFolder = baseDir + "variables";

        this.screenshotFolderPaths.put(Level.INFO, screenshotFolder);
        this.screenshotFolderPaths.put(Level.WARN, screenshotFolder);
        this.screenshotFolderPaths.put(Level.ERROR, screenshotFolder);

        this.variablesFolderPaths.put(Level.INFO, variablesFolder);
        this.variablesFolderPaths.put(Level.WARN, variablesFolder);
        this.variablesFolderPaths.put(Level.ERROR, variablesFolder);

        createDirectories();

        LoggerContext context = createNewLoggerContext();
        ConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<>();
        setupLoggerConfiguration(builder);

        AppenderComponentBuilder appenderBuilder = getCustomAppenderBuilder(builder, "COMBINED_" + loggerId,
                logFilePath, sizeLimitMB);
        builder.add(appenderBuilder);
        builder.add(builder.newLogger(loggerName, Level.INFO)
                .add(builder.newAppenderRef("COMBINED_" + loggerId)));
        builder.add(builder.newRootLogger(Level.INFO));

        BuiltConfiguration config = builder.build();
        context.start(config);

        this.loggerContext = context;
        this.logger = context.getLogger(loggerName);

        LOGGER_BY_FILE.put(logFilePath, this);
    }

    // Constructor para múltiples archivos por nivel
    public CustomLogger(String loggerName, Map<Level, String> levelFilePathMap, long sizeLimitMB) throws IOException {
        this.loggerId = UUID.randomUUID().toString();
        this.screenshotFolderPaths = new HashMap<>();
        this.variablesFolderPaths = new HashMap<>();

        for (Map.Entry<Level, String> entry : levelFilePathMap.entrySet()) {
            Level level = entry.getKey();
            String baseDir = FilenameUtils.getFullPath(entry.getValue());
            this.screenshotFolderPaths.put(level, baseDir + "screenshots");
            this.variablesFolderPaths.put(level, baseDir + "variables");
        }

        createDirectories();

        LoggerContext context = createNewLoggerContext();
        ConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<>();
        setupLoggerConfiguration(builder);

        for (Map.Entry<Level, String> entry : levelFilePathMap.entrySet()) {
            Level level = entry.getKey();
            String filePath = entry.getValue();
            String appenderName = level.name() + "_" + loggerId;

            AppenderComponentBuilder appenderBuilder = getCustomAppenderBuilder(builder, appenderName, filePath,
                    sizeLimitMB);
            appenderBuilder.add(builder.newFilter("LevelMatchFilter", "ACCEPT", "DENY")
                    .addAttribute("level", level));
            builder.add(appenderBuilder);

            LOGGER_BY_FILE.put(filePath, this);
        }

        builder.add(builder.newLogger(loggerName, Level.INFO)
                .add(builder.newAppenderRef(Level.INFO.name() + "_" + loggerId))
                .add(builder.newAppenderRef(Level.WARN.name() + "_" + loggerId))
                .add(builder.newAppenderRef(Level.ERROR.name() + "_" + loggerId)));
        builder.add(builder.newRootLogger(Level.INFO));

        BuiltConfiguration config = builder.build();
        context.start(config);

        this.loggerContext = context;
        this.logger = context.getLogger(loggerName);
    }

    private void createDirectories() throws IOException {
        for (String path : screenshotFolderPaths.values()) {
            Files.createDirectories(Paths.get(path));
        }
        for (String path : variablesFolderPaths.values()) {
            Files.createDirectories(Paths.get(path));
        }
    }

    private LoggerContext createNewLoggerContext() {
        return new LoggerContext("Context-" + loggerId);
    }

    private void setupLoggerConfiguration(ConfigurationBuilder<BuiltConfiguration> builder) {
        builder.setConfigurationName("CustomLogger-" + loggerId);
        builder.setPackages("com.automationanywhere.botcommand.utilities.logger");
        builder.setMonitorInterval("30");
        builder.setStatusLevel(Level.ERROR);
    }

    private AppenderComponentBuilder getCustomAppenderBuilder(ConfigurationBuilder<BuiltConfiguration> builder,
                                                              String appenderName, String filePath,
                                                              long sizeLimitMB) {
        LayoutComponentBuilder layoutBuilder = builder.newLayout("CustomHTMLLayout")
                .addAttribute("charset", "UTF-8");

        String filePattern = FilenameUtils.getFullPath(filePath) + FilenameUtils.getBaseName(filePath) +
                "_%i." + FilenameUtils.getExtension(filePath);

        return builder.newAppender(appenderName, "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", filePattern)
                .addAttribute("immediateFlush", true)
                .addAttribute("append", true)
                .addComponent(layoutBuilder)
                .addComponent(builder.newComponent("Policies")
                        .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                                .addAttribute("size", sizeLimitMB + "MB")))
                .addComponent(builder.newComponent("DefaultRolloverStrategy").addAttribute("fileIndex", "nomax"));
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void close() {
        if (!isClosed()) {
            loggerContext.stop();
            // No removemos del map para mantener la reutilización
        }
    }

    @Override
    public boolean isClosed() {
        return loggerContext.isStopped();
    }

    public String getScreenshotFolderPath(Level level) {
        return screenshotFolderPaths.getOrDefault(level, screenshotFolderPaths.get(Level.INFO));
    }

    public String getVariablesFolderPath(Level level) {
        return variablesFolderPaths.getOrDefault(level, variablesFolderPaths.get(Level.INFO));
    }
}
