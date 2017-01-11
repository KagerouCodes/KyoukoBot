package me.kagerou.kyoukobot;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

@Deprecated
public class MoxianCommand implements CommandExecutor {
	static String GoogleKey = "AIzaSyCICMglvqw7GuKPdzNP1bkdQM1iGvnoqXA";
	static String GoogleCX = "007613658332680872345:6mfmvokqqvc";

	@Command(aliases = {"k!moxian"}, description = "Performs a Google image search as Moxian adviced.", usage = "k!moxian query", showInHelpPage = false)
    public void onCommand(Message message, String[] args) {
    	if (args.length == 0)
    	{
    		message.reply("`Enter a query.`");
    		return;
    	}
    	String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
    	String result = "";
    	boolean cached = KyoukoBot.SearchResults.containsKey(query) &&
    		(KyoukoBot.SearchResults.get(query).time > System.currentTimeMillis() - KyoukoBot.CacheDuration); //milliseconds in a week
    	if (!cached)
    	{
    		JSONObject json;
    		try {    			
    			String searchURL = "https://www.googleapis.com/customsearch/v1?key=" + GoogleKey + "&cx=" + GoogleCX + "&q=" + URLEncoder.encode(query, "UTF-8") + "&searchType=image";
    			json = new JSONObject(IOUtils.toString(new URL(searchURL), Charset.forName("UTF-8")));
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			message.reply("`Failed to perform a search >_<`");
    			return;
    		}
    		try {
    			result = json.getJSONArray("items").getJSONObject(0).getString("link");
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			result = "";
    		}
    		KyoukoBot.SearchResults.put(query, new SearchResult(result, System.currentTimeMillis()));
    		KyoukoBot.SaveSearchResults(KyoukoBot.SearchResultsFile);
    	}
    	else
    		result = KyoukoBot.SearchResults.get(query).url;
    	if (!result.isEmpty())
    		KyoukoBot.postFile(message, result, query, "image");
    	else
    		message.reply("`No images found >_<`");
	}
}
