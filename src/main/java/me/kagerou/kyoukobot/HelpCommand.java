package me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Collections;
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
	
	 @Command(aliases = {"k!help", "k!commands"}, usage = "k!help [command]", description = "Shows the list of commands or info on a certain one.") //TODO shorten this one
	    public void onHelpCommand(Message message, String[] args) {
		 if (args.length > 0)
		 {
			 String arg = args[0].toLowerCase();
			 if (arg.startsWith("k!"))
				 arg = arg.substring(2);
			 if (arg.length() > 0)
			 {
				 TreeMap<String, CommandHandler.SimpleCommand> commands = new TreeMap<String, CommandHandler.SimpleCommand>((x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
				 for (CommandHandler.SimpleCommand simpleCommand: handler.getCommands())
					 if (simpleCommand.getCommandAnnotation().showInHelpPage())
						 for (String alias: simpleCommand.getCommandAnnotation().aliases())
							 commands.put(alias.substring(2), simpleCommand); //cut the "k!"
				 String commandName = commands.ceilingKey(arg);
				 if ((commandName != null) && (commandName.toLowerCase().startsWith(arg)))
				 {
					 //TODO help for a single command
					 Command anno = commands.get(commandName).getCommandAnnotation();
					 StringBuilder helpText = new StringBuilder("```\nCommand: " + anno.aliases()[0]);
					 if (anno.aliases().length > 1)
						 if (anno.aliases().length > 2)
						 {
							 helpText.append("\nAliases: ").append(anno.aliases()[1]);
							 for (int index = 2; index < anno.aliases().length; index++)
								 helpText.append(", ").append(anno.aliases()[index]);
						 }
						 else
							 helpText.append("\nAlias: ").append(anno.aliases()[1]);
					 if (!anno.usage().isEmpty())
						 helpText.append("\nUsage: ").append(anno.usage());
					 if (!anno.description().equals("none"))
						 helpText.append("\n\n").append(anno.description());
					 helpText.append("```");
					 message.reply(helpText.toString());
					 return;
				 }
			 }
		 }
		 //TODO list of commands
		 ArrayList<String> commandNames = new ArrayList<String>();
		 for (CommandHandler.SimpleCommand simpleCommand: handler.getCommands())
			 if (simpleCommand.getCommandAnnotation().showInHelpPage())
				 commandNames.add(simpleCommand.getCommandAnnotation().aliases()[0]);
		 Collections.sort(commandNames, (x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
		 StringBuilder commandListText = new StringBuilder("```xml\nCommands list:```\n");
		 commandListText.append('`').append(commandNames.get(0)).append('`');
		 for (int index = 1; index < commandNames.size(); index++)
			 commandListText.append(", `").append(commandNames.get(index)).append("`");
		 commandListText.append("\n\n```\nUse \"k!help command\" to get more info on a certain command.```");
		 message.reply(commandListText.toString());
	        /*StringBuilder builder = new StringBuilder();
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
	        message.reply(builder.toString());*/
	    }
}
