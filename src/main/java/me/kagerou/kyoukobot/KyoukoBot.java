package me.kagerou.kyoukobot;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.sangupta.imgur.api.ImgurClient;
import com.sangupta.imgur.api.model.Image;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.Sdcf4jMessage;
import de.btobastian.sdcf4j.handler.JavacordHandler;

@Deprecated //unused, it redirected clyp.it links to #recordings
class ClypListener implements MessageCreateListener 
{
	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
        if (message.getContent().contains("https://clyp.it") && !message.getChannelReceiver().getName().equals("recordings") && !message.getAuthor().isBot() && !message.isPrivateMessage())
        {
        	Collection<Channel> col = message.getChannelReceiver().getServer().getChannels();
        	for (Channel ch: col)
        	{
        		if (ch.getName().equals("recordings"))
        			ch.sendMessage("[from " + message.getAuthor().getMentionTag() + "] " + message.getContent());
        	}
        }
    }
}

class Emote //a class for Twitch emotes
{
	static String emoteDir = "emotes";
	String name, url;
	Emote(String name, String url)
	{
		this.name = name;
		this.url = url;
	}
	String getFileName()
	{
		return System.getProperty("user.dir") + "/" + emoteDir + "/" + name + ".png";
	}
	File toFile(boolean load)
	{
		File result = new File(getFileName());
		if (!result.exists())
			if (!load)
				return null;
			else
				try {
					FileUtils.copyURLToFile(new URL(url), result);
					System.out.println("Downloaded the " + name + " emote.");
				}
				catch (Exception e)
				{
					System.out.println("Failed to download the " + name + " emote.");
					e.printStackTrace();
					return null;
				}
		return result;	
	}
}

class ImageSearchResult //a class for cached Google Image Search results
{
	long time;
	String url;
	ImageSearchResult(String url, long time)
	{
		this.url = url;
		this.time = time;
	}
}

public class KyoukoBot {
	//all the databases
	//static ArrayList<String> AllEMTs, AllChitoses;
	static ImageCollection EMTs, Chitoses;
	static MemeBase memeBase;
	static ArrayList<SongProject> Songs, CurrentSongs;
	static JSONObject JSONLyrics; //this one is for storing links to lyrics for projects, they take too long to load
	static NewDataBase Database;
	
	final static String version = "0.3.2";
	static boolean release = true;
	
	static String releaseToken = "", betaToken = "", token = "", adminID = "";
	
    static DiscordAPI api;
    final static long ReconnectTimeoutMillis = 90000; //1,5 minutes; if there was no response for this long, manual reconnection happens
    final static long ReloadTimeoutMillis = 6 * 60 * 60 * 1000; //6 hours; the timeout for obligatory rebooting
    //final static long ReloadTimeoutMillis = 5 * 60 * 1000; //5 minutes for testing purposes
    static boolean manual_reconnecting = true;
	static boolean connected_once = false;
	static long connect_time = 0; //when the bot connected to Discord and initialised all the commands
	static long init_time = 0; //when the databases were loaded last time
	
	static ConsoleOutputTracker coc;
	
    final static String EMTAlbum = "zf0yQ"; //imgur album with Emilias
    final static String OneEMT = "http://danbooru.donmai.us/data/__emilia_re_zero_kara_hajimeru_isekai_seikatsu_drawn_by_tsukimori_usako__bd95cc37a9ec5a35aded8f25e6de5c59.png";
    final static String ChitoseAlbum = "m3ipy"; //imgur album with Chitoses
    final static String OneChitose = "https://remyfool.files.wordpress.com/2016/10/vlcsnap-2016-10-09-13h30m37s147.png";
    final static String OneCat = "http://i.imgur.com/JhkPph1.jpg"; //one cat, just in case random.cat isn't available
    final static String PrettyLink = "http://i.imgur.com/3zrwfZB.png";       //links
    final static String YuzuruLink = "http://i.imgur.com/9FulDOt.png";       //to
    final static String MeguminLink = "http://i.imgur.com/X1rO0A7.png";      //different
    final static String BreakingNewsLink = "http://i.imgur.com/28fDbUq.png"; //templates
    final static String CorrectLink = "http://i.imgur.com/mHZxFlV.png";
    final static String memeDir = "memes";
    final static String SongWiki = "https://www.reddit.com/r/anime/wiki/sings/";
    final static String LyricsDatabaseFile = "projects_lyrics.txt";
    final static String DatabaseFile = "people.txt";
    final static String SearchResultsFile = "search_results.txt";
    final static long CacheDuration = 3 * 24 * 60 * 60 * 1000; //milliseconds in three days, this is how long the image search results are stored
    final static String TatsumakiID = "172002275412279296";
    final static String NadekoID = "116275390695079945";//"222681293232668672";//"255367116608241685";
    final static String BotTestingID = "218471304452374528";//"279680583578419201";
    final static int CharLimit = 1900; //not 2000 just to be safe and be able to add newlines and stuff
    static String ChangeLog;
    static MimeTypes DefaultMimeTypes = MimeTypes.getDefaultMimeTypes();
    static Tika leTika = new Tika(); //it's too slow to load a new instance every time
    
    final static String TwitchEmotes[] = {"Kappa", "MrDestructoid", "DansGame", "SwiftRage", "PJSalt",
    		"Kreygasm", "FrankerZ", "SMOrc", "BibleThump", "PogChamp",
    		"4Head", "ResidentSleeper", "Kippa", "Keepo", "EleGiggle",
    		"BrokeBack", "BabyRage", "WutFace", "deIlluminati", "HeyGuys",
    		"KappaPride", "KappaRoss", "FailFish", "NotLikeThis", "MingLee",
    		"VoHiYo", "OpieOP", "haHAA", "FeelsBirthdayMan", "FeelsBadMan",
    		"FeelsGoodMan", "KKona", "AngelThump", "LUL", "FeelsAmazingMan",
    		"TehePelo", "PunOko", "KonCha"};
    final static String GlobalEmotesURL = "https://twitchemotes.com/api_cache/v2/global.json";
    final static String BTTVEmotesURL = "https://api.betterttv.net/emotes";
    static ArrayList<Emote> Emotes;
    
    static HashMap<String, ImageSearchResult> SearchResults = new HashMap<String, ImageSearchResult>(); //cached Google Image Search results
	
	static ImgurClient imgurClient;
	
	static Timer timer = new Timer(); //timer for alarms and other stuff
	
	//waits for a message from Tatsumaki after a t!daily or t!rep command
	static TreeMap<TatsumakiRequest, TatsumakiWaiter> WaitingRoom = new TreeMap<TatsumakiRequest, TatsumakiWaiter>(); 
    
	static NadekoTracker nadekoTracker;
	
    static ArrayList<String> InitImageCollection(ImgurClient client, String album, String single_pic) 
    { //loads the links to pictures from an imgur album (or a single pic in case of failure)
    	ArrayList<String> links = new ArrayList<String>();
    	try {
    		for (Image img: client.getAlbumDetails(album).data.images)
    			links.add(img.link);
    		if (links.isEmpty())
    			throw new Exception();
    		System.out.println("Imgur album " + album + " loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		links.add(single_pic);
    		System.out.println("Failed to load imgur album " + album + "!");
    	}
    	return links;
    }
    
    public static boolean InitSongCollection(ArrayList<SongProject> Songs, ArrayList<SongProject> CurrentSongs, String SongWiki)
    { //loads all the song projects as well as the lyrics pastebins for them, JSONLyrics isn't passed because i'd have to clone a JSONObject
    	try (FileInputStream fis = new FileInputStream(LyricsDatabaseFile)) {
			JSONLyrics = new JSONObject(IOUtils.toString(fis, Charset.forName("UTF-8"))); //should be UTF-16??
		}
		catch (Exception e)
		{
			System.out.println("Failed to read the project database from file.");
			JSONLyrics = new JSONObject();
		}
    	Songs.clear();
    	try { //parsing the project wiki page
    		Document doc = Jsoup.connect(SongWiki).userAgent("KyoukoBot").get();
    		Elements tables = doc.getElementsByTag("table");
    		for (Element el: tables.get(1).getElementsByTag("tr"))
    			if (!el.getElementsByTag("td").isEmpty())
    			{
    				SongProject project = new SongProject(el, false, JSONLyrics); 
    				Songs.add(project);
    				CurrentSongs.add(project);
    			}
    		for (Element el: tables.get(2).getElementsByTag("tr"))
    			if (!el.getElementsByTag("td").isEmpty())
    				Songs.add(new SongProject(el, true, JSONLyrics));
    		System.out.println("Project wiki loaded successfully!");
    		Collections.sort(CurrentSongs); //sorting current projects so that k!proj would display them in order of increasing due date
    		return true;
    	}
    	catch (Exception e)
    	{
    		Songs.clear();
    		e.printStackTrace();
    		System.out.println("Failed to load the /r/anime sings wiki.");
    		return false;
    	}
    }

    public static boolean InitDatabase(NewDataBase Database, String DatabaseFile)
    { //loads the user database from a file
    	if (Database.readFromFile(DatabaseFile))
    	{
    		System.out.println("Database loaded successfully!");
    		return true;
    	}
    	else
    	{
    		System.out.println("Failed to load the database.");
    		return false;
    	}
    }
    
	static String InitChangeLog(String FileName)
	{ //loads the changelog from file, the latest changes are found before the first ===============
		String result = "";
		try {
			result = FileUtils.readFileToString(new File(FileName), Charset.forName("UTF-8"));
			int next;
			if ((next = result.indexOf("================")) != -1)
				result = result.substring(0, next);
			return result.trim();
		}
		catch (IOException e)
		{
			System.out.println("Failed to read the changelog.");
			return "";
		}
	}
	
	public static String wrapLinks(String str) //wraps links in messages, i'd rather use regex next time
	{ //it would be something like (?<!<)(https?://[^\s<]+[^!:,.;<\s])|(?<=<)(https?://[^\s<>]+[^!:,.;<>\s])(?=(?:[!:,.;]*(?:<|\s|$))) then wrap each group 1/2 match
		StringBuilder result = new StringBuilder();
		int index = 0;
		while (index < str.length())
		{
			int next_http = str.indexOf("http://", index);
			int next_https = str.indexOf("https://", index);
			int next_url_start;
			String protocol;
			if ((next_https == -1) || (next_http < next_https) && (next_http != -1))
			{
				next_url_start = next_http;
				protocol = "http://";
			}
			else
			{
				next_url_start = next_https;
				protocol = "https://";
			}	
			if (next_url_start == -1)
			{
				result.append(str.substring(index));
				index = str.length(); 
			}
			else
			{
				boolean less_than = ((next_url_start > 0) && (str.charAt(next_url_start - 1) == '<')), greater_than = false;
				//result.append(str.substring(index, next_url_start));
				int next_space = str.indexOf(' ', next_url_start); //doesn't handle newlines but those don't get into intros anyway
				if (next_space == -1)
					next_space = str.length();
				while ("!:,.;".indexOf(str.charAt(next_space - 1)) != -1)
					next_space--;
				if (less_than)
				{
					int greater_index = str.indexOf('>', next_url_start + protocol.length() + 1); //at least one symbol between the protocol and >
					if ((greater_index >= next_url_start) && (greater_index <= next_space))
					{
						next_space = greater_index + 1;
						greater_than = true;
					}
				}
				//int dot = intro.indexOf('.', next_url_start);
				if (greater_than)
					result.append(str.substring(index, next_space));
				else
				{
					result.append(str.substring(index, next_url_start));
					if (next_url_start + protocol.length() + 2 <= next_space) //at least two valid symbols after the protocol //((dot != -1) && (dot < next_space))
						result.append('<').append(str.substring(next_url_start, next_space)).append('>');
					else
						result.append(str.substring(next_url_start, next_space));
				}
				index = next_space;
			}
		}
		return result.toString();
	}

	static HashMap<String, ImageSearchResult> InitSearchResults(String FileName)
	{ //reads cached image search results from file
		HashMap<String, ImageSearchResult> result = new HashMap<String, ImageSearchResult>();
		try (FileInputStream fis = new FileInputStream(FileName)) {
			JSONObject json = new JSONObject(IOUtils.toString(fis, Charset.forName("UTF-16")));
			JSONArray array = json.getJSONArray("results");
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("time") > System.currentTimeMillis() - CacheDuration) //we don't want to read outdated results
					result.put(obj.getString("query"), new ImageSearchResult(obj.getString("result"), obj.getLong("time")));
			}
			System.out.println("Cached image search results loaded successfully!");
		}
		catch (Exception e)
		{
			result.clear();
			System.out.println("Failed to read cached image search results.");
		}
		return result;
	}
	
	static void SaveSearchResults(String FileName)
	{ //saves the image search results to file
		JSONObject results_json = new JSONObject();
		JSONArray array = new JSONArray();
		for (Map.Entry<String, ImageSearchResult> entry: SearchResults.entrySet())
			array.put(new JSONObject().put("query", entry.getKey()).put("result", entry.getValue().url).put("time", entry.getValue().time));
		results_json.put("results", array);
		try {
			FileUtils.writeStringToFile(new File(FileName), results_json.toString(), Charset.forName("UTF-16"));
		}
		catch (Exception e)
		{
			System.out.println("Failed to save image search results to file.");
		}
	}
	
	static boolean postOnlyFile(Message message, String url, String FileName)
	{ //posts a file without checking its MIME type
		return postOnlyFile(message, url, FileName, "");
	}
	
	static boolean postOnlyFile(Message message, String url, String FileName, String type)
	{ //posts a file if it is of a certain MIME type
		try { //TODO learn to follow redirects??
    		URL leURL = new URL(url);
    		URLConnection leConnection = leURL.openConnection();
    		String contentType = leConnection.getContentType();
    		if (contentType == null)
    			contentType = leTika.detect(leURL); //URLConnection.getContentType() fails sometimes, e.g. OpieOP emote
    		if (!contentType.startsWith(type))
    			return false;
    		String ext = DefaultMimeTypes.forName(contentType).getExtension();
    		if (ext != "")
    		{
    			System.out.println("Trying to post file: " + URLEncoder.encode(FileName, "UTF-8") + ext);
    			message.getReceiver().sendFile(leConnection.getInputStream(), URLEncoder.encode(FileName, "UTF-8") + ext);
    			return true;
    		}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
		return false;
	}
	
	static void postFile(Message message, String url, String FileName)
	{ //posts a file or its URL in case of failure (doesn't check MIME type)
		postFile(message, url, FileName, "");
	}
	
    static void postFile(Message message, String url, String FileName, String type)
    { //posts a file or its URL in case of failure (checks MIME type too)
    	if (!postOnlyFile(message, url, FileName, type))
    		message.reply(url);
    }
    
    static ArrayList<Emote> InitTwitchEmotes(String[] TwitchEmotes, String GlobalEmotesURL, String BTTVEmotesURL)
    { //loads Twitch emotes using list of them and JSON URLs 
    	ArrayList<Emote> result = new ArrayList<Emote>();
    	try {
    		JSONObject globalJSON = new JSONObject(IOUtils.toString(new URL(GlobalEmotesURL), Charset.forName("UTF-8")));
    		String template = globalJSON.getJSONObject("template").getString("small");
    		JSONObject global_emotes = globalJSON.getJSONObject("emotes");
    		for (String name: TwitchEmotes)
    			if (global_emotes.has(name))
    				result.add(new Emote(name.toLowerCase(), template.replace("{image_id}", String.valueOf(global_emotes.getJSONObject(name).getInt("image_id")))));
    		System.out.println("Global Twitch emotes loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		System.out.println("Failed to load global Twitch emotes database.");
    	}
    	try {
    		JSONArray BTTV_emotes = new JSONObject(IOUtils.toString(new URL(BTTVEmotesURL), Charset.forName("UTF-8"))).getJSONArray("emotes");
    		for (String name: TwitchEmotes)
    			for (int index = 0; index < BTTV_emotes.length(); index++)
    				if (BTTV_emotes.getJSONObject(index).getString("regex").equals(name))
    				{
    					result.add(new Emote(name.toLowerCase(), "https:" + BTTV_emotes.getJSONObject(index).getString("url") + '.' + BTTV_emotes.getJSONObject(index).getString("imageType")));
    					break;
    				}
    		System.out.println("BTTV emotes loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		System.out.println("Failed to load BTTV emotes database.");
    	}
    	result.add(new Emote("goldenkappa", "http://i.imgur.com/JwmYhu7.png")); //yup, we even have the golden Kappa
    	//loading emotes from the "emotes" directory in case the online databases are down
    	for (String name: TwitchEmotes)
    	{
    		if (!Iterables.any(result, (x) -> x.name.equalsIgnoreCase(name)))
    		{
    			Emote local_emote = new Emote(name.toLowerCase(), "");
    			if (local_emote.toFile(false) != null)
    			{
    				result.add(local_emote);
    				System.out.println("Loaded the " + name + " emote from local storage.");
    			}
    		}
    	}
    	return result;
    }
    
	static Channel findChannelByName(String name, Server server)
	{ //finds a channel with a certain name on a server
		if (server == null)
			return null;
		return Iterables.find(server.getChannels(), (x) -> x.getName().equalsIgnoreCase(name), null);
	}
    
    public static ArrayList<User> findUsersOnServer(String arg, Server server, User author)
    { //finds users on a server by a part of username/nickname
    	// priority list (case insensitive):
    	// 1. both username and nickname match the argument perfectly
    	// 2. just the nickname matches the argument perfectly
    	// 3. just the username matched the argument perfectly
    	// 4. nickname starts with the argument
    	// 5. username starts with the argument
    	// 6. nickname contains the argument
    	// 7. username contains the argument
    	arg = arg.toLowerCase(); //just in case i forget
		ArrayList<User> result = new ArrayList<User>();
		Collection<User> users;
		if (server != null)
			users = server.getMembers();
		else
		{ //it's just a DM
			users = new ArrayList<User>();
			if (author != null)
				users.add(author);
			users.add(api.getYourself());
		}
		if (server != null)
			for (User user: users)
				if (user.getName().equalsIgnoreCase(arg) && ((user.getNickname(server) == null) || user.getNickname(server).equalsIgnoreCase(arg)))
					result.add(user);
		if (server != null)
			for (User user: users)
				if ((user.getNickname(server) != null) && (user.getNickname(server).equalsIgnoreCase(arg)))
					if (!result.contains(user))
						result.add(user);
		for (User user: users)
			if (user.getName().equalsIgnoreCase(arg))
				if (!result.contains(user))
					result.add(user);
		if (server != null)
			for (User user: users)
				if ((user.getNickname(server) != null) && (user.getNickname(server).toLowerCase().startsWith(arg)))
					if (!result.contains(user))
						result.add(user);
		for (User user: users)
			if (user.getName().toLowerCase().startsWith(arg))
				if (!result.contains(user))
					result.add(user);
		if (server != null)
			for (User user: users)
				if ((user.getNickname(server) != null) && (user.getNickname(server).toLowerCase().contains(arg)))
					if (!result.contains(user))
						result.add(user);
		for (User user: users)
			if (user.getName().toLowerCase().contains(arg))
				if (!result.contains(user))
					result.add(user);
		return result;
    }
    
	public static User findUserOnServer(String arg, Server server, User author)
	{ //finds a single user on a server by a part of username/nickname
    	// priority list (case insensitive, the same as last time):
    	// 1. both username and nickname match the argument perfectly
    	// 2. just the nickname matches the argument perfectly
    	// 3. just the username matched the argument perfectly
    	// 4. nickname starts with the argument
    	// 5. username starts with the argument
    	// 6. nickname contains the argument
    	// 7. username contains the argument
		arg = arg.toLowerCase(); //just in case i forget
		User result = null;
		Collection<User> users;
		if (server != null)
			users = server.getMembers();
		else
		{ //it's just a DM
			users = new ArrayList<User>();
			if (author != null)
				users.add(author);
			users.add(api.getYourself());
		}
		if (server != null)
			for (User user: users)
				if (user.getName().equalsIgnoreCase(arg) && ((user.getNickname(server) == null) || user.getNickname(server).equalsIgnoreCase(arg)))
				{
					result = user;
   					break;
				}
		if (result == null)
			if (server != null)
				for (User user: users)
					if ((user.getNickname(server) != null) && (user.getNickname(server).equalsIgnoreCase(arg)))
					{
						result = user;
						break;
					}
		if (result == null)
			for (User user: users)
				if (user.getName().equalsIgnoreCase(arg))
				{
					result = user;
					break;
				}
		if ((result == null) && (server != null))
			for (User user: users)
				if ((user.getNickname(server) != null) && (user.getNickname(server).toLowerCase().startsWith(arg)))
				{
					result = user;
					break;
				}
		if (result == null)
			for (User user: users)
				if (user.getName().toLowerCase().startsWith(arg))
				{
					result = user;
					break;
				}
		if ((result == null) && (server != null))
			for (User user: users)
				if ((user.getNickname(server) != null) && (user.getNickname(server).toLowerCase().contains(arg)))
				{
					result = user;
					break;
				}
		if (result == null)
			for (User user: users)
				if (user.getName().toLowerCase().contains(arg))
				{
					result = user;
					break;
				}
		return result;
	}
	
	static String getNickname(User user, MessageReceiver receiver)
	{ //returns the name a user has on receiver (maybe should take Server as parameter instead)
		if (receiver instanceof Channel)
		{
			String nickname = user.getNickname(((Channel) receiver).getServer());
			if (nickname != null)
				return nickname;
		}		
		return user.getName();
	}
	
	static String getArgument(Message message, boolean toLowerCase)
	{ //returns an argument of a command called in a message
		String[] split = message.getContent().split("\\s+", 2);
		if (split.length < 2)
			return "";
		if (toLowerCase)
			return split[1].trim().toLowerCase();
		return split[1].trim();
	}
	
	static String getArgument(Message message)
	{ //returns an argument of a command called in a message (converted to lower case)
		return getArgument(message, true);
	}
	
	static String msToTimeString(long time)
	{ //converts time in milliseconds to a user-friendly string, there should be a better way to do this
		int[] divisors = {0, 24, 60, 60};
		String[] time_units = {"day", "hour", "minute", "second"};
		time /= 1000;
		long time_divided[] = new long[4];
		for (int i = 3; i >= 0; i--)
			if (divisors[i] != 0)
			{
				time_divided[i] = time % divisors[i];
				time /= divisors[i];
			}
			else
			{
				time_divided[i] = time;
				time = 0;
			}
		String result = "";
		for (int i = 0; i <= 3; i++)
			if (time_divided[i] != 0)
				if (time_divided[i] != 1)
					result += time_divided[i] + " " + time_units[i] + "s ";
				else
					result += time_divided[i] + " " + time_units[i] + " ";
		if (result.length() == 0)
			result += "0 " + time_units[3] + "s";
		else
			result = result.substring(0, result.length() - 1);
		return result;
	}
	//checks the date for April Fools events
	public static boolean isAprilFools()
	{
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 &&
				Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL;
	}
    
	static void InitPhase()
	{ //initializes all the databases
		EMTs = new ImageCollection(imgurClient, EMTAlbum, OneEMT);
		Chitoses = new ImageCollection(imgurClient, ChitoseAlbum, OneChitose);
        
        memeBase = new MemeBase(memeDir);
        
        Songs = new ArrayList<SongProject>();
        CurrentSongs = new ArrayList<SongProject>();
        InitSongCollection(Songs, CurrentSongs, SongWiki);
        
        Database = new NewDataBase();
        InitDatabase(Database, DatabaseFile);
        
        ChangeLog = InitChangeLog("changelog.txt");
        
        Emotes = InitTwitchEmotes(TwitchEmotes, GlobalEmotesURL, BTTVEmotesURL);
        
        SearchResults = InitSearchResults(SearchResultsFile);
        
        init_time = System.currentTimeMillis(); 
	}
	
	static void reboot(boolean manual)//(boolean reload)
	{ //reboots the bot, it uses the additional jar (update.jar) to relaunch KyoukoBot because api.disconnect() is currently broken in Javacord
		/*api.disconnect();
		if (reload)
			InitPhase();
		api.connectBlocking();*/
		//connect_time = System.currentTimeMillis();
		//TODO investigate this
		try {
			List<String> args = new ArrayList<String>();
			args.add("java");
			args.add("-jar");
			args.add("update.jar");
			if (manual)
				args.add("manreboot");
			else
				args.add("reboot");
			args.add("KyoukoBot.jar");
			if (!release)
				args.add("beta");
			new ProcessBuilder(args).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
			System.exit(0);
		}
		catch (IOException e)
		{
			System.out.println("Failed to reboot.");
			e.printStackTrace();
		}
	}
    
    public static void main(String args[])
    {
    	//load the credentials first
    	File credentials = new File("credentials.txt");
    	String imgurClientID = "", imgurClientSecret = "";
    	boolean loaded = false;
    	if (credentials.exists())
    	{
    		try {
    			String[] creds = FileUtils.readLines(credentials, Charset.forName("UTF-8")).toArray(new String[0]);
    			releaseToken = creds[0];
    			betaToken = creds[1];
    			adminID = creds[2];
    			imgurClientID = creds[3];
    			imgurClientSecret = creds[4];
    			YouTubeSearcher.GoogleAPIKey = GoogleSearcher.GoogleKey = creds[5];
    			GoogleSearcher.GoogleCX = creds[6];
    			loaded = true;
    		}
    		catch (Exception e)
    		{
    			loaded = false;
    			e.printStackTrace();
    			return;
    		}
    	}
    	if (!loaded)
    	{
    		System.out.println("Failed to read the credentials.");
    		return;
    	}
    	if (Arrays.asList(args).contains("beta")) //launching the "release" bot by default, "beta" one if there's a "beta" argument
    		release = false;
    	token = (release) ? releaseToken : betaToken;
    	//initialising everything
    	coc = new ConsoleOutputTracker();
    	System.setProperty("http.agent", "KyoukoBot");
        api = Javacord.getApi(token, true);
        imgurClient = new ImgurClient(imgurClientID, imgurClientSecret);
        InitPhase();
        FutureCallback<DiscordAPI> callback = new FutureCallback<DiscordAPI>() {
        		@Override
        		public void onSuccess(DiscordAPI api) {
        			Database.adjustNames(api.getUsers()); //fix the database on startup TODO seems like an exception can happen here
        			
        			CommandHandler handler = new JavacordHandler(api);
        			// simple picture-posting commands
        			handler.registerCommand(new ShiyuCommand());
        			handler.registerCommand(new EMTCommand(EMTs, "Emilia-tan"));
        			handler.registerCommand(new RemCommand()); //this one is hidden
        			handler.registerCommand(new ChitoseCommand(Chitoses, "Chitose"));
        			handler.registerCommand(new CatCommand());
        			// "meme"-related commands
        			handler.registerCommand(new LeMemeCommand());
        			handler.registerCommand(new UploadCommand());
        			handler.registerCommand(new DeleteCommand());
        			// commands filling image templates
        			handler.registerCommand(new PrettyCommand());
        			handler.registerCommand(new YuzuruCommand());
        			handler.registerCommand(new MeguminCommand());
        			handler.registerCommand(new BreakingNewsCommand(BreakingNewsLink));
        			handler.registerCommand(new CorrectCommand(CorrectLink));
        			// a little bit of social interaction
        			handler.registerCommand(new HugCommand());
        			//handler.registerCommand(new ChocolateCommand()); //limited Valentine's Day event
        			handler.registerCommand(new WhoIsCommand());
        			handler.registerCommand(new SetIntroCommand());
        			handler.registerCommand(new IntroCommand()); //a redirect to k!setintro command
        			// project-related commands
        			handler.registerCommand(new ProjectCommand());
        			handler.registerCommand(new WikiCommand());
        			handler.registerCommand(new SpeadsheetCommand());
        			// different forms of Google search
        			handler.registerCommand(new ImageCommand());
        			handler.registerCommand(new GoogleShortCommand(1, true));
        			handler.registerCommand(new GoogleLongCommand(3, false));
        			handler.registerCommand(new YouTubeShortCommand(1, true));
        			handler.registerCommand(new YouTubeLongCommand(3, false));
        			handler.registerCommand(new AnimeLyricsCommand());
        			// this one reminds users about Tatsumaki's daily commands (t!daily and t!rep)
        			handler.registerCommand(new DailyCommand());
        			// service commands
        			handler.registerCommand(new PingCommand());
        			handler.registerCommand(new InfoCommand());
        			handler.registerCommand(new HelpCommand(handler));
        			handler.registerCommand(new ChangeLogCommand());
        			handler.registerCommand(new UptimeCommand());
        			handler.registerCommand(new TokenCommand()); //a bit of an easter egg
        			// calculator because why not
        			handler.registerCommand(new CalcCommand());
        			//admin-only commands
        			handler.registerCommand(new IntroUserCommand());
        			//handler.registerCommand(new IntroDefaultCommand());
        			handler.registerCommand(new SetGameCommand());
        			handler.registerCommand(new RehashCommand());
        			handler.registerCommand(new ConsoleCommand());
        			handler.registerCommand(new ShutdownCommand());
        			handler.registerCommand(new RebootCommand());
        			handler.registerCommand(new UpdateCommand());
        			handler.registerCommand(new RequestCommand());
        			handler.registerCommand(new ConvertCommand());
        			handler.registerCommand(new FetchRecCommand()); //RIP
        			handler.registerCommand(new RemindMeCommand());
        			handler.registerCommand(new ListAlarmsCommand());
        			handler.registerCommand(new MessageCommand());
        			handler.registerCommand(new SayCommand());
        			handler.registerCommand(new DelFileCommand());
        			handler.registerCommand(new MemoryCommand());
        			// other listeners that couldn't be made into commands
        			api.registerListener(new ExtraListener(handler)); //Twitch emotes + wrong commands + easter eggs
        			api.registerListener(new AnimemesListener()); //saving "memes" from #animemes
        			api.registerListener(new NameChangeListener()); //keeps correct usernames in the database
        			api.registerListener(new ShiyuReactionListener()); //easter egg
    				api.registerListener(new TatsumakiListener(TatsumakiID, WaitingRoom, timer)); //listens for t!daily/t!rep commands and Tatsumaki's response
    				
        			api.setGame(Database.game);
        			api.setAutoReconnect(true);
        			KyoukoBot.connected_once = true;
        			connect_time = System.currentTimeMillis();
        			//finding the admin and messaging them about going online/rebooting
        			handler.addPermission(adminID, "admin");
        			User admin = Iterables.find(api.getUsers(), (x) -> x.getId().equals(adminID), null);
        			Sdcf4jMessage.MISSING_PERMISSIONS.setMessage("Y-you're touching me inappropriately!");
        			if (Arrays.asList(args).contains("rebooted")) //"rebooted", "updated" and "hello" arguments are passed by update.jar
        				System.out.println("Reboot completed!");
        			if (Arrays.asList(args).contains("hello"))
        				if (admin != null)
        					if (Arrays.asList(args).contains("rebooted"))
        						admin.sendMessage("`Manual reboot completed!`");
        					else
        						admin.sendMessage("`I'm online!`");
        				else
        					System.out.println("Couldn't find the owner.");
        			if (Arrays.asList(args).contains("updated"))
        				if (admin != null)
        					admin.sendMessage("`Self-update completed!`");
        				else
        					if (!Arrays.asList(args).contains("hello")) //no point in printing the same line twice
        						System.out.println("Couldn't find the owner.");
        			
        			nadekoTracker = new NadekoTracker(NadekoID, BotTestingID, timer, Iterables.any(Arrays.asList(args), (x) -> (x.equalsIgnoreCase("nadeko"))));
        		}
        		@Override
        		public void onFailure(Throwable t) {
        			//t.printStackTrace();
        			System.out.println("Failed to connect >_<");
        		}
        	};
        	
       api.connect(callback); //actual connecting!
       //the whole manual reconnecting business, Javacord doesn't do it perfectly
       if (manual_reconnecting)
    	   while (true)
    	   {
    		   try {
    			   Thread.sleep(ReconnectTimeoutMillis); //probably would use timers next time
    			   double mem_mbs = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0;
    			   mem_mbs = Math.round(mem_mbs * 1000.0) / 1000.0;
    			   System.out.println("Current memory usage: " + mem_mbs + " MB");
    			   if (System.currentTimeMillis() - init_time > ReloadTimeoutMillis)
    			   { //rebooting if databases were loaded ReloadTimeoutMillis ms ago (usually 6 hours)
    				   System.out.println("Obligatory rebooting...");
    				   //reboot(true);
    				   reboot(false);
    			   }
    			   else
    				   if (!coc.newOutput() && connected_once)
    				   { //reconnect if there's no new console output
    					   System.out.println("RECONNECTING MANUALLY!");
    					   //reboot(false);
    					   api.disconnect();
    					   api.connectBlocking(); //TODO reboot(false)
    				   }
    				   else
    					   if (!connected_once)
    					   { //connect again if the commands are not loaded
    						   System.out.println("CONNECTING MANUALLY!");
    						   api.disconnect();
    						   api.connect(callback);
    					   }   
    		   }
    		   catch (InterruptedException ie)
    		   {
    			   ie.printStackTrace();
    			   //System.exit(-1); //this shouldn't be the line that crashes Kyouko but who knows :thinking:
    		   }
    		   catch (Exception e)
    		   {
    			   e.printStackTrace();
    		   }
    	   }
    }
    
//TODO post k!who all in private chat if it's too big 
//TODO enforce the "no chat in #recordings" rule
//TODO searching projects by songmasters
//TODO autoreboot script, lol
//TODO formatting in k!who
//TODO capture Twitter/YouTube links in #animemes, really??
//TODO https://i.imgur.com/JWMThRi.png
//TODO save the database after discarding outdated alarms somehow??
//TODO special case the remastered version of the Imagination project, really??
//TODO "do it for her" meme??
//TODO reminders about Nadeko flowers?? (test it!)
//TODO k!img fix 18 year old phone ( http://previews.123rf.com/images/vlue/vlue1002/vlue100200039/6408218-Young-18-year-old-adult-teenager-yells-into-his-wireless-phone-isolated-on-white-background--Stock-Photo.jpg )
//TODO announcements before projects' due dates (1 week and 1 day)
//TODO k!marry
//TODO downforeveryoneorjustme??
//TODO track old messages during a reboot??
//TODO remindme??
//TODO learn to delete https://cdn.discordapp.com/attachments/245044272473047040/287006575418408970/cff1a95dbe0328f89eae7f93ac4c08fc.png or https://cdn.discordapp.com/attachments/218471304452374528/287131941852151808/cff1a95dbe0328f89eae7f93ac4c08fc.jpg >_<
//TODO learn to delete https://cdn.discordapp.com/attachments/218471304452374528/298517133183418369/346705a8fc0f330993d1f43a39b2f722.jpg too
//TODO delete by filename if everything fails??
//TODO reminders when 5 minutes are left before t!daily and t!rep??
//TODO discard alarms for unknown users??
//TODO k!wtf??
//TODO limits??
//TODO "kill script"??
//TODO k!recordings person (outclassed by the discord search, sigh)
//TODO whatanime.ga??
//TODO headpats??
//TODO typerace??
//TODO auto-selfupdate from git??
//TODO assume "Kyouko" role??
//TODO global message queue to fix stability issues??
//TODO fix the issue of failing to handle a few posts with twitch emotes in quick succession?? (doesn't appear to happen with commands)   
//TODO make k!hug random not ping inactive people
//TODO logs??
//TODO OOC command??
//TODO kumirei pictures??
//TODO fancy embeds??
//TODO customisable commands??
    
//TODO random rolls??
//TODO "another daily to farm"??    
//TODO JDA?!
}