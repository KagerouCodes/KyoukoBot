package me.kagerou.kyoukobot;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.btobastian.javacord.entities.message.Message;
//a super class for all Google non-image search commands
public class GoogleSearcher {

	static String GoogleKey;
	static String GoogleCX;
	//should probably put the user agent in KyoukoBot's final static fields
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36"; 
	//that feel when there's no pair "template" in Java
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
	//safe search by default
	GoogleSearcher(int LinksLimit, boolean Preview)
	{
		this(LinksLimit, Preview, true);
	}
	//search using the Google Custom Search API (a fallback plan), return a list of SearchResult's
	ArrayList<SearchResult> searchWithAPI(String query, String GoogleKey, String GoogleCX) throws Exception
	{
		String searchURL = "https://www.googleapis.com/customsearch/v1?key=" + GoogleKey + "&cx=" + GoogleCX + (safe ? "&safe=high" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		JSONObject json = new JSONObject(IOUtils.toString(new URL(searchURL), Charset.forName("UTF-8")));
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		if (json.has("queries")) //if none, the search wasn't performed, very likely the daily limit was reached
		{
			if (json.has("items"))
			{
				JSONArray array = json.getJSONArray("items");
				for (int index = 0; (index < array.length()) && (index < LinksLimit); index++)
					result.add(new SearchResult(array.getJSONObject(index).getString("title"), array.getJSONObject(index).getString("link")));
			}
			return result;
		}
		System.out.println(json.toString());
		throw new Exception("Reached the daily limit."); //maybe there is a suitable type of exception for this situation??
	}
	//just open the search page and parse it manually, works until Google limits you
	ArrayList<SearchResult> searchWithoutAPI(String query) throws Exception
	{ //i just took the code from stackoverflow
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		
		Elements links;
		int links_included = 0;
		String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr" + (safe ? "&safe=active" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		Document doc = Jsoup.connect(searchURL).userAgent("KyoukoBot").get();
		//FileUtils.writeStringToFile(new File("google_search.txt"), doc.toString(), Charset.forName("UTF-8")); //for debug purposes
		links = doc.select(".g>.r>a");
		//links = doc.select(".rc>.r>a");
		
		for (Element link : links) {
			String title = link.text();
			String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
			url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

			if (!url.startsWith("http"))
				continue; // Ads/news/etc.
				
			result.add(new SearchResult(title, url));

			if (++links_included == LinksLimit) break;
		}
		return result;
	}
	//search manually, if that fails, use the API
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
		return null; //return null if everything fails
	}

	//returns a ready-to-post string with search results
	String search(String query) //TODO fix Oceanic Operetta?? (if the link ends with a closing bracket, Discord doesn't include the bracket in the link for some reason)
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
		//String query = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim().toLowerCase();
		return search(KyoukoBot.getArgument(message));		
	}
}
