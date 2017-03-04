package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//displays the info about an /r/anime sings project loaded from the wiki at startup (or all projects if the argument is empty or "current")
public class ProjectCommand implements CommandExecutor {
	@Command(aliases = {"k!proj", "k!project", "k!song"}, usage = "k!proj [name|current]", description = "Searches for an /r/anime sings project or all current ones (with the word \"current\" or no arguments at all).")
    public void onCommand(Message message, String args[])
	{
		if (KyoukoBot.Songs.isEmpty()) //reload the collection if it's not loaded already
			if (!KyoukoBot.InitSongCollection(KyoukoBot.Songs, KyoukoBot.CurrentSongs, KyoukoBot.SongWiki))
			{
				message.reply("`Failed to access the wiki.`");
				return;
			}
		String arg = KyoukoBot.getArgument(message);
		if (arg.isEmpty())
			arg = "current";
		if (arg.equals("current"))
		{ //displaying all current projects
			if (KyoukoBot.CurrentSongs.isEmpty())
			{
				message.reply("`There are no currently active projects!`");
				return;
			}
			String result = "`Current projects:`\n\n";
			for (SongProject proj: KyoukoBot.CurrentSongs)
			{ //assemble the result string while breaking it in parts if it's too long for a single message
				String next = proj.toString();
				if (result.length() + next.length() > KyoukoBot.CharLimit)
				{
					message.reply(result);
        			try {
    					Thread.sleep(500); //gotta guarantee the correct order
    				}
    				catch (InterruptedException e)
    				{
    					e.printStackTrace();
    				}
					result = "";
				}
				result += next + "\n\n";
			}
			message.reply(result);
			return;
		}
		for (SongProject proj: KyoukoBot.Songs) //searching for just one project
			if (proj.name_text.toLowerCase().contains(arg))
			{
				message.reply(proj.toString());
				return;
			}
		message.reply("`Project not found >_<`");
    }
}
