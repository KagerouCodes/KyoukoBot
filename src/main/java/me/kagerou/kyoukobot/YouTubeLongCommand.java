package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class YouTubeLongCommand extends YouTubeSearcher implements CommandExecutor {
	
	YouTubeLongCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview);
	}
	
	@Command(aliases = {"k!youtube", "k!utube"}, description = "Performs a YouTube search (3 results, no preview).", usage = "k!youtube query")
    public String onCommand(Message message, String[] args) {
		return search(message, args);
	}
}
