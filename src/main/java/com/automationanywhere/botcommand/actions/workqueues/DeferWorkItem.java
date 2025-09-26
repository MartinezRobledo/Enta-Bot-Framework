package com.automationanywhere.botcommand.actions.workqueues;

import com.automationanywhere.botcommand.exception.BotCommandException;
import com.automationanywhere.botcommand.utilities.workqueue.AccessExecutor;
import com.automationanywhere.botcommand.utilities.workqueue.WorkqueueItemDao;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.commandsdk.model.DataType;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


@BotCommand
@CommandPkg(
        label = "Defer Work Item",
        name = "workqueue_defer_item",
        description = "Difiere un ítem hasta una fecha/hora (Status queda en Pending)",
        group_label = "Workqueues",
        icon = "workqueue.svg"
)
public class DeferWorkItem {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Execute
    public void execute(
            @Idx(index = "1", type = AttributeType.FILE)
            @NotEmpty
            @Pkg(label = "Path to file Access") String filePath,

            @Idx(index = "2", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "2.1", pkg = @Pkg(label = "By Id", value = "id")),
                    @Idx.Option(index = "2.2", pkg = @Pkg(label = "By Key", value = "key"))
            })
            @Pkg(label = "Select item by", default_value = "id", default_value_type = DataType.STRING)
            @NotEmpty
            String identifyBy,

            @Idx(index = "2.1.1", type = AttributeType.NUMBER)
            @Pkg(label = "Item Id (workqueue.Id)")
            @NotEmpty
            Double itemId,

            @Idx(index = "2.2.1", type = AttributeType.TEXT)
            @Pkg(label = "Item Key (workqueue.Key)")
            @NotEmpty
            String itemKey,

            @Idx(index = "3", type = AttributeType.SELECT, options = {
                    @Idx.Option(index = "3.1", pkg = @Pkg(label = "By date", value = "date")),
                    @Idx.Option(index = "3.2", pkg = @Pkg(label = "By offset", value = "offset"))
            })
            @Pkg(label = "Defer item by", default_value = "date", default_value_type = DataType.STRING)
            @NotEmpty
            String selectBy,

            @Idx(index = "3.1.1", type = AttributeType.DATETIME)
            @Pkg(label = "Defer Until (ISO-8601 o yyyy-MM-dd HH:mm:ss)")
            @NotEmpty
            String deferUntil,

            @Idx(index = "3.2.1", type = AttributeType.NUMBER)
            @Pkg(label = "Offset in minutes")
            @NotEmpty
            Double deferOffset
    ) {
        try {
            AccessExecutor.executeVoidWithConnection(filePath, conn -> {
                WorkqueueItemDao dao = new WorkqueueItemDao(conn);

                Timestamp ts;
                if ("offset".equalsIgnoreCase(selectBy)) {
                    if (deferOffset == null || deferOffset <= 0) {
                        throw new BotCommandException("El offset debe ser un número mayor a 0.");
                    }
                    ts = Timestamp.from(Instant.now().plusSeconds((long) (deferOffset * 60)));
                } else {
                    if (deferUntil == null || deferUntil.isBlank()) {
                        throw new BotCommandException("Debe indicar la fecha/hora destino.");
                    }
                    ts = parseToTimestamp(deferUntil);
                }

                if ("id".equalsIgnoreCase(identifyBy)) {
                    if (itemId == null) throw new BotCommandException("Debe indicar el Item Id.");
                    dao.deferFromWorkingById(itemId.longValue(), ts);
                } else if ("key".equalsIgnoreCase(identifyBy)) {
                    if (itemKey == null || itemKey.isBlank())
                        throw new BotCommandException("Debe indicar el Item Key.");
                    dao.deferFromWorkingByKey(itemKey, ts);
                } else {
                    throw new BotCommandException("Valor inválido en 'Identificar ítem por': " + identifyBy);
                }
            });
        } catch (Exception e) {
            throw new BotCommandException("Defer Work Item: " + e.getMessage(), e);
        }
    }

    private static Timestamp parseToTimestamp(String input) {
        try {
            return Timestamp.from(OffsetDateTime.parse(input).toInstant()); // ISO-8601
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception ex) {
                throw new BotCommandException("Formato de fecha no válido: " + input);
            }
        }
    }
}
