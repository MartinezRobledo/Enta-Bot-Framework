/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.BooleanValue;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;

public class CopyFile {
    private static final String PATH_RELATIVE = "RELATIVE";
    private static final String PATH_ABSOLUTE = "ABSOLUTE";

    public DictionaryValue action(String pathType, String baseFolderPath, String sourcePath, String destinationPath, Boolean destinationIsFolder, Boolean createMissingFolders, Boolean overwriteExisting) {
        try {
            CopyOption[] copyOptionArray;
            Path dst;
            Path dstBase;
            Path src;
            boolean overwrite;
            boolean createDirs = createMissingFolders == null ? true : createMissingFolders;
            boolean bl = overwrite = overwriteExisting != null && overwriteExisting != false;
            if (PATH_RELATIVE.equalsIgnoreCase(pathType)) {
                if (baseFolderPath == null || baseFolderPath.isBlank()) {
                    throw new BotCommandException("Base folder path es obligatorio en modo RELATIVE.");
                }
                Path base = Paths.get(baseFolderPath, new String[0]).toAbsolutePath().normalize();
                src = base.resolve(sourcePath).normalize();
                dstBase = base.resolve(destinationPath).normalize();
            } else if (PATH_ABSOLUTE.equalsIgnoreCase(pathType)) {
                src = Paths.get(sourcePath, new String[0]).toAbsolutePath().normalize();
                dstBase = Paths.get(destinationPath, new String[0]).toAbsolutePath().normalize();
            } else {
                throw new BotCommandException("Path mode inv\u00e1lido: " + pathType);
            }
            if (!Files.exists(src, new LinkOption[0])) {
                throw new BotCommandException("El archivo de origen no existe: " + String.valueOf(src));
            }
            if (Files.isDirectory(src, new LinkOption[0])) {
                throw new BotCommandException("El origen es un directorio. Esta acci\u00f3n solo copia archivos.");
            }
            boolean destIsFolderFlag = destinationIsFolder != null && destinationIsFolder != false;
            boolean destExistsDir = Files.exists(dstBase, new LinkOption[0]) && Files.isDirectory(dstBase, new LinkOption[0]);
            boolean treatAsFolder = destIsFolderFlag || destExistsDir;
            Path path = dst = treatAsFolder ? dstBase.resolve(src.getFileName()).normalize() : dstBase.normalize();
            if (src.equals(dst)) {
                throw new BotCommandException("El destino es igual al origen. Operaci\u00f3n no v\u00e1lida.");
            }
            Path destParent = dst.getParent();
            if (destParent != null && !Files.exists(destParent, new LinkOption[0])) {
                if (createDirs) {
                    Files.createDirectories(destParent, new FileAttribute[0]);
                } else {
                    throw new BotCommandException("La carpeta destino no existe y 'Create missing folders' = false: " + String.valueOf(destParent));
                }
            }
            boolean existedBefore = Files.exists(dst, new LinkOption[0]);
            if (overwrite) {
                CopyOption[] copyOptionArray2 = new CopyOption[2];
                copyOptionArray2[0] = StandardCopyOption.REPLACE_EXISTING;
                copyOptionArray = copyOptionArray2;
                copyOptionArray2[1] = StandardCopyOption.COPY_ATTRIBUTES;
            } else {
                CopyOption[] copyOptionArray3 = new CopyOption[1];
                copyOptionArray = copyOptionArray3;
                copyOptionArray3[0] = StandardCopyOption.COPY_ATTRIBUTES;
            }
            CopyOption[] options = copyOptionArray;
            Files.copy(src, dst, options);
            BasicFileAttributes srcAttr = Files.readAttributes(src, BasicFileAttributes.class, new LinkOption[0]);
            long bytes = srcAttr.size();
            boolean renamed = !src.getFileName().toString().equals(dst.getFileName().toString());
            HashMap<String, Value> ret = new HashMap<String, Value>();
            ret.put("SourcePath", new StringValue(src.toString()));
            ret.put("DestinationPath", new StringValue(dst.toString()));
            ret.put("DestinationParent", new StringValue(destParent == null ? "" : destParent.toString()));
            ret.put("Renamed", new BooleanValue(renamed));
            ret.put("Overwritten", new BooleanValue(existedBefore && overwrite));
            ret.put("BytesCopied", new NumberValue(bytes));
            return new DictionaryValue(ret);
        }
        catch (Exception e) {
            throw new BotCommandException("CopyFile fall\u00f3: " + e.getMessage(), e);
        }
    }
}
