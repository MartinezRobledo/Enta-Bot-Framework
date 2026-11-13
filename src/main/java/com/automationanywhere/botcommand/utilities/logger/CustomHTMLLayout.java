/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.logger;

import botframework.shaded.commons.io.IOUtils;
import com.automationanywhere.botcommand.utilities.logger.HTMLGenerator;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

@Plugin(name="CustomHTMLLayout", category="Core", elementType="layout", printObject=true)
public class CustomHTMLLayout
extends AbstractStringLayout {
    private static final byte[] footer = "".getBytes();
    private static final String templatePath = "/templates/log.html";
    private static final byte[] header;
    private static final String machine;
    private static final String user;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss Z");

    public CustomHTMLLayout(Charset charset) {
        super(charset);
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toSerializable(LogEvent event) {
        String message;
        String sourceBotPath = "";
        Object variablesLink = "";
        String screenshotLink = "";
        Object[] parameters = event.getMessage().getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] instanceof Map) {
            Map messageObject = (Map)parameters[0];
            message = Optional.ofNullable(messageObject.get("Message")).map(Object::toString).orElse("");
            sourceBotPath = Optional.ofNullable(messageObject.get("Source")).map(Object::toString).orElse("");
            String screenshotPath = Optional.ofNullable(messageObject.get("Screenshot")).map(Object::toString).orElse("");
            if (messageObject.get("Variables") != null && messageObject.get("Variables") instanceof Map) {
                String logEventId;
                String variablesFolderPath;
                String variablesFilePath;
                Map variableMap = (Map)messageObject.get("Variables");
                String variablesCount = HTMLGenerator.generateHTML(variableMap);
                variablesLink = messageObject.get("variablesFolderPath") != null ? (!(variablesFilePath = HTMLGenerator.generateVariableFile(variableMap, variablesFolderPath = messageObject.get("variablesFolderPath").toString(), logEventId = UUID.randomUUID().toString())).isEmpty() ? "<a href='" + variablesFilePath + "' target='_blank' class='vars-link'>" + variablesCount + "</a>" : variablesCount) : variablesCount;
            }
            screenshotLink = HTMLGenerator.getScreenshotHTML(screenshotPath);
        } else {
            message = event.getMessage().getFormattedMessage();
        }
        String levelClass = "level-" + event.getLevel().toString();
        String formattedContent = String.format("<tr><td>%s</td><td class='%s'>%s</td><td>%s</td><td class='responsive-hide'>%s</td><td class='responsive-hide'>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", StringEscapeUtils.escapeHtml4(this.dateFormat.format(event.getTimeMillis())), levelClass, StringEscapeUtils.escapeHtml4(event.getLevel().toString()), StringEscapeUtils.escapeHtml4(sourceBotPath), StringEscapeUtils.escapeHtml4(machine), StringEscapeUtils.escapeHtml4(user), StringEscapeUtils.escapeHtml4(message), variablesLink, screenshotLink);
        return formattedContent;
    }

    @Override
    public byte[] getFooter() {
        return footer;
    }

    @Override
    public byte[] getHeader() {
        return header;
    }

    static {
        try {
            header = IOUtils.toByteArray(Objects.requireNonNull(CustomHTMLLayout.class.getResourceAsStream(templatePath)));
            machine = InetAddress.getLocalHost().getHostName();
            user = System.getProperty("user.name");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Columns {
        public static final String SOURCE = "Source";
        public static final String MESSAGE = "Message";
        public static final String VARIABLES = "Variables";
        public static final String SCREENSHOT = "Screenshot";
        public static final String VARIABLES_FOLDER_PATH = "variablesFolderPath";
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<CustomHTMLLayout> {
        @PluginBuilderAttribute
        private Charset charset;

        private Builder() {
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        @Override
        public CustomHTMLLayout build() {
            return new CustomHTMLLayout(this.charset);
        }
    }
}
