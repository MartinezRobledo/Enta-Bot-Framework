package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.data.impl.DictionaryValue;
import com.automationanywhere.botcommand.data.impl.BooleanValue;
import com.automationanywhere.botcommand.data.impl.NumberValue;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@BotCommand
@CommandPkg(
        label = "Copy Folder",
        name = "device_copy_folder",
        node_label = "Copy all files from {{sourceFolder}} to {{destinationFolder}} | mode {{pathType}}",
        description = "Copia todos los archivos dentro de una carpeta origen a una carpeta destino. Ignora subcarpetas.",
        icon = "folders.svg",
        group_label = "Device"
)
public class CopyFolder {

    private static final String PATH_RELATIVE = "RELATIVE";
    private static final String PATH_ABSOLUTE = "ABSOLUTE";

    @Execute
    public DictionaryValue action(
            @Idx(index = "1", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.1", pkg = @Pkg(label = "Relative Path", value = PATH_RELATIVE)),
                    @Idx.Option(index = "1.2", pkg = @Pkg(label = "Absolute Path", value = PATH_ABSOLUTE))
            })
            @Pkg(label = "Path mode", default_value = PATH_RELATIVE, default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String pathType,

            @Idx(index = "1.1.1", type = AttributeType.FILE)
            @Pkg(label = "Base folder path", description = "Carpeta raíz para resolver rutas relativas")
            @FileFolder
            String baseFolderPath,

            @Idx(index = "2", type = AttributeType.FILE)
            @Pkg(label = "Source folder path", description = "Ruta de la carpeta origen")
            @NotEmpty
            String sourceFolder,

            @Idx(index = "3", type = AttributeType.FILE)
            @Pkg(label = "Destination folder path", description = "Ruta de la carpeta destino")
            @NotEmpty
            String destinationFolder,

            @Idx(index = "4", type = AttributeType.CHECKBOX)
            @Pkg(label = "Create missing folders", default_value = "true", default_value_type = DataType.BOOLEAN)
            Boolean createMissingFolders,

            @Idx(index = "5", type = AttributeType.CHECKBOX)
            @Pkg(label = "Overwrite if exists", default_value = "false", default_value_type = DataType.BOOLEAN)
            Boolean overwriteExisting
    ) {
        try {
            boolean createDirs = createMissingFolders == null || createMissingFolders;
            boolean overwrite = overwriteExisting != null && overwriteExisting;

            final Path srcFolder;
            final Path dstFolder;

            if (PATH_RELATIVE.equalsIgnoreCase(pathType)) {
                if (baseFolderPath == null || baseFolderPath.isBlank()) {
                    throw new BotCommandException("Base folder path es obligatorio en modo RELATIVE.");
                }
                Path base = Paths.get(baseFolderPath).toAbsolutePath().normalize();
                srcFolder = base.resolve(sourceFolder).normalize();
                dstFolder = base.resolve(destinationFolder).normalize();
            } else if (PATH_ABSOLUTE.equalsIgnoreCase(pathType)) {
                srcFolder = Paths.get(sourceFolder).toAbsolutePath().normalize();
                dstFolder = Paths.get(destinationFolder).toAbsolutePath().normalize();
            } else {
                throw new BotCommandException("Path mode inválido: " + pathType);
            }

            if (!Files.exists(srcFolder) || !Files.isDirectory(srcFolder)) {
                throw new BotCommandException("La carpeta origen no existe o no es válida: " + srcFolder);
            }

            if (!Files.exists(dstFolder)) {
                if (createDirs) {
                    Files.createDirectories(dstFolder);
                } else {
                    throw new BotCommandException("La carpeta destino no existe y 'Create missing folders' = false: " + dstFolder);
                }
            }

            List<Value> copiedFiles = new ArrayList<>();
            double totalBytes = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcFolder)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Path destFile = dstFolder.resolve(file.getFileName()).normalize();
                        if (file.equals(destFile)) continue;

                        boolean existedBefore = Files.exists(destFile);
                        CopyOption[] options = overwrite
                                ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES}
                                : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

                        Files.copy(file, destFile, options);

                        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                        double bytesCopied = attr.size();
                        totalBytes += bytesCopied;

                        Map<String, Value> fileInfo = new HashMap<>();
                        fileInfo.put("SourcePath", new StringValue(file.toString()));
                        fileInfo.put("DestinationPath", new StringValue(destFile.toString()));
                        fileInfo.put("Overwritten", new BooleanValue(existedBefore && overwrite));
                        fileInfo.put("BytesCopied", new NumberValue(bytesCopied));

                        copiedFiles.add(new DictionaryValue(fileInfo));
                    }
                }
            }

            Map<String, Value> result = new HashMap<>();
            result.put("SourceFolder", new StringValue(srcFolder.toString()));
            result.put("DestinationFolder", new StringValue(dstFolder.toString()));
            Map<String, Value> filesMap = new HashMap<>();
            int i = 1;
            for (Value fileInfo : copiedFiles) {
                filesMap.put("File" + i++, fileInfo);
            }
            result.put("FilesCopied", new DictionaryValue(filesMap));
            result.put("TotalBytesCopied", new NumberValue(totalBytes));

            return new DictionaryValue(result);

        } catch (Exception e) {
            throw new BotCommandException("CopyFolder falló: " + e.getMessage(), e);
        }
    }
}