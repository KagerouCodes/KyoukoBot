package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

// performs a Google image search, returns the first result, caches results in a file for CacheDuration ms (3 days)
public class ImageCommand implements CommandExecutor
{
	// TODOKETE move it somewhere else!
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36"; //put this one in KyoukoBot's parameters??
	final static int LinksLimit = 10;
	
	GoogleSearcher googleSearcher;
	
	ImageCommand(GoogleSearcher googleSearcher)
	{
		this.googleSearcher = googleSearcher;
	}
	
	@Command(aliases = {"k!img", "k!image"}, description = "Performs a Google image search.", usage = "k!img query")
    public void onCommand(Message message, String[] args) {
		message.getReceiver().type();

		if (googleSearcher == null) {
			message.reply("`Google API secrets are missing >_<`");
			return;
		}

		String query = KyoukoBot.getArgument(message);
		if (query.isEmpty())
    	{
    		message.reply("`Enter a query.`");
    		return;
    	}
		boolean cached = KyoukoBot.SearchResults.containsKey(query) && // check if the query is in the cache
			(KyoukoBot.SearchResults.get(query).time > System.currentTimeMillis() - KyoukoBot.CacheDuration); // and it's not too old
		// if the result is cached, just use the cached URL
		if (cached && !KyoukoBot.SearchResults.get(query).url.isEmpty() &&
				KyoukoBot.postOnlyFile(message, KyoukoBot.SearchResults.get(query).url, query, "image")) {
			return;
		}

		ArrayList<String> imageURLs = googleSearcher.imageSearch(query, LinksLimit, true);
		if (imageURLs == null) {
			message.reply("`Failed to perform a search >_<`");
		}

		for (String imageURL : imageURLs) {
			if (KyoukoBot.postOnlyFile(message, imageURL, query, "image")) {
				// in case of success, cache the result
				KyoukoBot.SearchResults.put(query, new ImageSearchResult(imageURL, System.currentTimeMillis()));
				KyoukoBot.SaveSearchResults(KyoukoBot.SearchResultsFile);
				return;
			} else {
				System.out.println("Failed to post an image: " + imageURL);
			}
		}
		
		message.reply("`No images found >_<`");
	}
}
