package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class HugCommand implements CommandExecutor {
	@Command(aliases = {"k!hug"}, description = "Hugs a user over TCP/IP! You can get a hug too~", usage = "k!hug [name]"/*"k!hug [username|random]"*/)
    public void onCommand(DiscordAPI api, Message message, Server server, String args[])
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
        		String arg = message.getContent().split(" ", 2)[1].toLowerCase().trim();
        		target = KyoukoBot.findUserOnServer(arg, server, message.getAuthor());
        	}
        		/*Collection<User> users;
        		if (server != null)
        			users = message.getChannelReceiver().getServer().getMembers();
       			else
       			{
       				users = new ArrayList<User>();
       				users.add(message.getAuthor());
       				users.add(api.getYourself());
       			}
        		if (server != null)
           			for (User user: users)
            			if ((user.getNickname(server) != null) && (user.getNickname(server).equalsIgnoreCase(arg)))
            			{
            				target = user;
           					break;
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
        		/*if (target == null)
        			for (User user: users)
        				if (user.getName().equalsIgnoreCase(arg))
        				{
        					target = user;
        					break;
        				}
       			if ((target == null) && (server != null))
       				for (User user: users)
       					if ((user.getNickname(server) != null) && (user.getNickname(server).toLowerCase().startsWith(arg)))
       					{
       						target = user;
       						break;
       					}
       			if (target == null)
       				for (User user: users)
       					if (user.getName().toLowerCase().startsWith(arg))
       					{
       						target = user;
       						break;
       					}
       		}*/
        if (target == null)
    		who = "Couldn't find them, have your hug back >_<\n" + who + message.getAuthor().getMentionTag() + "*";
        else
        	if (target.isYourself())
        		who += "herself >_<*"; //change this to something else??
        	else
        		who += target.getMentionTag() + "*";
        message.reply(who);
	}
}
