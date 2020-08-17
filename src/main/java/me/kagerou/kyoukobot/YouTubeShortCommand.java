package main.java.me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//performs a YouTube search (1 result, with a preview)
public class YouTubeShortCommand extends YouTubeSearcher implements CommandExecutor {
	YouTubeShortCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview);
	}
	
	@Command(aliases = {"k!yt"}, description = "Performs a YouTube search (1 result, with a preview).", usage = "k!yt query")
    public String onCommand(Message message, String[] args) {
		message.getReceiver().type();
		return search(message, args);
	}
}
