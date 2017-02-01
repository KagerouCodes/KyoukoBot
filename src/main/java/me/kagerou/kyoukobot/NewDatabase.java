package me.kagerou.kyoukobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
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
		TreeMap<String, RemindTask> alarms = new TreeMap<String, RemindTask>();
		Person(String name, String intro)
		{
			this.name = name;
			this.intro = intro;
		}
		Person()
		{
			this("", "");
		}
		public boolean isEmpty() {
			return intro.isEmpty() && alarms.isEmpty();
		}
	}
	
	long time;
	final static String defaultGame = "!k help | k!proj Database"; 
	String game;
	TreeMap<String, Person> people;
	NewDataBase()
	{
		time = 0;
		game = defaultGame;
		people = new TreeMap<String, Person>();
	}
	synchronized void defaultFill(String FileName)
	{
		time = 0;
		game = defaultGame;
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
				{
					JSONObject person = intros.getJSONObject((String) id);
					people.put((String) id, new Person(person.getString("name"), person.getString("intro")));
					if (person.has("tasks"))
						for (Object taskName: person.getJSONObject("tasks").keySet())
							registerReminder((String) id, person.getString("name"), (String) taskName, person.getJSONObject("tasks").getLong((String) taskName), KyoukoBot.timer);
				}
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
	synchronized JSONObject toJSONObject() //TODO incorporate tasks
	{
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("game", game);
		JSONObject intros_json = new JSONObject();
		for (Map.Entry<String, Person> person: people.entrySet())
		{
			JSONObject person_json = new JSONObject().put("name", person.getValue().name).put("intro", person.getValue().intro);
			if (!person.getValue().alarms.isEmpty())
			{
				JSONObject tasks_json = new JSONObject();
				for (Map.Entry<String, RemindTask> taskEntry: person.getValue().alarms.entrySet())
					tasks_json.put(taskEntry.getKey(), taskEntry.getValue().getTime());
				person_json.put("tasks", tasks_json);
			}
			intros_json.put(person.getKey(), person_json);
		}
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
		for (RemindTask task: people.get(id).alarms.values())
			task.cancel();
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
		if (!people.containsKey(id))
			people.put(id, new Person());
		Person person = people.get(id);
		person.name = name;
		person.intro = intro;
		if (person.isEmpty())
		{
			people.remove(id);
			System.out.println("Removed " + id + " entry from the database.");
		}
		else
			System.out.println("Set the " + name + " name and " + intro + " intro for the ID " + id + " in the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	synchronized void registerReminder(User user, String msg, long alarmTime, Timer timer)
	{
		registerReminder(user.getId(), user.getName(), msg, alarmTime, timer);
	}
	synchronized void registerReminder(String id, String name, String msg, long alarmTime, Timer timer) {
		alarmTime = Math.max(alarmTime, System.currentTimeMillis());
		RemindTask task = new RemindTask(id, KyoukoBot.api, msg, alarmTime, this);
		Person person;
		if (!people.containsKey(id))
		{
			person = new Person(name, "");
			people.put(id, person);
		}
		else
			person = people.get(id);
		if (person.alarms.containsKey(msg))
			person.alarms.get(msg).cancel();
		person.alarms.put(msg, task);
		SaveToFile(KyoukoBot.DatabaseFile);
		System.out.println("Registered a reminder for the user " + id + ".");
		timer.schedule(task, Math.max(alarmTime - System.currentTimeMillis(), 0));
	}
	synchronized boolean refreshReminder(RemindTask task, Timer timer)
	{
		String id = task.getId();
		String message = task.getMessage();
		if (!people.containsKey(id) || !people.get(id).alarms.containsKey(message) || (people.get(id).alarms.get(message) != task)) //a bit of a sanity check
			return false;
		registerReminder(id, "", message, task.getTime(), timer);
		return true;
	}
	synchronized boolean removeReminder(RemindTask task)
	{
		String id = task.getId();
		String message = task.getMessage();
		if (!people.containsKey(id) || !people.get(id).alarms.containsKey(message))
			return false;
		people.get(id).alarms.remove(message);
		if (people.get(id).isEmpty())
			people.remove(id);
		SaveToFile(KyoukoBot.DatabaseFile);
		System.out.println("Removed a reminder for user " + id + ".");
		return true;
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