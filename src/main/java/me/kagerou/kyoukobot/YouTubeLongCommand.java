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
		/*if (args.length == 0)
    		return "`Enter a query.`";
		String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		String result = "";
		try {
			String APIquery = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=" + LinksLimit +
					"&order=rating&q=" + URLEncoder.encode(query, "UTF-8") + "&key=" + GoogleAPIKey;
			JSONObject json = new JSONObject(IOUtils.toString(new URL(APIquery), Charset.forName("UTF-8")));
			JSONArray array = json.getJSONArray("items");
			for (int i = 0; i < array.length(); i++)
			{
				result += "`\"" + array.getJSONObject(i).getJSONObject("snippet").getString("title") + "\" by " +
						array.getJSONObject(i).getJSONObject("snippet").getString("channelTitle") +
						"`\n<https://www.youtube.com/watch?v=" + array.getJSONObject(i).getJSONObject("id").getString("videoId") + ">\n\n";
			}
			if (result.isEmpty())
				result = "`No results found >_<`";
		}
		catch (Exception e)
		{
			return "`Failed to perform a search >_<`";
		}
		return result;
	}*/
}
