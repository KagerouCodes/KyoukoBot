package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.entities.message.Message;
import main.java.me.kagerou.kyoukobot.GoogleSearcher.SearchResult;

public class GoogleSearcherCommand {
	GoogleSearcher googleSearcher;
	int LinksLimit;
	boolean Safe;
	boolean Preview;
	
	GoogleSearcherCommand(GoogleSearcher googleSearcher, int linksLimit, boolean safe, boolean preview)
	{
		this.googleSearcher = googleSearcher;
		LinksLimit = linksLimit;
		Safe = safe;
		Preview = preview;
	}
	
	// returns a ready-to-post string with search results
	String search(String query)
	{
		if (googleSearcher == null) {
			return "`Google API secrets are missing >_<`";
		}
		
		if (query.isEmpty()) {
			return "`Enter a query.`";
		}
		
		String result = "";
		ArrayList<SearchResult> array = googleSearcher.search(query, LinksLimit, Safe);
		if (array != null)
		{
			for (SearchResult link: array) {
				result += '`' + link.title + "`\n" + (Preview ? "" : '<') + link.url + (Preview ? "" : '>') + "\n\n";
			}
			if (array.isEmpty()) {
				result = "`No results found >_<`";
			}
		} else {
			result = "`Failed to perform a search >_<`";
		}
		
		return result;
	}
	
	String search(Message message)
	{
		return search(KyoukoBot.getArgument(message));		
	}
}
