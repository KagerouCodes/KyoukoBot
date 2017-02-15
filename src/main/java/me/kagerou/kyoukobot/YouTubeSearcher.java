package me.kagerou.kyoukobot;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.btobastian.javacord.entities.message.Message;

public class YouTubeSearcher {
	final int LinksLimit;
	final boolean Preview;
	static String GoogleAPIKey;
	
	YouTubeSearcher(int LinksLimit, boolean Preview)
	{
		this.LinksLimit = LinksLimit;
		this.Preview = Preview;
	}
	
	String search(Message message, String args[])
	{
		//if (args.length == 0)
    		//return "`Enter a query.`";
		//String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		String query = KyoukoBot.getArgument(message);
		if (query.isEmpty())
			return "`Enter a query.`";
		String result = "";
		try {
			String APIquery = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=" + LinksLimit +
					"&q=" + URLEncoder.encode(query, "UTF-8") + "&key=" + GoogleAPIKey;
			JSONObject json = new JSONObject(IOUtils.toString(new URL(APIquery), Charset.forName("UTF-8")));
			JSONArray array = json.getJSONArray("items");
			for (int i = 0; i < array.length(); i++)
			{
				result += "`\"" + array.getJSONObject(i).getJSONObject("snippet").getString("title") + "\" by " +
						array.getJSONObject(i).getJSONObject("snippet").getString("channelTitle") + "`\n" + 
						(Preview ? "" : '<') + "https://www.youtube.com/watch?v=" + array.getJSONObject(i).getJSONObject("id").getString("videoId") +
						(Preview ? "" : '>') + "\n\n";
			}
			if (result.isEmpty())
				result = "`No results found >_<`";
		}
		catch (Exception e)
		{
			return "`Failed to perform a search >_<`";
		}
		return result;
	}
}
