package me.kagerou.kyoukobot;

import java.util.Map;
import java.util.TreeMap;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

public class HelpCommand implements CommandExecutor {
	private final CommandHandler handler;
	
	HelpCommand(CommandHandler handler)
	{
		this.handler = handler;
	}
	
	 @Command(aliases = {"k!help", "k!commands"}, description = "Shows this page.")
	    public String onHelpCommand(Message message) {
	        StringBuilder builder = new StringBuilder();
	        builder.append("```xml");
	        TreeMap<String, String> commands = new TreeMap<String, String>((x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
	        int max_usage_length = 0;
	        for (CommandHandler.SimpleCommand simpleCommand: handler.getCommands()) {
	            if (!simpleCommand.getCommandAnnotation().showInHelpPage()) {
	                continue; // skip command
	            }
	            String usage = simpleCommand.getCommandAnnotation().usage();
	            if (usage.isEmpty()) // no usage provided, using the first alias
	                usage = simpleCommand.getCommandAnnotation().aliases()[0];
	            if (!simpleCommand.getCommandAnnotation().requiresMention())
	                usage = handler.getDefaultPrefix() + usage; // the default prefix only works if the command does not require a mention
	            if (max_usage_length < usage.length())
	            	max_usage_length = usage.length();
	            String description = simpleCommand.getCommandAnnotation().description();
	            if (description.equals("none"))
	            	description = "";
	            commands.put(usage, description);
	        }
	        for (Map.Entry<String, String> command: commands.entrySet())
	        {
	        	builder.append('\n').append(command.getKey());
	        	for (int i = command.getKey().length(); i <= max_usage_length; i++)
	        		builder.append(' ');
	        	builder.append("| ").append(command.getValue());
	        }
	        builder.append("```\n");
	        builder.append("`Also, I can recognise most popular Twitch emotes and post them for you~`");
	        /*if (!message.isPrivateMessage())
	        	for (Channel channel: message.getChannelReceiver().getServer().getChannels())
	        		if (channel.getName().equalsIgnoreCase("recordings"))
	        			builder.append("`Also, I repost all the clyp.it links into` " + channel.getMentionTag() + " `channel.`");*/
	        return builder.toString();
	    }
}
