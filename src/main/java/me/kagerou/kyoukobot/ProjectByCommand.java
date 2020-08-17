package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
// displays info about /r/anime sings project by a certain songmaster
public class ProjectByCommand implements CommandExecutor {
    @Command(aliases = {"k!projby", "k!projectby", "k!projectsby"}, usage = "k!projby name", description = "Searches for /r/anime sings projects mixed by a certain songmaster.")
    public void onCommand(Message message, String args[])
    {
        if (KyoukoBot.Songs.isEmpty()) //reload the collection if it's not loaded already
            if (!KyoukoBot.InitSongCollection(KyoukoBot.Songs, KyoukoBot.CurrentSongs, KyoukoBot.SongWiki))
            {
                message.reply("`Failed to access the wiki.`");
                return;
            }
        String arg = KyoukoBot.getArgument(message);
        if (arg.isEmpty()) {
            message.reply("'Enter songmaster's name.'");
            return;
        }
        
        String[] words = arg.split("\\s+");
        ArrayList<SongProject> ValidProjects = new ArrayList<SongProject>();
        for (SongProject proj: KyoukoBot.Songs) {
            boolean valid = true;
            for (String word: words) {
                if (!proj.organisers.toLowerCase().contains(word.toLowerCase())) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                ValidProjects.add(proj);
            }
        }
        ArrayList<String> to_post = ProjectCommand.ProjectDescriptions(ValidProjects, "`No projects by " + arg + " found.`",
                "`Projects by " + arg + ":`\n\n");
        if (message.getChannelReceiver() == null || to_post.size() <= 2) {
            KyoukoBot.PostMultipleMessages(to_post, message.getReceiver());
        } else {
            message.reply("`Project list is too long, sending it via DM.`");
            KyoukoBot.PostMultipleMessages(to_post, message.getAuthor());
        }
    }
}
