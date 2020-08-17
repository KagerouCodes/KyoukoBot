package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
//help command, lists all the commands by default or shows info on one of the commands id you use it like this: "k!help help" (or even "k!help k!help")
public class HelpCommand implements CommandExecutor {
	private final CommandHandler handler;
	
	HelpCommand(CommandHandler handler)
	{
		this.handler = handler;
	}
	
	@Command(aliases = {"k!help", "k!commands"}, usage = "k!help [command]", description = "Shows the list of commands or info on a certain one.")
	public void onHelpCommand(Message message, String[] args)
	{
		if (args.length > 0)
		{ //if there's an argument, search for a command with such a name (or starting with it)
			String arg = args[0].toLowerCase();
			if (arg.startsWith("k!")) //accepting arguments like "k!youtube" just in case
				arg = arg.substring(2);
			if (arg.length() > 0)
			{
				//build a map with commands being indexed by their names and aliases
				TreeMap<String, CommandHandler.SimpleCommand> commands = new TreeMap<String, CommandHandler.SimpleCommand>((x, y) -> x.toLowerCase().compareTo(y.toLowerCase()));
				for (CommandHandler.SimpleCommand simpleCommand: handler.getCommands())
					if (simpleCommand.getCommandAnnotation().showInHelpPage())
						for (String alias: simpleCommand.getCommandAnnotation().aliases())
							commands.put(alias.substring(2), simpleCommand); //cut the "k!"
				//actual search
				String commandName = commands.ceilingKey(arg);
				if ((commandName != null) && (commandName.toLowerCase().startsWith(arg)))
				{
					//found the command 
					Command anno = commands.get(commandName).getCommandAnnotation();
					StringBuilder helpText = new StringBuilder("```\nCommand: " + anno.aliases()[0]); //command's name
					if (anno.aliases().length > 1)
						if (anno.aliases().length > 2)
						{ //command's alias(es)
							helpText.append("\nAliases: ").append(anno.aliases()[1]);
							for (int index = 2; index < anno.aliases().length; index++)
								helpText.append(", ").append(anno.aliases()[index]);
						}
						else
							helpText.append("\nAlias: ").append(anno.aliases()[1]);
					if (!anno.usage().isEmpty()) //command's usage
						helpText.append("\nUsage: ").append(anno.usage());
					if (!anno.description().equals("none")) //command's description
						helpText.append("\n\n").append(anno.description());
					helpText.append("```");
					message.reply(helpText.toString());
					return;
				}
			}
		}
		//no arguments or argument not found, just list all the commands sorted by name
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
	}
}
