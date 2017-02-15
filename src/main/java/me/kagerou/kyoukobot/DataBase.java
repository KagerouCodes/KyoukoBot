package me.kagerou.kyoukobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
//old database, not used anymore
@Deprecated
class DataBase
{
	long time;
	final static String defaultGame = "!k help | k!proj Database"; 
	String game;
	TreeMap<String, String> intros;
	DataBase()
	{
		time = 0;
		game = "k!help | k!info Database";
		intros = new TreeMap<String, String>((s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
	}
	void defaultFill(String FileName)
	{
		time = 0;
		game = defaultGame;
		intros.clear();
		intros.put("Kyouko", "Hey, that's me! Type k!info if you want to know more.");
		intros.put("Kagerou", "My cute master~ Fails at singing pretty hard.");
		intros.put("Nyako", "A crazy moeblob of existence.");
		intros.put("kaitlin*", "So moe you're gonna die! <:Heck:251011995539603458>");
		intros.put("shiyu", "She's shiyu, no matter what her nickname is. Known for being salty. Has a cute voice.");
		intros.put("MrPot4to", "A professional weeaboo and my main provider of Chitose pictures.");
		intros.put("pandaxtc", "He's a panda. And he loves Chitose.");
		intros.put("Electrorocket", "A crazy gentleman who'll never give you up. Occasionally turns into a god :cloud_lightning:");
		intros.put("Phreid", "Literally a rap god.");
		intros.put("kail", "Can rap in Korean like a boss.");
		intros.put("Tora_the_Tiger", "Admin of this Discord channel. People call him a lowkey Hitler but like his voice.");
		intros.put("ZTN", "High tension japanese goburin! :japanese_goblin: <:ZTN:251016635106721793>");
		SaveToFile(FileName);
	}
	boolean readFromFile(String FileName)
	{
		time = 0;
		game = defaultGame;
		intros.clear();
		JSONObject json;
		try {
			json = new JSONObject(IOUtils.toString(new FileInputStream(FileName), Charset.forName("UTF-8"))); //should be UTF-16??
		}
		catch (Exception e)
		{
			System.out.println("Failed to read the database from file.");
			return false;
		}
		if (json.has("time"))
			time = json.getLong("time");
		if (json.has("game"))
			game = json.getString("game");
		if (json.has("intros"))
		{
			JSONArray array = json.getJSONArray("intros");
			for (int i = 0; i < array.length(); i++)
				intros.put(array.getJSONObject(i).getString("name"), array.getJSONObject(i).getString("intro"));
		}
		else
		{
			defaultFill(FileName);
			return false;
		}
		return true;
	}
	JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("game", game);
		JSONArray array = new JSONArray();
		for (Map.Entry<String, String> person: intros.entrySet())
			array.put(new JSONObject().put("name", person.getKey()).put("intro", person.getValue()));
		json.put("intros", array);
		return json;
	}
	public String toString()
	{
		return toJSONObject().toString();
	}
	boolean SaveToFile(String FileName)
	{
		try {
			FileUtils.writeStringToFile(new File(FileName), toString(), Charset.forName("UTF-8"));
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
	String get(String name) //not used
	{
		String result = intros.get(name);
		if (result == null)
			result = new String();
		return result;
	}
	Map.Entry<String, String> getEntry(String name)
	{
		Map.Entry<String, String> result = intros.ceilingEntry(name);
		if ((result != null) && (intros.comparator().compare(result.getKey(), name) == 0))
			return result;
		return null;
	}
	Map.Entry<String, String> getPartialEntry(String name)
	{
		Map.Entry<String, String> result = intros.ceilingEntry(name);
		if ((result != null) && (result.getKey().toLowerCase().startsWith(name.toLowerCase())))
			return result;
		return null;
	}
	Map.Entry<String, String> getPartialEntryOnServer(String name, Server server)
	{
		String name_inc = name.substring(0, name.length() - 1) + (char) (name.charAt(name.length() - 1) + 1);
		TreeSet<String> usernames = new TreeSet<String> ((s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
		for (User user: server.getMembers())
			usernames.add(user.getName());
		for (String key: intros.navigableKeySet().subSet(name, name_inc))
			if (usernames.contains(key))
				return intros.ceilingEntry(key);
		return null;
	}
	void set(String name, String intro)
	{
		time = System.currentTimeMillis();
		if (!intro.isEmpty())
			intros.put(name, intro);
		else
			intros.remove(name);
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	void setGame(String game)
	{
		this.game = game;
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	String convert(DiscordAPI api) {
		JSONObject json = new JSONObject(); 
		json.put("time", time);
		json.put("game", game);
		int noID = 0;
		JSONObject intros_json = new JSONObject();
		for (Map.Entry<String, String> person: intros.entrySet())
		{
			User user = Iterables.find(api.getUsers(), (x) -> x.getName().equalsIgnoreCase(person.getKey()), null);
			String id = (user == null) ? ("-" + ++noID) : user.getId();
			intros_json.put(id, new JSONObject().put("name", person.getKey()).put("intro", person.getValue()));
		}
		json.put("intros", intros_json);
		return json.toString(2);
	}
}