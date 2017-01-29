package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class GoogleShortCommand extends GoogleSearcher implements CommandExecutor {

	GoogleShortCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview);
	}
	
	@Command(aliases = {"k!ggl"}, description = "Performs a Google search (1 result, with a preview).", usage = "k!ggl query")
    public String onCommand(Message message) {
		return search(message);
	}
}
