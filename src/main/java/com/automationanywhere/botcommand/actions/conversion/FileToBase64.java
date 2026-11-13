/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.conversion;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class FileToBase64 {
    private static final long MAX_FILE_SIZE_MB = 20L;

    public Value<String> action(String filePath) {
        File file = FileToBase64.validateFile(filePath);
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(content);
            return new StringValue(base64);
        }
        catch (IOException ex) {
            throw new BotCommandException("Error al leer el archivo.", ex);
        }
    }

    private static File validateFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new BotCommandException("La ruta del archivo no puede estar vac\u00eda.");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BotCommandException("El archivo no existe: " + filePath);
        }
        if (!file.isFile()) {
            throw new BotCommandException("La ruta especificada no corresponde a un archivo.");
        }
        if (!file.canRead()) {
            throw new BotCommandException("No se tienen permisos de lectura sobre el archivo.");
        }
        long maxBytes = 0x1400000L;
        if (file.length() > maxBytes) {
            throw new BotCommandException("El archivo supera el tama\u00f1o m\u00e1ximo permitido de 20 MB.");
        }
        return file;
    }
}
