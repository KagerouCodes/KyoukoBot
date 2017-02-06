package me.kagerou.kyoukobot;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ImageCommand implements CommandExecutor { //TODO make it stable??
	//static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";
	static String FetchURL(String sURL) throws Exception
	{
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
	
	@Command(aliases = {"k!img", "k!image"}, description = "Performs a Google image search.", usage = "k!img query")
    public void onCommand(Message message, String[] args) {
		if (args.length == 0)
    	{
    		message.reply("`Enter a query.`");
    		return;
    	}
		message.getReceiver().type();
		String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		String result = "";
		boolean cached = KyoukoBot.SearchResults.containsKey(query) &&
		   		(KyoukoBot.SearchResults.get(query).time > System.currentTimeMillis() - KyoukoBot.CacheDuration);
		
		if (cached && !KyoukoBot.SearchResults.get(query).url.isEmpty() && KyoukoBot.postOnlyFile(message, KyoukoBot.SearchResults.get(query).url, query, "image"))
			return;
			
		String contents = "";
		try {
			String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr&safe=active&q=" + URLEncoder.encode(query, "UTF-8") + "&tbm=isch";
			//String result = Jsoup.connect(searchURL).userAgent(userAgent).get().toString();
			contents = FetchURL(searchURL);
			//FileUtils.writeStringToFile(new File("desu.txt"), contents, Charset.forName("UTF-8"));
		}
		catch (Exception e)
		{
			message.reply("`Failed to perform a search >_<`");
			return;
		}
		
		int ou_index = 0, next_index = 0;
		while ((ou_index != -1) && (next_index != -1))
		{
			ou_index = contents.indexOf("\"ou\":\"", next_index);
			if ((ou_index != -1) && (next_index = contents.indexOf("\",\"", ou_index)) != -1)
			{
				result = StringEscapeUtils.unescapeEcmaScript(contents.substring(ou_index + 6, next_index));
				if (KyoukoBot.postOnlyFile(message, result, query, "image"))
				{
					KyoukoBot.SearchResults.put(query, new SearchResult(result, System.currentTimeMillis()));
					KyoukoBot.SaveSearchResults(KyoukoBot.SearchResultsFile);
					return;
				}
			}
		}
		message.reply("`No images found >_<`");
	}
}
