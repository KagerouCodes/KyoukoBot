package me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ChangeLogCommand implements CommandExecutor {
    @Command(aliases = {"k!changelog"}, description = "Displays the latest changes made to me.")
    public String onCommand(String command, String[] args) {
        if (KyoukoBot.ChangeLog.isEmpty())
        	return "`Failed to load the changelog.`";
        return "```xml\n" + KyoukoBot.ChangeLog + "```";
    }
}
