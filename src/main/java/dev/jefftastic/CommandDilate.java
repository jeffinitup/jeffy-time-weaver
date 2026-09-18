package dev.jefftastic;

import api.world.data.DataEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.util.Locale;

public class CommandDilate extends CommandBase {
    @Override
    public String getCommandName() {
        return "dilate";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Cheat/op only
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.dilate.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        if (strings.length == 0) {
            throw new WrongUsageException("commands.dilate.usage");
        }

        World world = sender.getEntityWorld();
        WorldInfo info = world.getWorldInfo();

        String toDilate = strings[0].toLowerCase(Locale.ROOT);
        DataEntry.WorldDataEntry<Double> toModify = toDilate.equals("day") ? JeffyTimeWeaverAddon.DAY_DILATION : JeffyTimeWeaverAddon.NIGHT_DILATION;
        String period = I18n.getString("commands.dilate.period.%s".formatted(toDilate));

        if (strings.length == 1) {
            // Check string
            if (!period.equals("day") && !period.equals("night")) {
                throw new WrongUsageException("commands.dilate.usage");
            }
        }

        if (strings.length == 2) {
            // Prepare new value
            try {
                int value = Integer.parseInt(strings[1]);

                // No negative numbers (or zero)
                if (value < 1) {
                    throw new WrongUsageException("commands.dilate.error.negative");
                }

                setDilation(toModify, (double) value / 100D);
                notifyAdmins(sender, I18n.getStringParams("commands.dilate.set", period, value));
            } catch (NumberFormatException _e) {
                throw new WrongUsageException("commands.dilate.usage");
            }
        } else if (strings.length == 1) {
            // Prepare display string (convert to percent)
            int amount = (int) (info.getData(toModify) * 100D);
            sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions("commands.dilate.current", period, amount));
        } else {
            throw new WrongUsageException("commands.dilate.usage");
        }
    }

    private void setDilation(DataEntry.WorldDataEntry<Double> toModify, double value) {
        for(int var3 = 0; var3 < MinecraftServer.getServer().worldServers.length; ++var3) {
            MinecraftServer.getServer().worldServers[var3].setData(toModify, value);
        }
    }
}
