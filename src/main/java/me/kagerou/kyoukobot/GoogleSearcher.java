package me.kagerou.kyoukobot;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.btobastian.javacord.entities.message.Message;

public class GoogleSearcher {

	static String GoogleKey;
	static String GoogleCX;
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";
	
	class SearchResult
	{
		String title, url;
		SearchResult(String title, String url)
		{
			this.title = title;
			this.url = url;
		}
	}
	
	final int LinksLimit;
	final boolean Preview;
	final boolean safe;
	
	GoogleSearcher(int LinksLimit, boolean Preview, boolean safe)
	{
		this.LinksLimit = LinksLimit;
		this.Preview = Preview;
		this.safe = safe;
	}
	
	GoogleSearcher(int LinksLimit, boolean Preview)
	{
		this(LinksLimit, Preview, true);
	}
	
	ArrayList<SearchResult> searchWithAPI(String query, String GoogleKey, String GoogleCX) throws Exception
	{
		String searchURL = "https://www.googleapis.com/customsearch/v1?key=" + GoogleKey + "&cx=" + GoogleCX + (safe ? "&safe=high" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		JSONObject json = new JSONObject(IOUtils.toString(new URL(searchURL), Charset.forName("UTF-8")));
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		if (json.has("queries"))
		{
			JSONArray array = json.getJSONArray("items");
			for (int index = 0; (index < array.length()) && (index < LinksLimit); index++)
				result.add(new SearchResult(array.getJSONObject(index).getString("title"), array.getJSONObject(index).getString("link")));
			return result;
		}
		System.out.println(json.toString());
		throw new Exception("Reached the daily limit."); //maybe there is a suitable type of exception for this situation??
	}
	
	ArrayList<SearchResult> searchWithoutAPI(String query) throws Exception
	{
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		
		Elements links;
		int links_included = 0;
		String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr" + (safe ? "&safe=active" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		Document doc = Jsoup.connect(searchURL).userAgent("KyoukoBot").get();
		//String html = ImageCommand.FetchURL("https://www.google.com/search?gfe_rd=cr&gws_rd=cr&safe=active&q=" + URLEncoder.encode(query, "UTF-8"));
		FileUtils.writeStringToFile(new File("google_search.txt"), doc.toString(), Charset.forName("UTF-8"));
		//Document doc = Jsoup.parse(html);
		links = doc.select(".g>.r>a");
		//links = doc.select(".rc>.r>a");
		
		for (Element link : links) {
			String title = link.text();
			String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
			url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");
			//url = URLDecoder.decode(url, "UTF-8");

			if (!url.startsWith("http"))
				continue; // Ads/news/etc.
				
			result.add(new SearchResult(title, url));

			if (++links_included == LinksLimit) break;
		}
		return result;
	}
	
	ArrayList<SearchResult> searchToArray(String query)
	{
		try {
			return searchWithoutAPI(query);
		}
		catch (Exception e)
		{
			System.out.println("Access denied by Google?");
			e.printStackTrace();
		}
		try {
			return searchWithAPI(query, GoogleKey, GoogleCX);
		}
		catch (Exception e)
		{
			if (e.getMessage().equals("Reached the daily limit."))
				System.out.println("Reached the daily limit of Google Custom Search API uses.");
			else
				System.out.println("Failed to access Google Custom Search API.");
			e.printStackTrace();
		}
		return null;
	}


	String search(String query) //TODO fix Oceanic Operetta??
	{
		if (query.isEmpty())
			return "`Enter a query.`";
		String result = "";
		ArrayList<SearchResult> array = searchToArray(query);
		if (array != null)
		{
			for (SearchResult link: array)
				result += '`' + link.title + "`\n" + (Preview ? "" : '<') + link.url + (Preview ? "" : '>') + "\n\n";
			if (array.isEmpty())
				result = "`No results found >_<`";
		}
		else
			result = "`Failed to perform a search >_<`";
		return result;
	}
	
	String search(Message message)
	{
		String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		return search(query);		
	}
}
