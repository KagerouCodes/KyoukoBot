package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//performs a Google search (1 results with a preview)
public class GoogleShortCommand extends GoogleSearcher implements CommandExecutor {

	GoogleShortCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview); //maybe i should've just fixed these two parameters
	}
	
	@Command(aliases = {"k!ggl"}, description = "Performs a Google search (1 result, with a preview).", usage = "k!ggl query")
    public String onCommand(Message message) {
		message.getReceiver().type();
		return search(message);
	}
}
