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
	
	 @Command(aliases = {"k!help", "k!commands"}, description = "Shows this page.") //TODO shorten this one
	    public void onHelpCommand(Message message) {
	        StringBuilder builder = new StringBuilder();
	        //builder.append("```xml");
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
	        	StringBuilder newline = new StringBuilder();
	        	newline.append('\n').append(command.getKey());
	        	for (int i = command.getKey().length(); i <= max_usage_length; i++)
	        		newline.append(' ');
	        	newline.append("| ").append(command.getValue());
	        	if (builder.length() + newline.length() > KyoukoBot.CharLimit)
	        	{
	        		builder.insert(0, "```xml");
	        		builder.append("```");
	        		message.reply(builder.toString());
	        		builder.setLength(0);
					try {
						Thread.sleep(500); //gotta guarantee the correct order
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
	        	}
	        	builder.append(newline);
	        }
	        if (builder.length() > 0)
	        {
	        	builder.insert(0, "```xml");
        		builder.append("```\n");
	        }
	        //builder.append("```\n");
	        builder.append("`Also, I can recognise most popular Twitch emotes and post them for you~`");
	        message.reply(builder.toString());
	    }
}
