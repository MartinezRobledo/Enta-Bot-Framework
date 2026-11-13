package com.automationanywhere.botcommand.utilities.file;

import org.apache.commons.io.FilenameUtils;
import java.nio.file.LinkOption;


import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileValidator {

    private final Path path;

    public FileValidator(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        this.path = Paths.get(filePath, new String[0]);
    }

    public void validateFile(String[] allowedExtensions) {
        this.validateCommonConditions();
        if (Files.isDirectory(this.path, new LinkOption[0])) {
            throw new IllegalArgumentException("Specified path is a directory, not a file: " + String.valueOf(this.path));
        }
        if (!Files.isRegularFile(this.path, new LinkOption[0])) {
            throw new IllegalArgumentException("Specified path does not point to a valid file: " + String.valueOf(this.path));
        }
        String fileExtension = FilenameUtils.getExtension(this.path.getFileName().toString());
        String lowerCaseFileExtension = fileExtension.toLowerCase();
        boolean isExtensionValid = Arrays.stream(allowedExtensions).map(String::toLowerCase).anyMatch(lowerCaseFileExtension::equals);
        if (!isExtensionValid) {
            throw new IllegalArgumentException("Invalid file extension. Expected: " + Arrays.toString(allowedExtensions) + ", Actual: " + lowerCaseFileExtension);
        }
    }

    private void validateCommonConditions() {
        if (!Files.exists(this.path, new LinkOption[0])) {
            throw new IllegalArgumentException("File does not exist at the specified path: " + String.valueOf(this.path));
        }
        if (!Files.isReadable(this.path)) {
            throw new IllegalArgumentException("Cannot read the specified file: " + String.valueOf(this.path));
        }
    }

    public void validateDirectory() {
        this.validateCommonConditions();
        if (!Files.isDirectory(this.path, new LinkOption[0])) {
            throw new IllegalArgumentException("Specified path is not a directory: " + String.valueOf(this.path));
        }
    }
}
