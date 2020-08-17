package main.java.me.kagerou.kyoukobot;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
//maintains the database which stores the introductions and alarms for users and is saved to the people.txt file 
//TODO save the database every X minutes instead of doing it all the time??
class DataBase //TODO: cloud backup??
{ //the class with all the needed information about users (except for their ids)
	class Person
	{
		String name;
		String intro;
		boolean subscribed = false;
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
		public boolean isEmpty()
		{ //if a record is empty, it should be removed
			return intro.isEmpty() && alarms.isEmpty() && !subscribed;
		}
	}
	
	long time; //time of latest DB update, it's there for the potential cloud backup feature
	final static String defaultGame = "!k help | k!proj Database"; 
	String game; //the game bot's playing
	TreeMap<String, Person> people; //user-related data
	final static String dailyAlarmMessage = " :alarm_clock: **Time to farm daily credits!**";
	final static String repAlarmMessage = " :alarm_clock: **Time to give a daily reputation point!**";
	DataBase()
	{
		time = 0;
		game = defaultGame;
		people = new TreeMap<String, Person>();
	}
	synchronized void defaultFill(String FileName)
	{ //i used to put default intros for a few users there
		time = 0;
		game = defaultGame;
		SaveToFile(FileName);
	}
	//reads the DB from file, returns false in case of failure
	//format: {"time": <time(long)>, "game": <game(String)>, "intros": {<users(JSON)>}}
	//format for each user: "<id>": {"intro": <intro(String)>, "name": <username(String)>[, "sub": true][, "tasks": <tasks(JSON)>]}
	//format for each task (aka alarm): "<message>": <time(long)>
	//if a user has no intro, an empty string is stored
	synchronized boolean readFromFile(String FileName)
	{
		time = 0;
		game = defaultGame;
		people.clear();
		JSONObject json;
		try (FileInputStream fis = new FileInputStream(FileName)) {
			json = new JSONObject(IOUtils.toString(fis, Charset.forName("UTF-8"))); //should be UTF-16??
			if (json.has("time"))
				time = json.getLong("time");
			if (json.has("game"))
				game = json.getString("game");
			if (json.has("intros"))
			{
				JSONObject intros = json.getJSONObject("intros");
				for (Object id: intros.keySet())
				{
					JSONObject personJSON = intros.getJSONObject((String) id);
					Person person = new Person(personJSON.getString("name"), personJSON.getString("intro"));
					if (personJSON.has("sub"))
						person.subscribed = true;
					people.put((String) id, person);
					if (personJSON.has("tasks")) //register the tasks (alarms)
						for (Object taskName: personJSON.getJSONObject("tasks").keySet())
							registerReminder((String) id, personJSON.getString("name"), (String) taskName, personJSON.getJSONObject("tasks").getLong((String) taskName), KyoukoBot.timer, false);
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
	//converts the DB to JSONObject, see the format above
	//maybe i should be using GSON for this
	synchronized JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("game", game);
		JSONObject intros_json = new JSONObject();
		for (Map.Entry<String, Person> person: people.entrySet())
		{
			JSONObject person_json = new JSONObject().put("name", person.getValue().name).put("intro", person.getValue().intro);
			if (person.getValue().subscribed)
				person_json.put("sub", true); //if not subbed, then not even putting false to save bytes, lol
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
	//saves the DB to file using the JSON conversion, returns false in case of failure
	boolean SaveToFile(String FileName)
	{
		time = System.currentTimeMillis();
		try {
			FileUtils.writeStringToFile(new File(FileName), toString(), Charset.forName("UTF-8"));
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
	//gets the user data by their ID
	synchronized Person get(String id)
	{
		return people.get(id);
	}
	//returns a submap of people containing UnIDed entries (the ones with IDs starting with '-')
	private SortedMap<String, Person> getUnIDedMap()
	{
		//IDs are ordered lexicographically by default, and unIDed entries have IDs starting with '-'; '-' < '0'
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			return people.headMap(firstID, false);
		else
			return people;
	}
	//returns a submap of people containing IDed entries (the ones with IDs starting with digits)
	private SortedMap<String, Person> getIDedMap()
	{
		String firstID = people.ceilingKey("0");
		if (firstID != null)
			return people.tailMap(firstID);
		else
			return new TreeMap<String, Person>(people.comparator());
	}
	//find an unIDed entry by username (strict matching, ignoring case)
	synchronized Map.Entry<String, Person> findUnIDedEntry(String name)
	{
		return Iterables.find(getUnIDedMap().entrySet(), (x) -> x.getValue().name.equalsIgnoreCase(name), null);
	}
	//find an IDed entry by username (strict matching, ignoring case)
	synchronized Map.Entry<String, Person> findIDedEntry(String name)
	{
		return Iterables.find(getIDedMap().entrySet(), (x) -> x.getValue().name.equalsIgnoreCase(name), null);
	}
	//find an entry by username, prioritising IDed ones, (strict matching, ignoring case)
	synchronized Map.Entry<String, Person> findEntry(String name) 
	{ //RIP efficiency
		Map.Entry<String, Person> result;
		if ((result = findIDedEntry(name)) != null)
			return result;
		return findUnIDedEntry(name);
	}
	//finds an entry with non-empty intro and the name containing the argument (case insensitive)
	//priority list:
	//1. name matches perfectly, IDed entry
	//2. name matches perfectly, unIDed entry
	//3. name starts with the argument, IDed entry
	//4. name starts with the argument, unIDed entry
	//5. name contains the argument, IDed entry
	//6. name contains the argument, unIDed entry
	synchronized Map.Entry<String, Person> findPartialEntryWithIntro(String name)
	{
		String lower_name = name.toLowerCase();
		Set<Map.Entry<String, Person>> UnIDedPeople = getUnIDedMap().entrySet();
		Set<Map.Entry<String, Person>> IDedPeople = getIDedMap().entrySet();
		
		Map.Entry<String, Person> result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.equalsIgnoreCase(lower_name) && !x.getValue().intro.isEmpty(), null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.equalsIgnoreCase(lower_name) && !x.getValue().intro.isEmpty(), null)) != null)
			return result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.toLowerCase().startsWith(lower_name) && !x.getValue().intro.isEmpty() , null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.toLowerCase().startsWith(lower_name) && !x.getValue().intro.isEmpty(), null)) != null)
			return result;
		if ((result = Iterables.find(IDedPeople, (x) -> x.getValue().name.toLowerCase().contains(lower_name) && !x.getValue().intro.isEmpty(), null)) != null)
			return result;
		if ((result = Iterables.find(UnIDedPeople, (x) -> x.getValue().name.toLowerCase().contains(lower_name) && !x.getValue().intro.isEmpty(), null)) != null)
			return result;
		return null;
	}
	//returns the first free negative ID in the map
	synchronized String firstNegativeID()
	{
		String result;
		for (int i = 1; people.containsKey(result = "-" + i); i++);
		return result;
	}
	//removes an entry with a specified id
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
	//change ID for the entry, needed to fix unIDed ones
	synchronized boolean changeID(String oldID, String newID)
	{
		if (!people.containsKey(oldID))
			return false;
		people.put(newID, people.get(oldID));
		people.remove(oldID);
		System.out.println("Changed the " + oldID + " ID to " +  newID + " in the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}
	//sets username for an entry with a specified id
	synchronized boolean setName(String id, String name)
	{
		if (!people.containsKey(id) || people.get(id).name.equals(name))
			return false;
		people.get(id).name = name;
		System.out.println("Set " + name + " name for the ID " + id + " in the database.");
		SaveToFile(KyoukoBot.DatabaseFile);
		return true;
	}
	//adjust the names using the collection of known users
	//done after the bot's launch to catch up with all the username changes
	synchronized void adjustNames(Collection<User> users)
	{
		System.out.println("Adjusting names in the database...");
		for (User user: users) //easy part: just fix the IDed entries
			setName(user.getId(), user.getName());
		for (Map.Entry<String, Person> unIDedEntry: getUnIDedMap().entrySet())
		{ //for every unIDed entry, if there is a user in the collection with the same name, fix the entry
			User unIDedUser = Iterables.find(users, (x) -> (x.getName().equalsIgnoreCase(unIDedEntry.getValue().name)), null);
			if (unIDedUser != null)
				if (findIDedEntry(unIDedEntry.getValue().name) != null)
					removeEntry(unIDedEntry.getKey()); //if there is an IDed entry with the same name, just remove one without the ID
				else
					changeID(unIDedEntry.getKey(), unIDedUser.getId());	//otherwise, give the entry a proper ID
		}
	}
	//sets username and intro for an entry with a specified id, creates the entry if it doesn't exist
	synchronized void setNameAndIntro(String id, String name, String intro)
	{
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
	//the next two functions register an alarm for a user at the time alarmTime (Unix timestamp) using the specified timer
	//saveToFile is needed to distinguish registering a new alarm for a user from reading one from file (can't save when you're in the middle of reading a database from the same file)
	synchronized void registerReminder(User user, String msg, long alarmTime, Timer timer, boolean saveToFile)
	{
		registerReminder(user.getId(), user.getName(), msg, alarmTime, timer, saveToFile);
	}
	synchronized void registerReminder(String id, String name, String msg, long alarmTime, Timer timer, boolean saveToFile) {
		final long tooLate = 24 * 60 * 60 * 1000; //ms in a day
		if (alarmTime <= System.currentTimeMillis() - tooLate) //the alarm is outdated, don't bother with it
		{
			System.out.println("Alarm " + msg + " for the user " + name + " (" + id + ") is discarded due to being oudated.");
			if (saveToFile)
				SaveToFile(KyoukoBot.DatabaseFile);
			return;
		}
		alarmTime = Math.max(alarmTime, System.currentTimeMillis());
		RemindTask task = new RemindTask(id, KyoukoBot.api, msg, alarmTime, this); //creating the actual task
		Person person; //finding or creating an entry in the database for this alarm
		if (!people.containsKey(id))
		{
			person = new Person(name, "");
			people.put(id, person);
		}
		else
			person = people.get(id);
		if (person.alarms.containsKey(msg))
			person.alarms.get(msg).cancel(); //cancel the old task if the one with the same message is registered
		person.alarms.put(msg, task); //associate the task with the person
		if (saveToFile)
			SaveToFile(KyoukoBot.DatabaseFile);
		System.out.println("Registered a reminder for the user " + id + " at time " + alarmTime + ".");
		timer.schedule(task, Math.max(alarmTime - System.currentTimeMillis(), 0)); //actual scheduling; probably should be using Date as a parameter??
	}
	//refreshes an alarm, used if the message couldn't be sent
	//not actually sure if this function is needed, the only difference from registerReminder is the sanity check
	synchronized boolean refreshReminder(RemindTask task, Timer timer)
	{
		String id = task.getId();
		String message = task.getMessage();
		if (!people.containsKey(id) || !people.get(id).alarms.containsKey(message) || (people.get(id).alarms.get(message) != task)) //a bit of a sanity check
			return false;
		registerReminder(id, "", message, task.getTime(), timer, true);
		return true;
	}
	//removes an alarm from the user's entry and cancels it
	synchronized boolean removeReminder(RemindTask task)
	{
		if (task == null)
			return false;
		String id = task.getId();
		String message = task.getMessage();
		if (!people.containsKey(id) || !people.get(id).alarms.containsKey(message)) //sanity check
			return false;
		people.get(id).alarms.remove(message);
		task.cancel();
		if (people.get(id).isEmpty())
			people.remove(id);
		SaveToFile(KyoukoBot.DatabaseFile);
		System.out.println("Removed a reminder for user " + id + ".");
		return true;
	}
	//the next functions are working with Tatsumaki-related alarms, the t!daily and t!rep ones
	
	//returns time left until user's next t!daily alarm
	synchronized long getDailyDelay(User user) {
		return getTimeLeft(user.getId(), dailyAlarmMessage);
	}
	//returns time left until user's next t!rep alarm
	synchronized long getRepDelay(User user) {
		return getTimeLeft(user.getId(), repAlarmMessage);
	}
	//sets a t!daily alarm for a user in delay ms
	synchronized boolean setDailyDelay(User user, long delay, Timer timer)
	{
		Person person = people.get(user.getId());
		if ((person == null) || !person.subscribed)
			return false;
		registerReminder(user, dailyAlarmMessage, System.currentTimeMillis() + delay, timer, true); //not the best way to use the delay...
		return true;
	}
	//sets a t!rep alarm for a user in delay ms
	synchronized boolean setRepDelay(User user, long delay, Timer timer)
	{
		Person person = people.get(user.getId());
		if ((person == null) || !person.subscribed)
			return false;
		registerReminder(user, repAlarmMessage, System.currentTimeMillis() + delay, timer, true); //not the best way to use the delay...
		return true;
	}
	//returns time left until next alarm with the message alarmMessage, -1 if the user isn't in the DB or the alarm can't be found
	synchronized long getTimeLeft(String id, String alarmMessage) {
		if (!people.containsKey(id))
			return -1;
		RemindTask task = people.get(id).alarms.get(alarmMessage);
		if (task == null)
			return -1;
		return Math.max(task.getTime() - System.currentTimeMillis(), 0);
	}
	//checks if the user is subbed to Tatsumaki-related reminders
	synchronized boolean isSubscribed(User user)
	{
		return people.containsKey(user.getId()) && people.get(user.getId()).subscribed;
	}
	//subscribes a user to Tatsumaki-related reminders
	synchronized boolean subscribe(User user)
	{
		String id = user.getId();
		if (!people.containsKey(id)) //find or create an entry for the user
			people.put(id, new Person());
		Person person = people.get(id);
		if (person.subscribed)
			return false;
		person.subscribed = true;
		SaveToFile(KyoukoBot.DatabaseFile);
		System.out.println("Subscribed user " + id + " to daily reminders.");
		return true;
	}
	//unsubscribes a user from Tatsumaki-related reminders
	synchronized boolean unsubscribe(User user)
	{
		String id = user.getId();	
		if (!people.containsKey(id))
			return false;
		Person person = people.get(id);
		if (!person.subscribed)
			return false;
		person.subscribed = false;
		if (!removeReminder(person.alarms.get(dailyAlarmMessage)) & !removeReminder(person.alarms.get(repAlarmMessage))) //yes, & and not &&, i want to remove both
			SaveToFile(KyoukoBot.DatabaseFile); //you only need to save if there were no relevant alarms, otherwise RemoveReminder does it for you
		System.out.println("Subscribed user " + id + " to daily reminders.");
		return true;
	}
	//memorises a game in the database (doesn't actually set it for the bot)
	synchronized void setGame(String game)
	{
		this.game = game;
		SaveToFile(KyoukoBot.DatabaseFile);
	}
	public String convert(DiscordAPI api) { //was supposed to convert an old format DB to the new format, ignore this one
		return toString();
	}
}