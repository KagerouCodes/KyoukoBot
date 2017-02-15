package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//performs a Google search (3 results, no preview)
public class GoogleLongCommand extends GoogleSearcher implements CommandExecutor {
	
	GoogleLongCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview); //maybe i should've just fixed these two parameters
	}

	@Command(aliases = {"k!google"}, description = "Performs a Google search (3 results, no preview).", usage = "k!google query")
    public String onCommand(Message message) {
		message.getReceiver().type();
		return search(message);
	}
}
