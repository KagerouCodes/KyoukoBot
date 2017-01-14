package me.kagerou.kyoukobot;

import java.io.*;
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

import com.google.common.util.concurrent.FutureCallback;
import com.sangupta.imgur.api.ImgurClient;
import com.sangupta.imgur.api.model.Image;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

@Deprecated //unused
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

class Emote
{
	String name, url;
	Emote(String name, String url)
	{
		this.name = name;
		this.url = url;
	}
}

class SearchResult
{
	long time;
	String url;
	SearchResult(String url, long time)
	{
		this.url = url;
		this.time = time;
	}
}

class ConsoleOutputTracker {
	private boolean newStuff;
	final static int MaxBufferSize = 50000;
	private char[] LastOutput = new char[MaxBufferSize];
	private PrintStream ps;
	int first_index, buffer_size;

    public ConsoleOutputTracker()
    {
    	newStuff = false;
    	first_index = buffer_size = 0;
    	ps = new PrintStream(new OutputStreamTracker(this));
    	System.setOut(ps);
    	System.setErr(ps);
    }
    
    public void setFlag(boolean flag)
    {
    	newStuff = flag;
    }
    
    public boolean newOutput()
    {
    	boolean result = newStuff;
    	newStuff = false;
    	return result;
    }
    
    synchronized void push(char ch)
    {
    	LastOutput[(first_index + buffer_size) % MaxBufferSize] = ch;
    	if (buffer_size != MaxBufferSize)
    		buffer_size++;
    	else
    		first_index = (first_index + 1) % MaxBufferSize;
    }
    
    synchronized void pushCodePoint(int cp)
    {
    	for (char ch: Character.toChars(cp))
    		push(ch);
    }
    
    synchronized public String getLastOutput()
    {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < buffer_size; i++)
    		sb.append(LastOutput[(first_index + i) % MaxBufferSize]);
    	return sb.toString();
    }
    
    synchronized public void stop()
    {
    	ps.close();
    	ps = null;
    }

    class OutputStreamTracker extends OutputStream {
    	ConsoleOutputTracker tracker;
    	PrintStream old;
    	PrintStream old_err;

        public OutputStreamTracker(ConsoleOutputTracker cot) {
            tracker = cot;
            old = System.out;
            old_err = System.err;
        }

        public void write(int b) throws IOException {
            old.write(b);
            tracker.setFlag(true);
            tracker.pushCodePoint(b);
        }

        public void flush() throws IOException {
            old.flush();
        }

        public void close() throws IOException {
            super.close();
            System.setOut(old);
            System.setErr(old_err);
        }
    }
}


public class KyoukoBot {
	static boolean EMTAlbumFound, ChitoseAlbumFound;
	static ArrayList<String> AllEMTs, AllChitoses;
	static MemeBase memeBase;
	static ArrayList<SongProject> Songs, CurrentSongs;
	static JSONObject JSONLyrics;
	static DataBase Database;
	
	final static String version = "0.2.3";
	final static boolean release = false;

	final static String releaseToken = "MjU0MTk0MTM3MTE1NTI1MTIw.CyLgRg.ZX1BeaPzWNBpgLmTeWP4bbYYWzI";
	final static String betaToken = "MjU1MzY3MTE2NjA4MjQxNjg1.Cyck1g.Fdf27IMvJBnmO2Hla43qh5hE8LM";
	final static String token = (release) ? releaseToken : betaToken;
	final static String adminID = "222681293232668672";
	
    static DiscordAPI api;
    final static long ReconnectTimeoutMillis = 90000/*120000*//*1800000*/;
    final static long ReloadTimeoutMillis = 6 * 60 * 60 * 1000; //6 hours
    static boolean manual_reconnecting = true;
	static boolean connected_once = false;
	static long connect_time = 0;
	static long init_time = 0;
	
	static ConsoleOutputTracker coc;
	
    final static String EMTs = "zf0yQ";
    final static String OneEMT = "http://danbooru.donmai.us/data/__emilia_re_zero_kara_hajimeru_isekai_seikatsu_drawn_by_tsukimori_usako__bd95cc37a9ec5a35aded8f25e6de5c59.png";
    final static String Chitoses = "m3ipy";//"wI6CQ";
    final static String OneChitose = "https://remyfool.files.wordpress.com/2016/10/vlcsnap-2016-10-09-13h30m37s147.png";
    final static String OneCat = "http://i.imgur.com/JhkPph1.jpg";
    final static String PrettyLink = "http://i.imgur.com/3zrwfZB.png";
    final static String memeDir = "memes";
    //final static String LeMemes = "hE33X";//"5Lt5e";
    //final static String OneLeMeme = "http://i0.kym-cdn.com/photos/images/facebook/001/115/949/d0d.jpg"; //"how to not shitpost"
    final static String SongWiki = "https://www.reddit.com/r/anime/wiki/sings/";
    final static String LyricsDatabaseFile = "projects_lyrics.txt";
    final static String DatabaseFile = "people.txt";
    final static String SearchResultsFile = "search_results.txt";
    final static long CacheDuration = 3 * 24 * 60 * 60 * 1000; //milliseconds in three days
    final static int CharLimit = 1900;
    static String ChangeLog;
    static MimeTypes DefaultMimeTypes = MimeTypes.getDefaultMimeTypes();
    static Tika leTika = new Tika();
    
    final static String TwitchEmotes[] = {"Kappa", "MrDestructoid", "DansGame", "SwiftRage", "PJSalt", //TODO add KappaRossPride??
    		"Kreygasm", "FrankerZ", "SMOrc", "BibleThump", "PogChamp",
    		"4Head", "ResidentSleeper", "Kippa", "Keepo", "EleGiggle",
    		"BrokeBack", "BabyRage", "WutFace", "deIlluminati", "HeyGuys",
    		"KappaPride", "KappaRoss", "FailFish", "NotLikeThis", "MingLee",
    		"VoHiYo", "OpieOP", "haHAA", "FeelsBirthdayMan", "FeelsBadMan",
    		"FeelsGoodMan", "KKona", "AngelThump", "LUL", "FeelsAmazingMan"};
    final static String GlobalEmotesURL = "https://twitchemotes.com/api_cache/v2/global.json";
    final static String BTTVEmotesURL = "https://api.betterttv.net/emotes";
    static ArrayList<Emote> Emotes;
    
    static HashMap<String, SearchResult> SearchResults = new HashMap<String, SearchResult>();
	
	static ImgurClient imgurClient;
    
    static ArrayList<String> InitImageCollection(ImgurClient client, String album, String single_pic)
    {
    	ArrayList<String> links = new ArrayList<String>();
    	boolean success = true;
    	try {
    		for (Image img: client.getAlbumDetails(album).data.images)
    		{
    			links.add(img.link);
    			//links.add(img.getLink());
    			//System.out.println(img.link);
    		}
    		if (links.isEmpty())
    			throw new Exception();
    		System.out.println("Imgur album " + album + " loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		success = false;
    		links.clear();
    		links.add(single_pic);
    		System.out.println("Failed to load imgur album " + album + "!");
    	}
    	if (success)
    		return links;
    	return null;
    }
    
    public static boolean InitSongCollection(ArrayList<SongProject> Songs, ArrayList<SongProject> CurrentSongs, String SongWiki)
    {
    	try {
			JSONLyrics = new JSONObject(IOUtils.toString(new FileInputStream(LyricsDatabaseFile), Charset.forName("UTF-8"))); //should be UTF-16??
		}
		catch (Exception e)
		{
			System.out.println("Failed to read the project database from file.");
			JSONLyrics = new JSONObject();
		}
    	Songs.clear();
    	try {
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

    public static boolean InitDatabase(DataBase Database, String DatabaseFile)
    {
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
    
	static String InitChangeLog(String FileName) {
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
	
	public static String wrapLinks(String str)
	{
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
	
	static HashMap<String, SearchResult> InitSearchResults(String FileName)
	{
		HashMap<String, SearchResult> result = new HashMap<String, SearchResult>();
		try {
			JSONObject json = new JSONObject(IOUtils.toString(new FileInputStream(FileName), Charset.forName("UTF-16")));
			JSONArray array = json.getJSONArray("results");
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject obj = array.getJSONObject(i);
				if (obj.getLong("time") > System.currentTimeMillis() - CacheDuration) //we don't want to read outdated queries
					result.put(obj.getString("query"), new SearchResult(obj.getString("result"), obj.getLong("time")));
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
	{
		JSONObject results_json = new JSONObject();
		JSONArray array = new JSONArray();
		for (Map.Entry<String, SearchResult> entry: SearchResults.entrySet())
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
	{
		return postOnlyFile(message, url, FileName, "");
	}
	
	static boolean postOnlyFile(Message message, String url, String FileName, String type)
	{
		try { //TODO learn to follow redirects??
    		URL leURL = new URL(url);
    		URLConnection leConnection = leURL.openConnection();
    		String contentType = leConnection.getContentType();
    		if (contentType == null)
    			contentType = leTika.detect(leURL); //what an odd fix for OpieOP
    		if (!contentType.startsWith(type))
    			return false;
    		String ext = DefaultMimeTypes.forName(contentType).getExtension();
    		if (ext != "")
    		{
    			System.out.println("Trying to post file: " + URLEncoder.encode(FileName, "UTF-8") + ext);
    			message.replyFile(leConnection.getInputStream(), URLEncoder.encode(FileName, "UTF-8") + ext)/*.get()*/;
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
	{
		postFile(message, url, FileName, "");
	}
	
    static void postFile(Message message, String url, String FileName, String type)
    {
    	if (!postOnlyFile(message, url, FileName, type))
    		message.reply(url);
    }
    
    static ArrayList<Emote> InitTwitchEmotes(String[] TwitchEmotes, String GlobalEmotesURL, String BTTVEmotesURL)
    {
    	//HashMap<String, String> result = new HashMap<String, String>();
    	ArrayList<Emote> result = new ArrayList<Emote>();
    	try {
    		JSONObject globalJSON = new JSONObject(IOUtils.toString(new URL(GlobalEmotesURL), Charset.forName("UTF-8")));
    		String template = globalJSON.getJSONObject("template").getString("small");
    		JSONObject global_emotes = globalJSON.getJSONObject("emotes");
    		for (String name: TwitchEmotes)
    			if (global_emotes.has(name))
    				result.add(new Emote(name.toLowerCase(), template.replace("{image_id}", String.valueOf(global_emotes.getJSONObject(name).getInt("image_id")))));
    				//result.put(name.toLowerCase(), template.replace("{image_id}", String.valueOf(global_emotes.getJSONObject(name).getInt("image_id"))));
    		System.out.println("Global Twitch emotes loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		System.out.println("Failed to load global Twitch emotes.");
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
    					//result.put(name.toLowerCase(), "https:" + BTTV_emotes.getJSONObject(index).getString("url") + '.' + BTTV_emotes.getJSONObject(index).getString("imageType"));
    		System.out.println("BTTV emotes loaded successfully!");
    	}
    	catch (Exception e)
    	{
    		System.out.println("Failed to load BTTV emotes.");
    	}
    	return result;
    }
    
	static void InitPhase() {
		AllEMTs = InitImageCollection(imgurClient, EMTs, OneEMT);
        AllChitoses = InitImageCollection(imgurClient, Chitoses, OneChitose);
        
        memeBase = new MemeBase(memeDir);
        
        Songs = new ArrayList<SongProject>();
        CurrentSongs = new ArrayList<SongProject>();
        InitSongCollection(Songs, CurrentSongs, SongWiki);
        
        Database = new DataBase();
        InitDatabase(Database, DatabaseFile);
        
        ChangeLog = InitChangeLog("changelog.txt");
        
        Emotes = InitTwitchEmotes(TwitchEmotes, GlobalEmotesURL, BTTVEmotesURL);
        
        SearchResults = InitSearchResults(SearchResultsFile);
        
        init_time = System.currentTimeMillis(); 
	}
	
	static void reboot(boolean reload)
	{
		api.disconnect();
		if (reload)
			InitPhase();
		api.connectBlocking();
		connect_time = System.currentTimeMillis();
	}
    
    public static void main(String args[]) {
    	coc = null;
    	if (manual_reconnecting)
    		coc = new ConsoleOutputTracker();
    	System.setProperty("http.agent", "KyoukoBot");
        api = Javacord.getApi(token, true);
        imgurClient = new ImgurClient("2e201595b0e2dc9", "08fff5052be91a3ae9e6685858f66a81399c11fa");
        InitPhase();
        FutureCallback<DiscordAPI> callback = new FutureCallback<DiscordAPI>() {
        		@Override
        		public void onSuccess(DiscordAPI api) {
        			KyoukoBot.connected_once = true;
        			CommandHandler handler = new JavacordHandler(api);
        			handler.registerCommand(new PingCommand());
        			handler.registerCommand(new ShiyuCommand());
        			handler.registerCommand(new EMTCommand());
        			handler.registerCommand(new RemCommand()); //this one is hidden
        			handler.registerCommand(new ChitoseCommand());
        			handler.registerCommand(new CatCommand());
        			
        			handler.registerCommand(new LeMemeCommand());
        			handler.registerCommand(new UploadCommand());
        			handler.registerCommand(new DeleteCommand());
        			
        			handler.registerCommand(new HugCommand());
        			handler.registerCommand(new ProjectCommand());
        			handler.registerCommand(new WhoIsCommand());
        			handler.registerCommand(new IntroCommand());
        			handler.registerCommand(new WikiCommand());
        			
        			handler.registerCommand(new ImageCommand());
        			handler.registerCommand(new GoogleShortCommand(1, true));
        			handler.registerCommand(new GoogleLongCommand(3, false));
        			handler.registerCommand(new YouTubeShortCommand(1, true));
        			handler.registerCommand(new YouTubeLongCommand(3, false));
        			handler.registerCommand(new AnimeLyricsCommand());
        			
        			handler.registerCommand(new PrettyCommand(PrettyLink));
        			//handler.registerCommand(new BreakingNewsCommand());
        			
        			handler.registerCommand(new InfoCommand());
        			handler.registerCommand(new HelpCommand(handler));
        			handler.registerCommand(new ChangeLogCommand());
        			handler.registerCommand(new UptimeCommand());
        			
        			handler.registerCommand(new CalcCommand());
        			
        			handler.registerCommand(new IntroUserCommand());
        			handler.registerCommand(new IntroDefaultCommand());
        			handler.registerCommand(new SetGameCommand());
        			handler.registerCommand(new RehashCommand());
        			handler.registerCommand(new ConsoleCommand());
        			handler.registerCommand(new ShutdownCommand());
        			handler.registerCommand(new RebootCommand());
        			handler.registerCommand(new UpdateCommand());
        			
        			/*api.registerListener(new WrongCommandListener(handler));
        			api.registerListener(new TwitchListener());*/
        			api.registerListener(new ExtraListener(handler)); //Twitch emotes + wrong commands + easter eggs
        			api.registerListener(new AnimemesListener());

        			api.setGame(Database.game);
        			api.setAutoReconnect(true);
        			connect_time = System.currentTimeMillis();
        			
        		}
        		@Override
        		public void onFailure(Throwable t) {
        			//t.printStackTrace();
        			System.out.println("Failed to connect >_<");
        		}
        	};
       api.connect(callback);
       if (manual_reconnecting)
    	   while (true)
    	   {
    		   try {
    			   Thread.sleep(ReconnectTimeoutMillis);
    			   if (System.currentTimeMillis() - init_time > ReloadTimeoutMillis)
    			   {
    				   System.out.println("Obligatory rebooting...");
    				   reboot(true);
    			   }
    			   else
    				   if (!coc.newOutput())
    					   if (connected_once)
    					   {
    						   System.out.println("RECONNECTING MANUALLY!");
    						   reboot(false);
    					   }
    					   else
    					   {
    						   System.out.println("CONNECTING MANUALLY!");
    						   api.disconnect();
    						   api.connect(callback);
    					   }   
    		   }
    		   catch (InterruptedException ie)
    		   {
    			   ie.printStackTrace();
    			   System.exit(-1);
    		   }
    		   catch (Exception e)
    		   {
    			   e.printStackTrace();
    		   }
    	   }
    }

//TODO nickname support (from the new Javacord)
//TODO k!recordings person
//TODO Breaking Your Own News??
//TODO whatanime.ga??
//TODO auto-selfupdate from git??
//TODO Google search using Startpage??
//TODO assume "Kyouko" role??
//TODO global message queue to fix stability issues??
//TODO fix the issue of failing to handle a few posts with twitch emotes in quick succession?? (doesn't appear to happen with commands)   
//TODO make k!hug random not ping inactive people
//TODO logs??
//TODO OOC command??
//TODO remindme??
//TODO kumirei pictures??
//TODO fancy embeds??
    
//TODO random rolls??
//TODO limits??
//TODO "another daily to farm"??
//TODO make Tora dodge hugs, possibly with RNG, wtf??    
//TODO JDA?!
}