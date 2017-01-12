package me.kagerou.kyoukobot;

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

public class AnimeLyricsCommand extends GoogleSearcher implements CommandExecutor {

	AnimeLyricsCommand() {
		super(10, false, false);
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
	
	private static int min(int a, int b) {
		return (a < b) ? a : b;
	}
	
	private int indexOf(String string, String substr, int from_index) {
		int result = string.indexOf(substr, from_index);
		if (result != -1)
			return result;
		return string.length();
	}
	
	private int skipISOControl(String string, int index) {
		int result;
		for (result = index; ((result < string.length()) && (string.charAt(result) <= ' ')); result++);
		return result;
	}
	
	private int lastValidIndex(String string, int index, String substr, int charlimit) {
		int new_index = index, forward_index = new_index;
		while (forward_index < min(string.length(), index + KyoukoBot.CharLimit))
		{
			new_index = forward_index;
			forward_index = skipISOControl(string, new_index);
			//for (new_index = forward_index; (forward_index < string.length()) && Character.isISOControl(string.charAt(forward_index)); forward_index++);
			//yes, new_index = forward_index, not the other way around
			forward_index = indexOf(string, substr, forward_index);
		}
		if (forward_index <= index + KyoukoBot.CharLimit)
			new_index = forward_index;
		return new_index;
	}

	@Command(aliases = {"k!lyrics", "k!alyrics", "k!animelyrics"}, description = "Searches for lyrics at animelyrics.com.", usage = "k!lyrics song")
    public void onCommand(Message message, String[] args) {
		String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		String url = "";
		//return search(query + " site:animelyrics.com");
		ArrayList<SearchResult> array;
		int link_index = 0;
		try {
			array = searchToArray(query + " site:animelyrics.com");
			while ((link_index < array.size()) && !array.get(link_index).url.endsWith("htm") && !array.get(link_index).url.endsWith("html"))
				link_index++;
			//if (array.isEmpty())
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
		{
			String result = '`' + title + "`\n" +
		((url.endsWith(".htm") || url.endsWith(".html")) ? "`(to get the full lyrics, send this command in private chat)`\n" : "") +
			'<' + url + '>';
			message.reply(result);
			return;
		}
		try {
			//FileUtils.copyURLToFile(new URL(array.get(0).url), new File("degeso.txt"));
			String html = IOUtils.toString(new URL(url), "UTF-8"); 
			Document doc = Jsoup.parse(html);
			//Document doc = Jsoup.connect(array.get(0).url).get();
			
			//FileUtils.writeStringToFile(new File("desu.txt"), doc.html(), "UTF-8");
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
				//for (Element dt: elem.getElementsByTag("dt"))
				//		dt.remove();
				elem.getElementsByTag("br").append("\\n");
				//for (Element br: elem.getElementsByTag("br"))
				//	br.appendText("\\n");
				result += elem.text().replaceAll("\\\\n", "\n");
			}
			if (result.isEmpty())
			{
				//TODO file an issue at jsoup's github
				//Indian code because JSoup's buggy
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
			if (result.isEmpty())
				throw new NullPointerException();
			result = '`' + title + "`\n<" + url + ">\n```xml\n" + result.trim()/* + "```"*/;
			if (result.length() <= KyoukoBot.CharLimit)
			{
				message.reply(result  + "```");
				return;
			}
			int index = 0;
			while (index != result.length())
			{
				//int new_index = indexOf(result, "\n\n", index), forward_index = new_index;
				int new_index = lastValidIndex(result, index, "\n \n", KyoukoBot.CharLimit); //India intensifies
				
				/*int new_index = index, forward_index = new_index;
				while (forward_index < min(result.length(), index + KyoukoBot.CharLimit))
				{
					for (new_index = forward_index; (forward_index < result.length()) && Character.isISOControl(result.charAt(forward_index)); forward_index++);
					//yes, new_index = forward_index, not the other way around
					forward_index = indexOf(result, "\n\n", forward_index);
				}*/
				if (new_index == index)
					new_index = lastValidIndex(result, index, "\n", KyoukoBot.CharLimit);
				if (new_index == index)
					new_index = lastValidIndex(result, index, " ", KyoukoBot.CharLimit);
				if (new_index == index)
					new_index = min(index + KyoukoBot.CharLimit, result.length());
				message.reply(((index != 0) ? "```xml\n" : "") + result.substring(index, new_index) + "```");
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
