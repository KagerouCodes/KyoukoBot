package main.java.me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//an easter egg
public class RemCommand implements CommandExecutor {
    @Command(aliases = {"k!rem"}, showInHelpPage = false)
    public String onCommand(String command, String[] args) {
    	return "Who is Rem? :thinking:";
    }
}
