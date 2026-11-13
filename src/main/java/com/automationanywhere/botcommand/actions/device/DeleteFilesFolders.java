/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import botframework.shaded.commons.io.FileUtils;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

    public void action(String inputFolderPath, String selectMethod, Boolean recursive, Number thresholdNumber, String thresholdUnit, String thresholdCriteria, Boolean skipFolders, String skipFolderPathPattern, Boolean skipFiles, String skipFilePathPattern, String unableToDeleteBehavior) {
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

    private static class DeletionProcessor {
        private final Path basePath;
        private final Set<Path> filesToDelete;
        private final Set<Path> directoriesToDelete;
        private final Set<Path> directoriesToPreserve = new HashSet<Path>();

        public DeletionProcessor(Path basePath, Set<Path> filesToDelete, Set<Path> directoriesToDelete, Set<Path> filesToSkip, Set<Path> directoriesToSkip) {
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

    private static class FileCollector
    extends SimpleFileVisitor<Path> {
        private final Path basePath;
        private final boolean recursive;
        private final String thresholdCriteria;
        private final Instant deletionThresholdInstant;
        private final boolean skipFiles;
        private final String skipFilePathPattern;
        private final boolean skipFolders;
        private final String skipFolderPathPattern;
        private final Set<Path> filesToDelete = new HashSet<Path>();
        private final Set<Path> directoriesToDelete = new HashSet<Path>();
        private final Set<Path> filesToSkip = new HashSet<Path>();
        private final Set<Path> directoriesToSkip = new HashSet<Path>();

        public FileCollector(Path basePath, boolean recursive, String thresholdCriteria, Instant deletionThresholdInstant, boolean skipFiles, String skipFilePathPattern, boolean skipFolders, String skipFolderPathPattern) {
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
}
