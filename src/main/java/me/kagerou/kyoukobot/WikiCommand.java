package main.java.me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//links the /r/anime sings wiki
public class WikiCommand implements CommandExecutor {
    @Command(aliases = {"k!wiki"}, description = "Links the /r/anime sings wiki.")
    public String onCommand(String command, String[] args) {
        return "`/r/anime sings wiki:` <" + KyoukoBot.SongWiki + ">";
    }
}
