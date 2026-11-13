package com.automationanywhere.botcommand.actions.device;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;

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
import java.util.HashMap;

@BotCommand
@CommandPkg(
        label = "Copy File",
        name = "device_copy_file",
        node_label = "Copy {{sourcePath}} to {{destinationPath}} | mode {{pathType}} {{overwriteExisting}} {{createMissingFolders}}",
        description = "Copia un archivo desde un origen a un destino. Permite renombrar el archivo en el destino y crear carpetas faltantes.",
        icon = "folders.svg",
        group_label = "Device"
)
public class CopyFile {

    private static final String PATH_RELATIVE = "RELATIVE";
    private static final String PATH_ABSOLUTE = "ABSOLUTE";

    @Execute
    public DictionaryValue action(
            // Modo de rutas
            @Idx(index = "1", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "1.1", pkg = @Pkg(label = "Relative Path", value = PATH_RELATIVE)),
                    @Idx.Option(index = "1.2", pkg = @Pkg(label = "Absolute Path", value = PATH_ABSOLUTE))
            })
            @Pkg(label = "Path mode", default_value = PATH_RELATIVE, default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String pathType,

            // Base folder para modo relativo
            @Idx(index = "1.1.1", type = AttributeType.FILE)
            @Pkg(label = "Base folder path", description = "Carpeta raíz para resolver rutas relativas")
            @NotEmpty
            @FileFolder
            String baseFolderPath,

            // Origen (archivo)
            @Idx(index = "2", type = AttributeType.FILE)
            @Pkg(label = "Source file path", description = "Ruta del archivo de origen")
            @NotEmpty
            String sourcePath,

            // Destino: puede ser ruta completa a archivo (permite renombrar) o carpeta si se marca la opción
            @Idx(index = "3", type = AttributeType.FILE)
            @Pkg(
                    label = "Destination path",
                    description = "Si 'Destination is a folder' está activo o el destino ya es una carpeta, se reutiliza el nombre del archivo de origen."
            )
            @NotEmpty
            String destinationPath,

            @Idx(index = "4", type = AttributeType.BOOLEAN)
            @Pkg(label = "Destination is a folder", default_value = "false", default_value_type = DataType.BOOLEAN)
            Boolean destinationIsFolder,

            // Opciones
            @Idx(index = "5", type = AttributeType.BOOLEAN)
            @Pkg(label = "Create missing folders", description = "Crear las carpetas del destino si no existen",
                    default_value = "true", default_value_type = DataType.BOOLEAN)
            Boolean createMissingFolders,

            @Idx(index = "6", type = AttributeType.BOOLEAN)
            @Pkg(label = "Overwrite if exists", description = "Sobrescribir si el archivo destino ya existe",
                    default_value = "false", default_value_type = DataType.BOOLEAN)
            Boolean overwriteExisting
    ) {
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
                throw new BotCommandException("Path mode inválido: " + pathType);
            }
            if (!Files.exists(src, new LinkOption[0])) {
                throw new BotCommandException("El archivo de origen no existe: " + String.valueOf(src));
            }
            if (Files.isDirectory(src, new LinkOption[0])) {
                throw new BotCommandException("El origen es un directorio. Esta acción solo copia archivos.");
            }
            boolean destIsFolderFlag = destinationIsFolder != null && destinationIsFolder != false;
            boolean destExistsDir = Files.exists(dstBase, new LinkOption[0]) && Files.isDirectory(dstBase, new LinkOption[0]);
            boolean treatAsFolder = destIsFolderFlag || destExistsDir;
            Path path = dst = treatAsFolder ? dstBase.resolve(src.getFileName()).normalize() : dstBase.normalize();
            if (src.equals(dst)) {
                throw new BotCommandException("El destino es igual al origen. Operación no válida.");
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
            throw new BotCommandException("CopyFile falló: " + e.getMessage(), e);
        }
    }
}
