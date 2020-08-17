package main.java.me.kagerou.kyoukobot;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//stores info about a single project
class SongProject implements Comparable<SongProject>
{
    //name � the project name to display (with ** for bold text etc.)
    //name_text � the actual name without the tags
    String name, name_text, progress, date, address, organisers, thread_link, video_link, lyrics_link;
    boolean old; //whether the project is closed for submissions
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH); //TODO fill this one
    static final Map<String, String> easterEggs;
    static {
        HashMap<String, String> tmpEasterEggs = new HashMap<String, String>();
        tmpEasterEggs.put("**chase**", "<https://www.youtube.com/watch?v=5mtZ2UnQTFw>");
        
        easterEggs = Collections.unmodifiableMap(tmpEasterEggs);
    }
    // parses the info from a single line in the table from the wiki (https://www.reddit.com/r/anime/wiki/sings/)
    // uses preloaded links to lyrics from jsonLyrics
    SongProject(Element el, boolean old, JSONObject jsonLyrics)
    {
        Elements tds = el.getElementsByTag("td");
        this.old = old;
        name_text = tds.get(1).text();
        //replace <strong>, <b> and <i> tags with Discord formatting, ignore the other tags
        String name_html = tds.get(1).html();
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
            //parse the video link: links to youtube.com gets highest priority, then it's youtu.be and vimeo.com
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
        //loads the lyrics link from jsonLyrics if possible
        try {
            lyrics_link = jsonLyrics.getString(name_text);
            return;
        }
        catch (Exception e)
        { //otherwise, trying to find a link to pastebin or animelyrics at the project page 
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
                e.printStackTrace();
                return;
            }
            try { //write the link to jsonLyrics and save them to file
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
    //converts the due date to a Date object (or returns "infinity" if the parse failed)
    Date toDate()
    {
        try {
            return dateFormat.parse(date.replaceAll("(?<=\\d)(st|nd|rd|th)", ""));
        }
        catch (Exception e)
        {
            return new Date(Long.MAX_VALUE); //"infinite" date
        }
    }
    //comparing projects by them being old/new and due date
    @Override
    public int compareTo(SongProject proj)
    {
        if (old != proj.old)
            return Boolean.compare(proj.old, old); //new projects should go before new ones
        int comp_dates = toDate().compareTo(proj.toDate());
        if (comp_dates != 0)
            return comp_dates;
        return date.compareTo(proj.date);
    }
    //a user-friendly representation of the info
    @Override
    public String toString()
    {
        for (Map.Entry<String, String> easterEgg: easterEggs.entrySet()) {
            if (name.toLowerCase().contains(easterEgg.getKey()) && !progress.equalsIgnoreCase("Completed")) {
                return "`Project:` " + name +  "\n" + easterEgg.getValue();
            }
        }
        
        if (old)
            if (progress.equalsIgnoreCase("Completed"))
                return "`Project:` " + name + "\n`Progress:` " + progress + (!lyrics_link.isEmpty() ? "\n`Lyrics:` <" + lyrics_link : "\n`Announcement:` <" + thread_link) + ">\n`Organiser(s)`: " + organisers + "\n" + video_link;
            else
                return "`Project:` " + name + "\n`Due date:` " + date + "\n`Progress:` " + progress + (!lyrics_link.isEmpty() ? "\n`Lyrics:` <" : "\n`Announcement:` <")  + thread_link + ">\n`Organiser(s)`: " + organisers/* + thread_link*/;
        else
            return "`Project:` " + name + "\n`Thread:` <" + thread_link + (!lyrics_link.isEmpty() ? (">\n`Lyrics:` <" + lyrics_link) : "") + ">\n" + "`Due date:` " + date + "\n`Organiser(s)`: " + organisers + " (" + KyoukoBot.wrapLinks(address) + ")"; 
    }
}