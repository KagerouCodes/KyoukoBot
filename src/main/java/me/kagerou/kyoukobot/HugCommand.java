package me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Collection;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class HugCommand implements CommandExecutor {
	@Command(aliases = {"k!hug"}, description = "Hugs a user over TCP/IP!", usage = "k!hug [username]"/*"k!hug [username|random]"*/)
    public void onCommand(DiscordAPI api, Message message, String args[])
	{
        String who = "*hugs ";
        User target = null;
        if (args.length == 0)
        	target = message.getAuthor();
        else
        	if (!message.getMentions().isEmpty())
        		target = message.getMentions().get(0);
        	else
        	{
        		String arg = message.getContent().substring(message.getContent().indexOf(' ') + 1).toLowerCase().trim();
        		Collection<User> users;
        		if (!message.isPrivateMessage())
        			users = message.getChannelReceiver().getServer().getMembers();
       			else
       			{
       				users = new ArrayList<User>();
       				users.add(message.getAuthor());
       				users.add(api.getYourself());
       			}
        		/*if (arg.equals("random"))
        		{
        			ArrayList<User> online_users = new ArrayList<User>();
        			for (User user: users)
        				if (user.getStatus() == UserStatus.ONLINE)
        					online_users.add(user);
        			if (online_users.isEmpty())
        				online_users.add(message.getAuthor());
        			target = online_users.get(new Random().nextInt(online_users.size()));
        		}*/
       			for (User user: users)
       			{
       				//System.out.println((user.getName()));
        			if (user.getName().equalsIgnoreCase(args[0]))
        			{
        				target = user;
        				//who += user.getMentionTag() + "*";
       					break;
       				}
       			}
       			if (target == null)
       				for (User user: users)
       					if (user.getName().toLowerCase().startsWith(arg))
       					{
       						target = user;
       						break;
       					}
       		}
        if (target == null)
        {
        	//message.reply("Couldn't find them, have your hug back >_<");
    		who = "Couldn't find them, have your hug back >_<\n" + who + message.getAuthor().getMentionTag() + "*";
    	}
        else
        	if (target.isYourself())
        		who += "herself >_<*";
        	else
        		who += target.getMentionTag() + "*";
        message.reply(who);
	}
}
