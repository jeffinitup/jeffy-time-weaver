package dev.jefftastic;

import api.world.data.DataEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandRealTime extends CommandBase {
    @Override
    public String getCommandName() {
        return "realtime";
    }

    @Override
    public List<?> getCommandAliases() {
        return Collections.singletonList("rt");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Cheat/op only
    }

    @Override
    public String getCommandUsage(ICommandSender _s) {
        return "commands.realtime.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        if (strings.length == 0) {
            throw new WrongUsageException("commands.realtime.usage");
        }

        String stringValue = strings[0].toLowerCase(Locale.ROOT);
        if (!stringValue.equals("on") && !stringValue.equals("off")) {
            throw new WrongUsageException("commands.realtime.usage");
        }

        boolean value = stringValue.equals("on");
        setRealTime(value);
        notifyAdmins(sender, I18n.getStringParams("commands.realtime.set", stringValue));

        if (value) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("commands.realtime.warn").setColor(EnumChatFormatting.GOLD));
        }
    }

    private void setRealTime(boolean value) {
        for(int var3 = 0; var3 < MinecraftServer.getServer().worldServers.length; ++var3) {
            MinecraftServer.getServer().worldServers[var3].setData(JeffyTimeWeaverAddon.REAL_TIME, value);
        }
    }
}
