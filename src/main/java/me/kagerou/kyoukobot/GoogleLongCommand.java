package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class GoogleLongCommand extends GoogleSearcher implements CommandExecutor {
	
	GoogleLongCommand(int LinksLimit, boolean Preview) {
		super(LinksLimit, Preview);
	}

	@Command(aliases = {"k!google"}, description = "Performs a Google search (3 results, no preview).", usage = "k!google query")
    public String onCommand(Message message) {
		return search(message);
	}
		/*if (args.length == 0)
    		return "`Enter a query.`";
		String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		String result = "";
		Elements links;
		int links_included = 0;
		try {
			links = Jsoup.connect("https://www.google.com/search?gfe_rd=cr&gws_rd=cr&q=" + URLEncoder.encode(query, "UTF-8")).userAgent("KyoukoBot").get().select(".g>.r>a");

			for (Element link : links) {
				String title = link.text();
				String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
				url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

				if (!url.startsWith("http"))
					continue; // Ads/news/etc.
				
				result += '`' + title + "`\n<" + url + ">\n\n";
				if (++links_included == LinksLimit) break;
			}
		}
		catch (Exception e)
		{
			return "`Failed to perform a search >_<`";
		}
		if (result.isEmpty())
			result = "`No results found >_<`";
		return result;
	}*/
}
