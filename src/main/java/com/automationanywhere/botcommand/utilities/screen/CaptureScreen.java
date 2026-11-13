/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.screen;

import botframework.shaded.commons.io.FilenameUtils;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;

public class CaptureScreen {
    public static void captureDesktop(String filePath, boolean isOverwriteFile) {
        try {
            if (StringUtils.isBlank(filePath)) {
                throw new BotCommandException("Empty screenshot path provided");
            }
            Path path = Paths.get(filePath, new String[0]);
            File file = path.toFile();
            if (!isOverwriteFile && file.exists()) {
                throw new BotCommandException("Screenshot path already exists " + String.valueOf(path));
            }
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRectangle = new Rectangle(screenSize);
            BufferedImage image = robot.createScreenCapture(screenRectangle);
            String extension = FilenameUtils.getExtension(filePath);
            CaptureScreen.saveFile(image, extension, file);
        }
        catch (Exception ex) {
            throw new BotCommandException("Unable to save screenshot " + ex.getMessage());
        }
    }

    private static void saveFile(BufferedImage screenCapture, String extension, File fileToSave) throws IOException {
        if ("wmf".equalsIgnoreCase(extension)) {
            extension = "png";
        }
        ImageIO.write((RenderedImage)screenCapture, extension, fileToSave);
    }
}
