package me.kagerou.kyoukobot;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class FetchRecCommand implements CommandExecutor {
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";
	Channel getChannelByName(String name, Server server) {
		if (server == null)
			return null;
		return Iterables.find(server.getChannels(), (x) -> x.getName().equalsIgnoreCase(name));
	}
	
	Map.Entry<Integer, String> findStrings(String str, int fromIndex, String... substr)
	{
		Map.Entry<Integer, String> result = new AbstractMap.SimpleEntry<Integer, String>(-1, "");
		for (String substring: substr)
		{
			int index = str.indexOf(substring, fromIndex);
			if ((index != -1) && ((result.getKey() == -1) || (index < result.getKey())))
				result = new AbstractMap.SimpleEntry<Integer, String>(index, substring);
		}
		return result;
	}
	
	List<String> detectLinks(String str) { //TODO learn the darn regexp
		List<String> result = new ArrayList<String>();
		int index = 0;
		while (index < str.length())
		{
			Map.Entry<Integer, String> protocolEntry = findStrings(str, index, "http://", "https://");
			int next_url_start = protocolEntry.getKey();
			String protocol = protocolEntry.getValue();

			if (next_url_start != -1)
			{
				int next_space = findStrings(str, next_url_start, " ", "\n").getKey();//str.indexOf(' ', next_url_start); //doesn't handle newlines but those don't get into intros anyway
				boolean less_than = ((next_url_start > 0) && (str.charAt(next_url_start - 1) == '<'));
				if (next_space == -1)
					next_space = str.length();
				while ("!:,.;".indexOf(str.charAt(next_space - 1)) != -1)
					next_space--;
				if (less_than)
				{
					int greater_index = str.indexOf('>', next_url_start + protocol.length() + 1); //at least one symbol between the protocol and >
					if ((greater_index >= next_url_start) && (greater_index <= next_space))
						next_space = greater_index;
				}
				result.add(str.substring(next_url_start, next_space));
				index = next_space;
			}
			else
				index = str.length();
		}
		return result;
	}
	
	String getTitle(URL url) { //totally not stole this one from stackoverflow
		/*InputStream response = null;
		Scanner scanner = null;
	    try {
		    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		    //httpCon.addRequestProperty("User-Agent", userAgent);
		    HttpURLConnection.setFollowRedirects(true);
		    httpCon.setInstanceFollowRedirects(true);
			response = httpCon.getInputStream();
	        scanner = new Scanner(response);
	        String responseBody = scanner.useDelimiter("\\A").next();
	        if (responseBody.indexOf("<title>") == -1)
	        	return null;
	        return responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        try {
	            response.close();
	            scanner.close();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return null;*/
		try {
			return Jsoup.connect(url.toString()).get().title();
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	@Command(aliases = {"k!fetchrec"}, description = "Cheesy admin-only command.", usage = "k!fetchrec", requiredPermissions = "admin", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
    {
		Channel recordings = getChannelByName("recordings", server);
		if (recordings == null)
		{
			message.reply("`#recordings channel not found.`");
			return;
		}
		MessageHistory history = null;
		try {
			history = recordings.getMessageHistory(1000000000).get(); //can't get away with a limit of 0, huh
		}
		catch (Exception e)
		{
			e.printStackTrace();
			message.reply("An error occured.");
			return;
		}
		JSONObject historyJSON = new JSONObject();
		for (Message msg: history.getMessagesSorted())
		{
 			String id = msg.getAuthor().getId(); //TODO maybe detect old message from Kyouko herself
			if (!historyJSON.has(id))
				historyJSON.put(id, new JSONArray());
			JSONArray array = historyJSON.getJSONArray(id); 
			for (MessageAttachment att: msg.getAttachments())
				if (KyoukoBot.leTika.detect(att.getFileName()).startsWith("audio"))
					array.put(new JSONObject().put("title", att.getFileName()).put("link", att.getUrl()));
			for (String link: detectLinks(msg.getContent()))
			{
				try {
					URL url = new URL(link);
					String contentType = KyoukoBot.leTika.detect(url);
					if (contentType.startsWith("audio"))
						array.put(new JSONObject().put("title", FilenameUtils.getName(link)).put("link", link));
					else
						if (contentType.startsWith("text/html"))
						{
							String title = getTitle(url);
							if (title != null)
								array.put(new JSONObject().put("title", title).put("link", link));
						}
					}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (array.length() == 0)
				historyJSON.remove(id);
		}
		try {
			message.getReceiver().sendFile(IOUtils.toInputStream(historyJSON.toString(2), "UTF-8"), "recordings.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			message.reply("An error occured while sending the file.");
		}
    }
}
