package me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.UserStatus;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class WhoIsCommand implements CommandExecutor {
	@Command(aliases = {"k!who", "k!whois"}, description = "Introduces a user or the entire chat room.", usage = "k!who[is] [username|everyone]")
    public void onCommand(DiscordAPI api, Message message, String args[])
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
        	message.reply("Who is Rem? :thinking:");
        if (!name.equalsIgnoreCase("everyone") && !name.equalsIgnoreCase("all")) //TODO break up into several messages
        {
        	Map.Entry<String, String> result = KyoukoBot.Database.getEntry(name);
        	if ((result == null) && (target == null) && (!message.isPrivateMessage()))
        		result = KyoukoBot.Database.getPartialEntryOnServer(name, message.getChannelReceiver().getServer());
        	if ((result == null) && (target == null))
        		result = KyoukoBot.Database.getPartialEntry(name);
        	if (result == null)
        		if (!name.equalsIgnoreCase(message.getAuthor().getName()))
        			message.reply("I-I don't know **" + name + "** yet.");
        		else
        			message.reply("I-I don't know you, **" + message.getAuthor().getName() + "**, yet. Use k!intro command to introduce yourself.");
        	message.reply("**" + result.getKey() + ":** " + result.getValue());
        }
        else
        { //TODO break into 2000 character long posts
        	ArrayList<String> list = new ArrayList<String>();
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
        				list.add("**" + result.getKey() + ":** " + result.getValue());
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
        	message.reply(output);
        }
	}
}
