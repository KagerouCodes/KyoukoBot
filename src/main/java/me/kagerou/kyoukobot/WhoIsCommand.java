package me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.UserStatus;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class WhoIsCommand implements CommandExecutor {
	@Command(aliases = {"k!who", "k!whois"}, description = "Introduces a user or the entire chat room.", usage = "k!who[is] [username|everyone]")
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
	{
        User target = null;
        if (args.length == 0)
        	target = message.getAuthor();
        else
        	if (!message.getMentions().isEmpty())
        		target = message.getMentions().get(0);
        String name;
        if (target != null)
        	name = target.getName();
        else
        	name = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim();
        if (name.equalsIgnoreCase("Rem"))
        {
        	message.reply("Who is Rem? :thinking:");
        	return;
        }
        if (!name.equalsIgnoreCase("everyone") && !name.equalsIgnoreCase("all"))
        {
        	if (target != null)
        	{
        		NewDataBase.Person person = KyoukoBot.Database.get(target.getId());
    			if (person == null)
    			{
    				Map.Entry<String, NewDataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(target.getName());
    				if (unIDedEntry != null)
    				{
    					KyoukoBot.Database.changeID(unIDedEntry.getKey(), target.getId());
    					person = unIDedEntry.getValue();
    				}
    			}
    			if (person != null)
    				message.reply("**" + KyoukoBot.getNickname(target, message.getReceiver()) + ":** " + KyoukoBot.wrapLinks(person.intro));
    			else
    				message.reply("I-I don't know **" + name + "** yet.");
    			return;
        	}
        	List<User> usersByName = KyoukoBot.findUsersOnServer(name, server, message.getAuthor());
        	if ((target = Iterables.find(usersByName, (x) -> (KyoukoBot.Database.get(x.getId()) != null), null)) != null)
        	{
        		message.reply("**" + KyoukoBot.getNickname(target, message.getReceiver()) + ":** " + KyoukoBot.wrapLinks(KyoukoBot.Database.get(target.getId()).intro));
        		return;
        	}
        	Map.Entry<String, NewDataBase.Person> entry = KyoukoBot.Database.findPartialEntry(name);
        	if (entry != null)
        		message.reply("**" + entry.getValue().name + ":** " + KyoukoBot.wrapLinks(entry.getValue().intro));
        	else
        		message.reply("I-I don't know **" + name + "** yet.");
        	/*Map.Entry<String, String> result = KyoukoBot.Database.getEntry(name);
        	if ((result == null) && (target == null) && (!message.isPrivateMessage()))
        		result = KyoukoBot.Database.getPartialEntryOnServer(name, message.getChannelReceiver().getServer());
        	if ((result == null) && (target == null))
        		result = KyoukoBot.Database.getPartialEntry(name);
        	if (result == null)
        		if (!name.equalsIgnoreCase(message.getAuthor().getName()))
        		{
        			message.reply("I-I don't know **" + name + "** yet.");
        			return;
        		}
        		else
        		{
        			message.reply("I-I don't know you, **" + message.getAuthor().getName() + "**, yet. Use k!intro command to introduce yourself.");
        			return;
        		}
        	message.reply("**" + result.getKey() + ":** " + result.getValue());*/
        }
        else
        { //TODO make it introduce only more active/recent users??
        	TreeMap<String, String> result = new TreeMap<String, String> ((x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
        	Collection<User> users;
        	if (!message.isPrivateMessage())
        		users = message.getChannelReceiver().getServer().getMembers();
        	else
        	{
        		users = new ArrayList<User>();
        		users.add(message.getAuthor());
        	}
        	for (User user: users)
        	{
        		if (!user.isBot() && (user.getStatus() != UserStatus.OFFLINE))
        		{
        			NewDataBase.Person person = KyoukoBot.Database.get(user.getId());
        			if (person == null)
        			{
        				Map.Entry<String, NewDataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(user.getName());
        				if (unIDedEntry != null)
        				{
        					KyoukoBot.Database.changeID(unIDedEntry.getKey(), user.getId());
        					person = unIDedEntry.getValue();
        				}
        			}
        			if (person != null)
        				result.put(KyoukoBot.getNickname(user, message.getReceiver()), person.intro);
        		}
        	}
        	String output = new String();
        	for (Map.Entry<String, String> entry: result.entrySet())
        	{
        		String str = "**" + entry.getKey() + ":** " + KyoukoBot.wrapLinks(entry.getValue());
        		if (output.length() + str.length() > KyoukoBot.CharLimit)
        		{
        			message.reply(output);
        			output = "";
        			try {
    					Thread.sleep(500); //gotta guarantee the correct order
    				}
    				catch (InterruptedException e)
    				{
    					e.printStackTrace();
    				}
        		}
        		output += str + '\n';
        	}
        	if (output.isEmpty())
            	output = "I-I don't know anyone here...";
        	message.reply(output);
        	/*ArrayList<String> list = new ArrayList<String>();
        	Collection<User> users;
        	if (!message.isPrivateMessage())
        		users = message.getChannelReceiver().getServer().getMembers();
        	else
        	{
        		users = new ArrayList<User>();
        		users.add(message.getAuthor());
        	}
        	for (User user: users)
        	{
        		if (!user.isBot() && (user.getStatus() != UserStatus.OFFLINE))
        		{
        			Map.Entry<String, String> result = KyoukoBot.Database.getEntry(user.getName());
        			if (result != null)
        				list.add("**" + result.getKey() + ":** " + KyoukoBot.wrapLinks(result.getValue()));
        		}
        	}
        	Collections.sort(list, (x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
        	String output = new String();
        	for (String str: list)
        	{
        		if (output.length() + str.length() > KyoukoBot.CharLimit)
        		{
        			message.reply(output);
        			output = "";
        			try {
    					Thread.sleep(500); //gotta guarantee the correct order
    				}
    				catch (InterruptedException e)
    				{
    					e.printStackTrace();
    				}
        		}
        		output += str + '\n';
        	}
        	if (output.isEmpty())
            	output = "I-I don't know anyone here...";
        	message.reply(output);*/
        }
	}
}
