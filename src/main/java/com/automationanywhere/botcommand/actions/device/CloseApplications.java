/*
 * Decompiled with CFR 0.152.
 */
package com.automationanywhere.botcommand.actions.device;

import com.automationanywhere.botcommand.data.Value;
import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.process.TaskKiller;
import java.util.ArrayList;
import java.util.List;

public class CloseApplications {
    public void action(List<Value> killTaskList) {
        try {
            if (killTaskList == null) {
                return;
            }
            ArrayList<String> processes = new ArrayList<String>();
            for (Value value : killTaskList) {
                processes.add(value.get().toString().trim());
            }
            TaskKiller.killProcesses(processes, false);
            TaskKiller.killProcesses(processes, true);
        }
        catch (Exception e) {
            throw new BotCommandException("Error occurred while terminating applications: " + e.getMessage());
        }
    }
}
