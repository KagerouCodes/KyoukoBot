package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class ProjectCommand implements CommandExecutor {
	@Command(aliases = {"k!proj", "k!project", "k!song"}, usage = "k!proj [name|current]", description = "Searches for an /r/anime sings project or all current ones.")
    public String onCommand(Message message, String args[]) {
		if (KyoukoBot.Songs.isEmpty())
			if (!KyoukoBot.InitSongCollection(KyoukoBot.Songs, KyoukoBot.CurrentSongs, KyoukoBot.SongWiki))
				return "`Failed to access the wiki.`";
		String arg = message.getContent().substring(message.getContent().indexOf(' ') + 1).toLowerCase().trim();
		if (args.length == 0)
			arg = "current";
		if (arg.equals("current"))
		{
			String result = "`Current projects:`\n\n";
			for (SongProject proj: KyoukoBot.CurrentSongs)
				result += proj.toString() + "\n\n";
			return result;
		}
		for (SongProject proj: KyoukoBot.Songs)
			if (proj.name_text.toLowerCase().contains(arg))
				return proj.toString();
		return "`Project not found >_<`";
    }
}
