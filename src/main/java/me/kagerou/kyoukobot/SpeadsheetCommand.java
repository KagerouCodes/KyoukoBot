package me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class SpeadsheetCommand implements CommandExecutor {
	@Command(aliases = {"k!spreadsheet", "k!suggestions"}, description = "Links the song suggestion spreadsheet.")
    public String onCommand(String command, String[] args) {
        return "https://docs.google.com/spreadsheets/d/1-otNwoj793L11ZZ26ely9AMgXC0d3U4BqRcJnq_DWNc/edit?usp=drive_web";
    }
}
