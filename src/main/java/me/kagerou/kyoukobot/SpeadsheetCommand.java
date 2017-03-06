package me.kagerou.kyoukobot;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//links the song suggestion spreadsheet
public class SpeadsheetCommand implements CommandExecutor {
	@Command(aliases = {"k!spreadsheet", "k!suggestions"}, description = "Links the song suggestion spreadsheet (give me a shorter name for this one).")
    public String onCommand(String command, String[] args) {
        return "Song suggestions spreadsheet: <https://docs.google.com/spreadsheets/d/1-otNwoj793L11ZZ26ely9AMgXC0d3U4BqRcJnq_DWNc/edit?usp=drive_web>";
    }
}
