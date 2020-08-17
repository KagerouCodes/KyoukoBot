package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class MemoryCommand implements CommandExecutor {
	@Command(aliases = {"k!memory"}, description = "Cheesy admin-only command.", requiredPermissions = "admin", showInHelpPage = false)
    public String onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		double mem_mbs = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0;
		mem_mbs = Math.round(mem_mbs * 1000.0) / 1000.0;
		return "`Current memory usage: " + mem_mbs + " MB`";
    }
}
