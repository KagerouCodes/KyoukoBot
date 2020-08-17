package main.java.me.kagerou.kyoukobot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleSearcher {
	String GoogleKey;
	String GoogleCX;
	// TODOKETE should probably put the user agent in KyoukoBot's final static fields
	static String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0"; 

	class SearchResult
	{
		String title, url;
		SearchResult(String title, String url)
		{
			this.title = title;
			this.url = url;
		}
	}
	
	GoogleSearcher(String googleKey, String googleCX)
	{
		GoogleKey = googleKey;
		GoogleCX = googleCX;
	}

	//search using the Google Custom Search API (a fallback plan), return a list of SearchResult's
	ArrayList<SearchResult> searchWithAPI(String query, int linksLimit, boolean safe) throws Exception
	{
		String searchURL = "https://www.googleapis.com/customsearch/v1?key=" + GoogleKey + "&cx=" + GoogleCX + (safe ? "&safe=high" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		JSONObject json = new JSONObject(IOUtils.toString(new URL(searchURL), Charset.forName("UTF-8")));
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		if (json.has("queries")) // if none, the search wasn't performed, very likely the daily limit was reached
		{
			if (json.has("items"))
			{
				JSONArray array = json.getJSONArray("items");
				for (int index = 0; (index < array.length()) && (index < linksLimit); index++)
					result.add(new SearchResult(array.getJSONObject(index).getString("title"), array.getJSONObject(index).getString("link")));
			}
			return result;
		}
		System.out.println(json.toString());
		throw new Exception("Reached the daily limit."); // TODOKETE maybe there is a suitable type of exception for this situation??
	}

	// just open the search page and parse it manually, works until Google limits you
	ArrayList<SearchResult> searchWithoutAPI(String query, int linksLimit, boolean safe) throws Exception
	{
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		
		Elements links;
		String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr" + (safe ? "&safe=active" : "") + "&q=" + URLEncoder.encode(query, "UTF-8");
		Document doc = Jsoup.connect(searchURL).userAgent(userAgent).get();
		links = doc.select(".rc>.r>a");
		
		for (Element link : links) {
			if (result.size() >= linksLimit) {
				break;
			}

			Element titleElement = link.selectFirst("h3");
			if (titleElement == null) {
				continue;
			}
			String title = titleElement.text();
			String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
				
			result.add(new SearchResult(title, url));
		}
		return result;
	}

	// search using StartPage, similar to the previous function
	ArrayList<SearchResult> searchWithStartPage(String query, int linksLimit, boolean safe) throws Exception
	{
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		String searchURL = "https://www.startpage.com/do/asearch?language=english&ff=" + (safe ? "on" : "off") + "&q=" + URLEncoder.encode(query, "UTF-8");
		Document doc = Jsoup.connect(searchURL).userAgent(userAgent).get();
		Elements links = doc.select(".search-item__title>a");
		if (links.isEmpty()) {
			links = doc.select("a.w-gl__result-url");
		}
		for (Element link: links)
		{
			if (result.size() >= linksLimit) {
				break;
			}
			result.add(new SearchResult(link.text(), link.attr("href")));
		}
		return result;
	}

	// search manually, if that fails, use StartPage; in case of second failure, try the API
	ArrayList<SearchResult> search(String query, int linksLimit, boolean safe)
	{
		try {
			return searchWithoutAPI(query, linksLimit, safe);
		}
		catch (Exception e)
		{
			System.out.println("Access denied by Google?");
			e.printStackTrace();
		}
		try {
			return searchWithStartPage(query, linksLimit, safe);
		}
		catch (Exception e)
		{
			System.out.println("Failed to search using StartPage >_<");
			e.printStackTrace();
		}
		try {
			return searchWithAPI(query, linksLimit, safe);
		}
		catch (Exception e)
		{
			// TODOKETE make your own exception for this, idiot!
			if (e.getMessage().equals("Reached the daily limit.")) {
				System.out.println("Reached the daily limit of Google Custom Search API uses.");
			} else {
				System.out.println("Failed to access Google Custom Search API.");
			}
			e.printStackTrace();
		}
		return null; // return null if everything fails
	}

	String fetchURL(String sURL) throws Exception
	{ //fetches the contents of an URL; might just want to use IOUtils.toString(URL, Charset)
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

	// an ad-hoc way to parse the Google Image Search page
	ArrayList<String> imageSearchWithoutAPI(String query, int linksLimit, boolean safe) throws Exception
	{
		ArrayList<String> result = new ArrayList<String>();

		String searchURL = "https://www.google.com/search?gfe_rd=cr&gws_rd=cr" + (safe ? "&safe=active" : "") + "&q=" + URLEncoder.encode(query, "UTF-8") + "&tbm=isch";
		String contents = fetchURL(searchURL);

		int urlIndex = contents.lastIndexOf("\"GRID_STATE0\"");
		if (urlIndex == -1) {
			throw new Exception("GRID_STATE0 not found");
		}

		// TODOKETE use regex, Luke!
		while (result.size() < linksLimit) {
			urlIndex = contents.indexOf("[1,[0,\"", urlIndex);
			if (urlIndex == -1) {
				break;
			}

			urlIndex = contents.indexOf("[\"", urlIndex);
			if (urlIndex == -1) {
				break;
			}

			urlIndex = contents.indexOf("[\"", urlIndex + 1);
			if (urlIndex == -1) {
				break;
			}
			urlIndex += 2;

			int urlEndIndex = contents.indexOf('"', urlIndex);
			if (urlEndIndex == -1) {
				break;
			}

			String imageURL = contents.substring(urlIndex, urlEndIndex);
			result.add(StringEscapeUtils.unescapeEcmaScript(imageURL));
			urlIndex = urlEndIndex;
		}

		return result;
	}
	
	ArrayList<String> imageSearchWithAPI(String query, int linksLimit, boolean safe) throws IOException
	{
		ArrayList<String> result = new ArrayList<String>();

		String searchURL = "https://www.googleapis.com/customsearch/v1?key=" + GoogleKey + "&cx=" + GoogleCX + "&q=" + URLEncoder.encode(query, "UTF-8") + "&searchType=image";
		JSONObject json = new JSONObject(IOUtils.toString(new URL(searchURL), Charset.forName("UTF-8")));
		JSONArray items = json.getJSONArray("items");

		for (int index = 0; index < Math.min(linksLimit, items.length()); ++index) {
			result.add(items.getJSONObject(index).getString("link"));
		}

		return result;
	}

	ArrayList<String> imageSearch(String query, int linksLimit, boolean safe)
	{
		try {
			return imageSearchWithoutAPI(query, linksLimit, safe);
		}
		catch (Exception e)
		{
			System.out.println("Access denied by Google?");
			e.printStackTrace();
		}
		// TODOKETE StartPage??
		try {
			return imageSearchWithAPI(query, linksLimit, safe);
		}
		catch (Exception e)
		{
			// TODOKETE make your own exception for this, idiot!
			if (e.getMessage().equals("Reached the daily limit.")) {
				System.out.println("Reached the daily limit of Google Custom Search API uses.");
			} else {
				System.out.println("Failed to access Google Custom Search API.");
			}
			e.printStackTrace();
		}
		return null; // return null if everything fails
	}
}
