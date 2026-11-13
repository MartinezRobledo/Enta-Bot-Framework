/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.utilities.process;

import botframework.shaded.commons.io.FilenameUtils;
import com.automationanywhere.botcommand.exception.BotCommandException;
import java.util.List;
import java.util.Optional;

public class TaskKiller {
    public static void killProcesses(List<String> processNames, boolean forceKill) {
        try {
            for (String processName : processNames) {
                Optional<ProcessHandle> processHandleOptional = TaskKiller.findByExactCommand(processName);
                processHandleOptional.ifPresent(processHandle -> {
                    try {
                        if (forceKill) {
                            processHandle.destroyForcibly();
                        } else {
                            processHandle.destroy();
                        }
                        processHandle.onExit().get();
                    }
                    catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                });
            }
        }
        catch (Exception e) {
            throw new BotCommandException("Error killing processes: " + e.getMessage());
        }
    }

    private static Optional<ProcessHandle> findByExactCommand(String processName) {
        String baseProcessName = FilenameUtils.getBaseName(processName);
        return ProcessHandle.allProcesses().filter(process -> {
            Optional<String> cmd = process.info().command();
            return cmd.isPresent() && FilenameUtils.getBaseName(cmd.get()).equalsIgnoreCase(baseProcessName);
        }).findFirst();
    }
}
