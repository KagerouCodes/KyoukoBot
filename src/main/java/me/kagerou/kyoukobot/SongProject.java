package me.kagerou.kyoukobot;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class SongProject
{
	String name, name_text, progress, date, address, organisers, thread_link, video_link, lyrics_link;
	boolean old;
	SongProject(Element el, boolean old, JSONObject jsonLyrics)
	{
		Elements tds = el.getElementsByTag("td");
		this.old = old;
		String name_html = tds.get(1).html();
		/*name = tds.get(1).html();
		name = name.replaceAll("<strong>", "**");
		name = name.replaceAll("</strong>", "**");
		name = name.replaceAll("<b>", "**");
		name = name.replaceAll("</b>", "**");
		name = name.replaceAll("<i>", "*");
		name = name.replaceAll("</i>", "*");*/
		name_text = tds.get(1).text();
		name_html = tds.get(1).html();
		name_html = name_html.replaceAll("<strong>", "**");
		name_html = name_html.replaceAll("</strong>", "**");
		name_html = name_html.replaceAll("<b>", "**");
		name_html = name_html.replaceAll("</b>", "**");
		name_html = name_html.replaceAll("<i>", "*");
		name_html = name_html.replaceAll("</i>", "*");
		name = tds.get(1).html(name_html).text();
		date = tds.get(2).text();
		thread_link = tds.get(2).getElementsByTag("a").get(0).attr("href");
		if (old)
		{
			progress = tds.get(3).text();
			video_link = "";
			for (Element links: tds.get(4).getElementsByTag("a"))
				if (links.attr("abs:href").contains("youtube.com"))
				{
					video_link = links.attr("abs:href");
					break;
				}
			if (video_link.isEmpty())
				for (Element links: tds.get(4).getElementsByTag("a"))
					if (links.attr("abs:href").contains("youtu.be"))
					{
						video_link = links.attr("abs:href");
						break;
					}
			if (video_link.isEmpty())
				for (Element links: tds.get(4).getElementsByTag("a"))
					if (links.attr("abs:href").contains("vimeo.com"))
					{
						video_link = links.attr("abs:href");
						break;
					}
			organisers = tds.get(5).text();
			address = "";
		}
		else
		{
			address = tds.get(3).text();
			organisers = tds.get(4).text();
			progress = "";
			video_link = "";
		}
		
		try {
			lyrics_link = jsonLyrics.getString(name_text);
			return;
		}
		catch (Exception e)
		{
			lyrics_link = "";
			try { // this is very resource-intensive, i'm getting my socket closed??
				Document doc = Jsoup.connect(thread_link).userAgent("KyoukoBot").get();
				for (Element links: doc.getElementsByTag("a"))
					if (links.text().toLowerCase().contains("pastebin") || links.text().toLowerCase().contains("animelyrics"))
					{
						lyrics_link = links.attr("abs:href");
						break;
					}
			}
			catch (Exception exc)
			{
				System.out.println("Failed to access reddit thread: " + thread_link);
				return;
				//e.printStackTrace();
			}
			try {
				jsonLyrics.put(name_text, lyrics_link);
				jsonLyrics.toString();
				FileUtils.writeStringToFile(new File(KyoukoBot.LyricsDatabaseFile), jsonLyrics.toString(), Charset.forName("UTF-8"));
				System.out.println("Updated the lyrics database file with " + name_text + "!");
			}
			catch (Exception exc)
			{
				System.out.println("Failed to update the lyrics database file");
			}
		}
	}
	@Override
	public String toString()
	{
		if (name.toLowerCase().contains("database") && name.toLowerCase().contains("log horizon"))
			return "`Project:` " + name +  "\n<https://www.youtube.com/watch?v=oHg5SJYRHA0>";
		if (old)
			if (progress.equalsIgnoreCase("Completed"))
				return "`Project:` " + name + "\n`Progress:` " + progress + (!lyrics_link.isEmpty() ? "\n`Lyrics:` <" + lyrics_link : "\n`Announcement:` <" + thread_link) + ">\n`Organiser(s)`: " + organisers + "\n" + video_link;
			else
				return "`Project:` " + name + "\n`Due date:` " + date + "\n`Progress:` " + progress + (!lyrics_link.isEmpty() ? "\n`Lyrics:` <" : "\n`Announcement:` <")  + thread_link + ">\n`Organiser(s)`: " + organisers/* + thread_link*/;
		else
			return "`Project:` " + name + "\n`Thread:` <" + thread_link + (!lyrics_link.isEmpty() ? (">\n`Lyrics:` <" + lyrics_link) : "") + ">\n" + "`Due date:` " + date + "\n`Organiser(s)`: " + organisers + " (" + KyoukoBot.wrapLinks(address) + ")"; 
	}
}