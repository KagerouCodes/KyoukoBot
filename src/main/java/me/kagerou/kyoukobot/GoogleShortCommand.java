package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
// performs a Google search (1 result with a preview)
public class GoogleShortCommand extends GoogleSearcherCommand implements CommandExecutor {

	GoogleShortCommand(GoogleSearcher googleSearcher, int LinksLimit) {
		super(googleSearcher, LinksLimit, true, false);
	}
	
	@Command(aliases = {"k!ggl"}, description = "Performs a Google search (1 result, with a preview).", usage = "k!ggl query")
    public String onCommand(Message message) {
		message.getReceiver().type();
		return search(message);
	}
}
