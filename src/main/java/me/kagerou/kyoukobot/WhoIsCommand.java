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
//displays introductions for a single user or all the online ones
public class WhoIsCommand implements CommandExecutor {
	@Command(aliases = {"k!who", "k!whois"}, description = "Lets you look up your or other user's short info which is set up with k!setintro.\nk!who all lists info of all online users on the server.", usage = "k!who [name|everyone|all]")
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
	{
        User target = null;
        String name = KyoukoBot.getArgument(message);
        if (name.isEmpty()) //grab the intro of the person using the command if there's no argument
        	target = message.getAuthor();
        else
        	if (!message.getMentions().isEmpty()) //just grab the mention if there is one
        		target = message.getMentions().get(0);
        if (target != null)
        	name = target.getName();
        if (name.equalsIgnoreCase("Rem"))
        { //an easter egg; inb4 a real user with this name shows themselves
        	message.reply("Who is Rem? :thinking:");
        	return;
        }
        if (!name.equalsIgnoreCase("everyone") && !name.equalsIgnoreCase("all"))
        { //need to find a single user by their name
        	if (target != null)
        	{ //if user's already identified, show their intro
        		NewDataBase.Person person = KyoukoBot.Database.get(target.getId());
    			if (person == null)
    			{
    				Map.Entry<String, NewDataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(target.getName());
    				if (unIDedEntry != null)
    				{ //if there's an unIDed entry with the needed username, use it and give it the proper ID
    					KyoukoBot.Database.changeID(unIDedEntry.getKey(), target.getId());
    					person = unIDedEntry.getValue();
    				}
    			}
    			if ((person != null) && !person.intro.isEmpty()) //the actual output
    				message.reply("**" + KyoukoBot.getNickname(target, message.getReceiver()) + ":** " + person.intro);
    			else
    				message.reply("I-I don't know **" + name + "** yet.");
    			return;
        	}
        	//if user not found yet, try to find them on the server
        	List<User> usersByName = KyoukoBot.findUsersOnServer(name, server, message.getAuthor());
        	target = Iterables.find(usersByName, (x) -> ((KyoukoBot.Database.get(x.getId()) != null) && !KyoukoBot.Database.get(x.getId()).intro.isEmpty()), null);
        	if (target != null)
        	{
        		message.reply("**" + KyoukoBot.getNickname(target, message.getReceiver()) + ":** " + KyoukoBot.Database.get(target.getId()).intro);
        		return;
        	}
        	Map.Entry<String, NewDataBase.Person> entry = KyoukoBot.Database.findPartialEntryWithIntro(name); //if the user's not found on the server, search for the name in the DB TODO find an entry with an actual intro!
        	if (entry != null)
        		message.reply("**" + entry.getValue().name + ":** " + entry.getValue().intro);
        	else
        		message.reply("I-I don't know **" + name + "** yet.");
        }
        else
        { //introduce all the online users on the server; TODO make it introduce only more active/recent users??
        	//resulting list of users with introductions, sorts itself by the name
        	TreeMap<String, String> result = new TreeMap<String, String> ((x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
        	Collection<User> users;
        	if (!message.isPrivateMessage())
        		users = message.getChannelReceiver().getServer().getMembers();
        	else
        	{ //if it's a private message, just include the author in the user list
        		users = new ArrayList<User>();
        		users.add(message.getAuthor());
        	}
        	for (User user: users)
        	{
        		if (!user.isBot() && (user.getStatus() != UserStatus.OFFLINE))
        		{
        			NewDataBase.Person person = KyoukoBot.Database.get(user.getId());
        			if (person == null)
        			{ //if there's no entry with the user's ID, try to find an unIDed one and fix it
        				Map.Entry<String, NewDataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(user.getName());
        				if (unIDedEntry != null)
        				{
        					KyoukoBot.Database.changeID(unIDedEntry.getKey(), user.getId());
        					person = unIDedEntry.getValue();
        				}
        			}
        			if ((person != null) && !person.intro.isEmpty())
        				result.put(KyoukoBot.getNickname(user, message.getReceiver()), person.intro);
        		}
        	}
        	String output = new String();
        	boolean too_long = false; 
        	for (Map.Entry<String, String> entry: result.entrySet())
        	{
        		String str = "**" + entry.getKey() + ":** " + KyoukoBot.wrapLinks(entry.getValue());
        		if (output.length() + str.length() > KyoukoBot.CharLimit)
        		{ //break the message into multiple ones if it's too long, also, send it via DM
        			if (!too_long)
        			{
        				too_long = true;
        				message.reply("`User list is too long, sending it via DM.`");
        			}
        			//message.reply(output);
        			message.getAuthor().sendMessage(output);
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
        	if (!too_long)
        		message.reply(output);
        	else
        		message.getAuthor().sendMessage(output);
        }
	}
}
