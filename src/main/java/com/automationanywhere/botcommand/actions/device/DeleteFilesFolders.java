package com.automationanywhere.botcommand.actions.device;

import org.apache.commons.io.FileUtils;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.*;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.logging.Logger;

@BotCommand
@CommandPkg(
        label = "Clean Directory",
        node_label = "{{inputFolderPath}} by deleting {{selectMethod}} {{recursive}}",
        description = "Remove files/folders based on rule set",
        icon = "delete_folders.svg",
        name = "device_delete_files_folders",
        group_label = "Device",
        documentation_url = "https://github.com/A360-Tools/Bot-Framework/blob/main/docs/device/DeleteFilesFolders.md"
)
public class DeleteFilesFolders {
    private static final String THRESHOLD_UNIT_DAY = "DAY";
    private static final String THRESHOLD_UNIT_HOUR = "HOUR";
    private static final String THRESHOLD_UNIT_MINUTE = "MINUTE";
    private static final String THRESHOLD_UNIT_SECOND = "SECOND";
    private static final String THRESHOLD_CRITERIA_CREATION = "CREATION";
    private static final String THRESHOLD_CRITERIA_MODIFICATION = "MODIFICATION";
    private static final String PROCESS_ONLY_FILE_TYPE = "FILE";
    private static final String PROCESS_ALL_TYPES = "ALL";
    private static final String ERROR_THROW = "THROW";
    private static final String ERROR_IGNORE = "IGNORE";
    private static final Logger LOGGER = Logger.getLogger(DeleteFilesFolders.class.getName());

    @Execute
    public void action(
            @Idx(index = "1", type = AttributeType.FILE)
            @Pkg(label = "Enter base folder path", description = "Files/Folders will be scanned within this folder " +
                    "for deletion")
            @NotEmpty
            @FileFolder
            String inputFolderPath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "Directories and Files", value = PROCESS_ALL_TYPES,
                            node_label = "it's directories and files")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "Files Only", value = PROCESS_ONLY_FILE_TYPE,
                            node_label = "it's files"))})
            @Pkg(label = "Deletion option", default_value = PROCESS_ALL_TYPES,
                    default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String selectMethod,

            @Idx(index = "3", type = AttributeType.CHECKBOX)
            @Pkg(label = "All subdirectories are searched as well", default_value = "true",
                    node_label = "Action all subdirectories",
                    default_value_type = DataType.BOOLEAN)
            Boolean recursive,

            @Idx(index = "4", type = AttributeType.NUMBER)
            @Pkg(label = "Threshold number", default_value_type = DataType.NUMBER, default_value = "30",
                    description = "any file/folder with threshold age value older than this value will be " +
                            "deleted")
            @NotEmpty
            @GreaterThanEqualTo("0")
            @NumberInteger
            Number thresholdNumber,

            @Idx(index = "5", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "5.1", pkg = @Pkg(label = "DAY", value = THRESHOLD_UNIT_DAY)),
                    @Idx.Option(index = "5.2", pkg = @Pkg(label = "HOUR", value = THRESHOLD_UNIT_HOUR)),
                    @Idx.Option(index = "5.3", pkg = @Pkg(label = "MINUTE", value = THRESHOLD_UNIT_MINUTE)),
                    @Idx.Option(index = "5.4", pkg = @Pkg(label = "SECOND", value = THRESHOLD_UNIT_SECOND))})
            @Pkg(label = "Threshold Unit", default_value = THRESHOLD_UNIT_DAY,
                    default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String thresholdUnit,

            @Idx(index = "6", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "6.1", pkg = @Pkg(label = "CREATION", value = THRESHOLD_CRITERIA_CREATION)),
                    @Idx.Option(index = "6.2", pkg = @Pkg(label = "LAST MODIFICATION", value =
                            THRESHOLD_CRITERIA_MODIFICATION))})
            @Pkg(label = "Threshold age type", default_value = THRESHOLD_CRITERIA_CREATION,
                    default_value_type = DataType.STRING)
            @NotEmpty
            @SelectModes
            String thresholdCriteria,

            @Idx(index = "7", type = AttributeType.CHECKBOX)
            @Pkg(label = "Ignore specific folder paths", default_value = "false", default_value_type =
                    DataType.BOOLEAN)
            Boolean skipFolders,

            @Idx(index = "7.1", type = AttributeType.TEXT)
            @Pkg(label = "Regex pattern to match folder paths to ignore",
                    description = ".*\\\\subDirectory" + " to skip folder called subDirectory on windows platform" +
                            "Matching will be done on absolute path in OS file separator format.")
            @NotEmpty
            String skipFolderPathPattern,

            @Idx(index = "8", type = AttributeType.CHECKBOX)
            @Pkg(label = "Ignore specific file paths", default_value = "false", default_value_type =
                    DataType.BOOLEAN)
            Boolean skipFiles,

            @Idx(index = "8.1", type = AttributeType.TEXT)
            @Pkg(label = "Regex pattern to match file paths to ignore", description =
                    ".*\\.txt$" + " to skip all text files on windows platform. Matching will be done on absolute " +
                            "path in OS file separator format.")
            @NotEmpty
            String skipFilePathPattern,

            @Idx(index = "9", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "9.1", pkg = @Pkg(label = "Throw error", value = ERROR_THROW)),
                    @Idx.Option(index = "9.2", pkg = @Pkg(label = "Ignore", value = ERROR_IGNORE))
            })
            @Pkg(label = "If certain files/folders cannot be deleted", default_value_type = DataType.STRING,
                    description =
                            "Behavior in case a file is locked/missing permission", default_value = ERROR_IGNORE)
            @NotEmpty
            String unableToDeleteBehavior
    ) {
        try {
            Path basePath = Paths.get(inputFolderPath, new String[0]);
            if (!Files.exists(basePath, new LinkOption[0])) {
                throw new BotCommandException("Base folder path does not exist: " + inputFolderPath);
            }
            LOGGER.info("Starting deletion process for base path: " + String.valueOf(basePath));
            FileCollector collector = new FileCollector(basePath, recursive, thresholdCriteria, this.calculateAgeThreshold(thresholdNumber.longValue(), thresholdUnit), skipFiles, skipFilePathPattern, skipFolders, skipFolderPathPattern);
            Files.walkFileTree(basePath, collector);
            DeletionProcessor processor = new DeletionProcessor(basePath, collector.getFilesToDelete(), collector.getDirectoriesToDelete(), collector.getFilesToSkip(), collector.getDirectoriesToSkip());
            if (selectMethod.equalsIgnoreCase(PROCESS_ONLY_FILE_TYPE) || selectMethod.equalsIgnoreCase(PROCESS_ALL_TYPES)) {
                this.delete(processor.getFilesToDelete(), unableToDeleteBehavior);
            }
            if (selectMethod.equalsIgnoreCase(PROCESS_ALL_TYPES)) {
                this.delete(processor.getSortedDirectoriesToDelete(), unableToDeleteBehavior);
            }
            LOGGER.info("Deletion process completed successfully");
        }
        catch (IOException e) {
            LOGGER.severe("IO error occurred: " + e.getMessage());
            if (unableToDeleteBehavior.equalsIgnoreCase(ERROR_THROW)) {
                throw new BotCommandException("IO error: " + e.getMessage(), e);
            }
        }
        catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            throw new BotCommandException("Error: " + e.getMessage(), e);
        }
    }

    private Instant calculateAgeThreshold(long threshold, String unit) {
        Instant now = Instant.now();
        switch (unit) {
            case "DAY": {
                return now.minus(threshold, ChronoUnit.DAYS);
            }
            case "HOUR": {
                return now.minus(threshold, ChronoUnit.HOURS);
            }
            case "MINUTE": {
                return now.minus(threshold, ChronoUnit.MINUTES);
            }
            case "SECOND": {
                return now.minus(threshold, ChronoUnit.SECONDS);
            }
        }
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }

    private void delete(List<Path> pathsToDelete, String unableToDeleteBehavior) {
        for (Path path : pathsToDelete) {
            try {
                LOGGER.info("Deleting: " + String.valueOf(path));
                FileUtils.forceDelete(path.toFile());
            }
            catch (IOException e) {
                LOGGER.warning("Failed to delete " + String.valueOf(path) + ": " + e.getMessage());
                if (!unableToDeleteBehavior.equalsIgnoreCase(ERROR_THROW)) continue;
                throw new BotCommandException("Failed to delete " + String.valueOf(path) + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Helper class that collects files and directories during file tree traversal.
     */
    private static class FileCollector extends SimpleFileVisitor<Path> {
        private final Path basePath;
        private final boolean recursive;
        private final String thresholdCriteria;
        private final Instant deletionThresholdInstant;
        private final boolean skipFiles;
        private final String skipFilePathPattern;
        private final boolean skipFolders;
        private final String skipFolderPathPattern;

        private final Set<Path> filesToDelete = new HashSet<>();
        private final Set<Path> directoriesToDelete = new HashSet<>();
        private final Set<Path> filesToSkip = new HashSet<>();
        private final Set<Path> directoriesToSkip = new HashSet<>();

        public FileCollector(
                Path basePath,
                boolean recursive,
                String thresholdCriteria,
                Instant deletionThresholdInstant,
                boolean skipFiles,
                String skipFilePathPattern,
                boolean skipFolders,
                String skipFolderPathPattern) {
            this.basePath = basePath;
            this.recursive = recursive;
            this.thresholdCriteria = thresholdCriteria;
            this.deletionThresholdInstant = deletionThresholdInstant;
            this.skipFiles = skipFiles;
            this.skipFilePathPattern = skipFilePathPattern;
            this.skipFolders = skipFolders;
            this.skipFolderPathPattern = skipFolderPathPattern;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (dir.equals(this.basePath)) {
                return FileVisitResult.CONTINUE;
            }
            if (this.skipFolders && this.matchesPattern(dir.toFile().getAbsolutePath(), this.skipFolderPathPattern)) {
                LOGGER.info("Skipping directory based on pattern: " + String.valueOf(dir));
                this.directoriesToSkip.add(dir);
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (this.meetsDeletionCriteria(attrs)) {
                LOGGER.info("Marking directory for potential deletion: " + String.valueOf(dir));
                this.directoriesToDelete.add(dir);
            }
            if (!this.recursive) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (this.skipFiles && this.matchesPattern(file.toFile().getAbsolutePath(), this.skipFilePathPattern)) {
                LOGGER.info("Skipping file based on pattern: " + String.valueOf(file));
                this.filesToSkip.add(file);
                return FileVisitResult.CONTINUE;
            }
            if (this.meetsDeletionCriteria(attrs)) {
                LOGGER.info("Marking file for deletion: " + String.valueOf(file));
                this.filesToDelete.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            LOGGER.warning("Failed to visit file: " + String.valueOf(file) + " - " + exc.getMessage());
            return FileVisitResult.CONTINUE;
        }

        private boolean meetsDeletionCriteria(BasicFileAttributes attrs) {
            Instant fileTime;
            switch (this.thresholdCriteria) {
                case "CREATION": {
                    fileTime = attrs.creationTime().toInstant();
                    break;
                }
                case "MODIFICATION": {
                    fileTime = attrs.lastModifiedTime().toInstant();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported threshold criteria: " + this.thresholdCriteria);
                }
            }
            return fileTime.isBefore(this.deletionThresholdInstant);
        }

        private boolean matchesPattern(String pathString, String pattern) {
            return Pattern.matches(pattern, pathString);
        }

        public Set<Path> getFilesToDelete() {
            return this.filesToDelete;
        }

        public Set<Path> getDirectoriesToDelete() {
            return this.directoriesToDelete;
        }

        public Set<Path> getFilesToSkip() {
            return this.filesToSkip;
        }

        public Set<Path> getDirectoriesToSkip() {
            return this.directoriesToSkip;
        }
    }

    /**
     * Helper class that processes file and directory lists to resolve conflicts
     * and prepare for deletion.
     */
    private static class DeletionProcessor {
        private final Path basePath;
        private final Set<Path> filesToDelete;
        private final Set<Path> directoriesToDelete;
        private final Set<Path> directoriesToPreserve = new HashSet<>();

        public DeletionProcessor(
                Path basePath,
                Set<Path> filesToDelete,
                Set<Path> directoriesToDelete,
                Set<Path> filesToSkip,
                Set<Path> directoriesToSkip) {
            this.basePath = basePath;
            this.filesToDelete = new HashSet<Path>(filesToDelete);
            this.directoriesToDelete = new HashSet<Path>(directoriesToDelete);
            this.directoriesToPreserve.add(basePath);
            for (Path skippedFile : filesToSkip) {
                this.addParentsToPreserveList(skippedFile);
            }
            for (Path skippedDir : directoriesToSkip) {
                this.directoriesToPreserve.add(skippedDir);
                this.addParentsToPreserveList(skippedDir);
            }
            this.directoriesToDelete.removeAll(this.directoriesToPreserve);
            LOGGER.info("Files to delete after processing: " + this.filesToDelete.size());
            LOGGER.info("Directories to delete after processing: " + this.directoriesToDelete.size());
            LOGGER.info("Directories preserved for containing skipped items: " + (this.directoriesToPreserve.size() - 1));
        }

        private void addParentsToPreserveList(Path path) {
            for (Path parent = path.getParent(); parent != null && !parent.equals(this.basePath); parent = parent.getParent()) {
                this.directoriesToPreserve.add(parent);
            }
        }

        public List<Path> getFilesToDelete() {
            return new ArrayList<Path>(this.filesToDelete);
        }

        public List<Path> getSortedDirectoriesToDelete() {
            ArrayList<Path> sortedDirectories = new ArrayList<Path>(this.directoriesToDelete);
            sortedDirectories.sort((p1, p2) -> Integer.compare(p2.getNameCount(), p1.getNameCount()));
            return sortedDirectories;
        }
    }
}
