package me.kagerou.kyoukobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;

class NewDataBase //TODO: cloud backup??
{
	class Person
	{
		String name;
		String intro;
		Person(String name, String intro)
		{
			this.name = name;
			this.intro = intro;
		}
		Person()
		{
			this("", "");
		}
	}
	
	long time;
	final static String defaultGame = "!k help | k!proj Database"; 
	String game;
	TreeMap<String, Person> people;
	NewDataBase()
	{
		time = 0;
		game = "k!help | k!info Database";
		people = new TreeMap<String, Person>();
	}
	synchronized void defaultFill(String FileName)
	{
		time = 0;
		game = defaultGame;
		/*intros.clear();
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
		intros.put("ZTN", "High tension japanese goburin! :japanese_goblin: <:ZTN:251016635106721793>");*/
		SaveToFile(FileName);
	}
	synchronized boolean readFromFile(String FileName)
	{
		time = 0;
		game = defaultGame;
		people.clear();
		JSONObject json;
		try {
			json = new JSONObject(IOUtils.toString(new FileInputStream(FileName), Charset.forName("UTF-8"))); //should be UTF-16??
			if (json.has("time"))
				time = json.getLong("time");
			if (json.has("game"))
				game = json.getString("game");
			if (json.has("intros"))
			{
				JSONObject intros = json.getJSONObject("intros");
				for (Object id: intros.keySet())
					people.put((String) id, new Person(intros.getJSONObject((String) id).getString("name"), intros.getJSONObject((String) id).getString("intro")));
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Failed to read the database from file.");
		}
		people.clear();
		return false;
	}
	synchronized JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("game", game);
		JSONObject intros_json = new JSONObject();
		for (Map.Entry<String, Person> person: people.entrySet())
			intros_json.put(person.getKey(), new JSONObject().put("name", person.getValue().name).put("intro", person.getValue().intro));
		json.put("intros", intros_json);
		return json;
	}
	public String toString()
	{
		return toJSONObject().toString(2);
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
	synchronized Person get(String id)
	{
		return people.get(id);
	}
	synchronized Map.Entry<String, Person> findUnIDedEntry(String name)
	{
		SortedMap<String, Person> UnIDedPeople;
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			UnIDedPeople = people.headMap(firstID, false);
		else
			UnIDedPeople = people;
		return Iterables.find(UnIDedPeople.entrySet(), (x) -> x.getValue().name.equalsIgnoreCase(name), null);
	}
	synchronized Map.Entry<String, Person> findIDedEntry(String name)
	{
		SortedMap<String, Person> IDedPeople;
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			IDedPeople = people.tailMap(firstID);
		else
			IDedPeople = new TreeMap<String, Person>();
		return Iterables.find(IDedPeople.entrySet(), (x) -> x.getValue().name.equalsIgnoreCase(name), null);
	}
	synchronized Map.Entry<String, Person> findEntry(String name) //RIP efficiency, that's some bad code
	{
		Map.Entry<String, Person> result;
		if ((result = findIDedEntry(name)) != null)
			return result;
		return findUnIDedEntry(name);
	}
	synchronized Map.Entry<String, Person> findPartialEntry(String name)
	{
		String lower_name = name.toLowerCase();
		Set<Map.Entry<String, Person>> UnIDedPeople;
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			UnIDedPeople = people.headMap(firstID, false).entrySet();
		else
			UnIDedPeople = people.entrySet();
		
		Set<Map.Entry<String, Person>> IDedPeople;
		if (firstID != null)
			IDedPeople = people.tailMap(firstID).entrySet();
		else
			IDedPeople = new TreeSet<Map.Entry<String, Person>>();
		
		Map.Entry<String, Person> result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.equalsIgnoreCase(lower_name), null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.equalsIgnoreCase(lower_name), null)) != null)
			return result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.toLowerCase().startsWith(lower_name), null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.toLowerCase().startsWith(lower_name), null)) != null)
			return result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.toLowerCase().contains(lower_name), null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.toLowerCase().contains(lower_name), null)) != null)
			return result;
		return null;
	}
	synchronized String firstNegativeID()
	{
		String result;
		for (int i = 1; people.containsKey(result = "-" + i); i++);
		return result;
	}
	
	synchronized boolean removeEntry(String id)
	{
		if (!people.containsKey(id))
			return false;
		people.remove(id);
		System.out.println("Removed " + id + " entry from the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}
	synchronized boolean changeID(String oldID, String newID) //needed to fix unIDed entries
	{
		if (!people.containsKey(oldID))
			return false;
		people.put(newID, people.get(oldID));
		people.remove(oldID);
		System.out.println("Changed the " + oldID + " ID to " +  newID + " in the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}
	/*Map.Entry<String, String> getEntry(String name)
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
	}*/
	synchronized boolean setName(String id, String name)
	{
		if (!people.containsKey(id) || people.get(id).name.equals(name))
			return false;
		time = System.currentTimeMillis();
		people.get(id).name = name;
		System.out.println("Set " + name + " name for the ID " + id + " in the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}
	synchronized void adjustNames(Collection<User> users)
	{
		System.out.println("Adjusting names in the database...");
		for (User user: users)
			setName(user.getId(), user.getName());
		SortedMap<String, Person> UnIDedPeople;
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			UnIDedPeople = people.headMap(firstID, false);
		else
			UnIDedPeople = people;
		for (Map.Entry<String, Person> unIDedEntry: UnIDedPeople.entrySet())
		{
			User unIDedUser = Iterables.find(users, (x) -> (x.getName().equalsIgnoreCase(unIDedEntry.getValue().name)), null);
			if (unIDedUser != null)
				if (findIDedEntry(unIDedEntry.getValue().name) != null)
					removeEntry(unIDedEntry.getKey());
				else
					changeID(unIDedEntry.getKey(), unIDedUser.getId());	
		}
	}
	/*boolean setIntro(String id, String intro)
	{
		if (!people.containsKey(id))
			return false;
		time = System.currentTimeMillis();
		if (intro.isEmpty())
			people.remove(id);
		else
			people.get(id).intro = intro;
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}*/
	synchronized void setNameAndIntro(String id, String name, String intro)
	{
		time = System.currentTimeMillis();
		if (intro.isEmpty())
		{
			people.remove(id); //TODO change this behavior when more fields are added to the DB
			System.out.println("Removed " + id + " entry from the database.");
		}
		else
		{
			people.put(id, new Person(name, intro));
			System.out.println("Set the " + name + " name and " + intro + " intro for the ID " + id + " in the database.");
		}
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	synchronized void setGame(String game)
	{
		time = System.currentTimeMillis();
		this.game = game;
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	public String convert(DiscordAPI api) { //dummy
		return toString();
	}
}