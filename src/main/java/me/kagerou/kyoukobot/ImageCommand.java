package me.kagerou.kyoukobot;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//performs a Google image search, returns the first result, caches results in a file for CacheDuration ms (3 days) 
@SuppressWarnings("deprecation")
public class ImageCommand implements CommandExecutor
{
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36"; //put this one in KyoukoBot's parameters??
	
	static String FetchURL(String sURL) throws Exception
	{ //fetches the contents of an URL; i can't just use IOUtils.toString(URL, Charset) because without the user agent i won't get the Javascript code
	    URL url = new URL(sURL);
	    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
	    httpCon.addRequestProperty("User-Agent", userAgent);
	    //HttpURLConnection.setFollowRedirects(true);
	    httpCon.setInstanceFollowRedirects(true);
	    //httpCon.setDoOutput(true);
	    httpCon.setUseCaches(true);

	    httpCon.setRequestMethod("GET");
	    
	    return IOUtils.toString(httpCon.getInputStream(), "UTF-8");
	}
	
	//an ad-hoc way to grab the first result without using the API
	static String GrabImageURL(String googlePage)
	{
		int last_grid_state0_index = googlePage.lastIndexOf("\"GRID_STATE0\"");
		if (last_grid_state0_index == -1)
			return null;
		
		int url_index = googlePage.indexOf("[\"", last_grid_state0_index);
		if (url_index == -1)
			return null;
		url_index = googlePage.indexOf("[\"", url_index + 1);
		
		if (url_index == -1)
			return null;
		url_index += 2;
		int url_end_index = googlePage.indexOf('"', url_index);
		if (url_index == -1)
			return null;
		
		return googlePage.substring(url_index, url_end_index);
	}
	
	@Command(aliases = {"k!img", "k!image"}, description = "Performs a Google image search.", usage = "k!img query")
    public void onCommand(Message message, String[] args) {
		String query = KyoukoBot.getArgument(message);
		if (query.isEmpty())
    	{
    		message.reply("`Enter a query.`");
    		return;
    	}
		message.getReceiver().type();
		boolean cached = KyoukoBot.SearchResults.containsKey(query) && //check if the query is in the cache
		   	(KyoukoBot.SearchResults.get(query).time > System.currentTimeMillis() - KyoukoBot.CacheDuration); //and it's not too old
		//if the result is cached, just use the cached URL
		if (cached && !KyoukoBot.SearchResults.get(query).url.isEmpty() &&
				KyoukoBot.postOnlyFile(message, KyoukoBot.SearchResults.get(query).url, query, "image"))
			return;
		//if not, fetch the search page; this works without the API so far
		String contents = "";
		try {
			String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr&safe=active&q=" +
				URLEncoder.encode(query, "UTF-8") + "&tbm=isch";
			contents = FetchURL(searchURL);	
			
			String imageURL = GrabImageURL(contents);
			if (imageURL != null && KyoukoBot.postOnlyFile(message, imageURL, query, "image"))
			{
				//in case of success, cache the result
				KyoukoBot.SearchResults.put(query, new ImageSearchResult(imageURL, System.currentTimeMillis()));
				KyoukoBot.SaveSearchResults(KyoukoBot.SearchResultsFile);
				return;
			}
			// TODOKETE a fallback to Google API
		}
		catch (Exception e)
		{
			e.printStackTrace();
			message.reply("`Failed to perform a search >_<`");
			return;
		}
		
		message.reply("`No images found >_<`");
	}
}
