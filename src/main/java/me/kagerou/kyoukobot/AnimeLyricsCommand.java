package main.java.me.kagerou.kyoukobot;

import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
// searches for lyrics at animelyrics.com
// TODOKETE search for Touhou and Vocaloid lyrics too!
public class AnimeLyricsCommand implements CommandExecutor {
	final static int LinksLimit = 10;

	GoogleSearcher googleSearcher;

	AnimeLyricsCommand(GoogleSearcher googleSearcher) {
		this.googleSearcher = googleSearcher;
	}
	
	String getTitle(String url, String title)
	{ 
		String result;
		try {
			result = Jsoup.connect(url).userAgent("KyoukoBot").get().title();
		}
		catch (Exception e)
		{
			result = title;
		}
		return result.replaceAll("Anime Lyrics dot Com -", "").trim();
	}
	// String.indexOf() modified to return string's length instead of -1, needed for correct comparisons
	private int indexOf(String string, String substr, int from_index) {
		int result = string.indexOf(substr, from_index);
		if (result != -1)
			return result;
		return string.length();
	}
	
	private int skipISOControl(String string, int index)
	{ // returns index of next non ISO control character in a string
		int result;
		for (result = index; ((result < string.length()) && (string.charAt(result) <= ' ')); result++);
		return result;
	}
	
	private int lastValidIndex(String string, int index, String substr, int charlimit)
	{ // finds the largest substring in a string starting at the index and ending right before substr (or end of the string) which can fit in a Discord message,
	  // returns its ending index (substr is supposed to consist of ISOControl characters, that's why skipISOControl skips it)
		int new_index = index, forward_index = new_index;
		while (forward_index < Math.min(string.length(), index + KyoukoBot.CharLimit))
		{
			new_index = forward_index;
			forward_index = indexOf(string, substr, skipISOControl(string, new_index));
		}
		if (forward_index <= index + KyoukoBot.CharLimit)
			new_index = forward_index;
		return new_index;
	}

	@Command(aliases = {"k!lyrics", "k!alyrics", "k!animelyrics"}, description = "Searches for lyrics at animelyrics.com, then links them on a server or prints them in DM.", usage = "k!lyrics song")
    public void onCommand(Message message, String[] args) {
		message.getReceiver().type();
		if (googleSearcher == null) {
			// TODOKETE this stuff needs to be a helper
			message.reply("`Google API secrets are missing >_<`");
			return;
		}

		String query = KyoukoBot.getArgument(message);
		if (query.isEmpty())
		{
			message.reply("`Enter a query.`");
			return;
		}
		String url = "";
		ArrayList<GoogleSearcher.SearchResult> array;
		int link_index = 0;
		try {
			// search for 10 results at animelyrics.com 
			array = googleSearcher.search(query + " site:animelyrics.com", LinksLimit, true);
			while ((link_index < array.size()) && !array.get(link_index).url.endsWith("htm") && !array.get(link_index).url.endsWith("html"))
				link_index++; //skip the results not ending in htm(l) since those don't contain lyrics of single songs
			if (link_index == array.size())
			{
				message.reply("`No results found >_<`");
				return;
			}
			url = array.get(link_index).url;
		}
		catch (Exception e)
		{
			message.reply("`Failed to perform a search >_<`");
			return;
		}
		String title = getTitle(url, array.get(link_index).title);
		if (!message.isPrivateMessage())
		{ //post just the link with a title if the message isn't private, no need to spam the chat
			String result = '`' + title + "`\n" +
		//((url.endsWith(".htm") || url.endsWith(".html")) ? "`(to get the full lyrics, send this command in private chat)`\n" : "") + //no need for this check, right??
			"`(to get the full lyrics, send this command in private chat)`\n" + '<' + url + '>';
			message.reply(result);
			return;
		}
		//if it's a private message, parse the lyrics and post them
		try {
			String html = IOUtils.toString(new URL(url), "UTF-8"); 
			Document doc = Jsoup.parse(html);
			// if there are romaji and a translation, lyrics are in <td class=romaji><td class=lyrics>, otherwise they are in <td class=lyrics> 
			Elements tds = doc.getElementsByClass("romaji"), lyrics = new Elements();
			if (tds.isEmpty())
				lyrics = doc.getElementsByClass("lyrics");
			else
				for (Element elem: tds)
					lyrics.addAll(elem.getElementsByClass("lyrics"));
			String result = "";
			for (Element elem: lyrics)
			{
				elem.getElementsByTag("dt").remove();
				elem.getElementsByTag("br").append("\\n");
				result += elem.text().replaceAll("\\\\n", "\n");
			}
			
			if (result.isEmpty())
			{ //JSoup is buggy and doesn't parse some spans properly, e.g. result would be empty while parsing view-source:http://www.animelyrics.com/anime/overlord/clattanoia.htm
				//TODO file an issue at jsoup's github
				//searching for <span class=lyrics> like a true Indian, probably should search for the regex <span\s+class\s*=\s*(lyrics|"lyrics")> or something like that instead
				int open_index = -1, close_index = -1;
				do
				{
					open_index = html.indexOf("<span class=lyrics>", close_index + 1);
					if (open_index != -1)
						if ((close_index = html.indexOf("</span>", open_index)) != -1)
						{
							Document span = Jsoup.parse(html.substring(open_index, close_index + 7), array.get(0).url);
							span.getElementsByTag("dt").remove();
							span.getElementsByTag("br").append("\\n");
							span.getElementsByTag("p").prepend("\\n\\n");
							result += span.text().replaceAll("\\\\n", "\n");
						}
				} while ((open_index != -1) && (close_index != -1));
			}
			if (result.isEmpty()) //still failed to parse the lyrics somehow?? Just post the link.
				throw new NullPointerException(); //pretty much goto catch (Exception e)
			result = '`' + title + "`\n<" + url + ">\n```\n" + result.trim();
			if (result.length() <= KyoukoBot.CharLimit)
			{ //just post the lyrics is they fit into a single message
				message.reply(result  + "```");
				return;
			}
			int index = 0;
			while (index != result.length())
			{ //breaking the message by different demiliters: two new lines, one new line, space, no breaking at all � in order of decreasing priority 
				int new_index = lastValidIndex(result, index, "\n \n", KyoukoBot.CharLimit); //India intensifies
				if (new_index == index)
					new_index = lastValidIndex(result, index, "\n", KyoukoBot.CharLimit);
				if (new_index == index)
					new_index = lastValidIndex(result, index, " ", KyoukoBot.CharLimit);
				if (new_index == index)
					new_index = Math.min(index + KyoukoBot.CharLimit, result.length());
				message.reply(((index != 0) ? "```\n" : "") + result.substring(index, new_index) + "```");
				index = skipISOControl(result, new_index);
				
				try {
					Thread.sleep(500); //gotta guarantee the correct order
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}			
		}
		catch (Exception e)
		{
			String result = '`' + title + "`\n" +
					((url.endsWith(".htm") || url.endsWith(".html")) ? "`(failed to parse the lyrics)`\n" : "") +
					"<" + url + ">";
			message.reply(result);
		}
	}
}
