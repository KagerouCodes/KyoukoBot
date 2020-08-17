package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//displays the info about an /r/anime sings project loaded from the wiki at startup (or all projects if the argument is empty or "current")
public class ProjectCommand implements CommandExecutor
{
	class MatchResult implements Comparable<MatchResult>
	{ //a class for matchWords to return
		int matches; //words matched "as words"
		int first_match; //earliest index of a match "as word"
		MatchResult(int matches, int first_match)
		{
			this.matches = matches;
			this.first_match = first_match;
		}
		MatchResult()
		{
			this(-1, -1);
		}
		@Override
		public int compareTo(MatchResult res)
		{
			if (matches != res.matches)
				return matches - res.matches; //the more words are matched, the better
			return res.first_match - first_match; //if the amount of matches is equal, the earliest one is preferable
		}		
	}
	
	MatchResult matchWords(String name, String[] words)
	{ //returns (-1, -1) if the name doesn't contain all words as substrings, otherwise returns the amount of words it contains "as words"
	//as in surrounded by non-letters, non-digits or bounds of the string
	//plus the earliest match of one of words "as a word"
		int result = 0, first_match = name.length();
		for (String word: words)
		{
			if (!name.contains(word))
				return new MatchResult();
			int index = 0;
			while ((index = name.indexOf(word, index + 1)) != -1)
				if (((index == 0) || !Character.isLetterOrDigit(name.charAt(index - 1))) && ((index + word.length() == name.length()) || !Character.isLetterOrDigit(name.charAt(index + word.length()))))
				{
					result++;
					first_match = Math.min(first_match, index);
					break;
				}
		}
		return new MatchResult(result, first_match);
	}
	
	static ArrayList<String> ProjectDescriptions(ArrayList<SongProject> projects, String empty_answer, String header) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (projects.isEmpty())
		{
			result.add(empty_answer);
			return result;
		}
		
		String text = header;
		for (SongProject proj: projects)
		{ //assemble the result string while breaking it in parts if it's too long for a single message
			String next = proj.toString();
			if (text.length() + next.length() > KyoukoBot.CharLimit)
			{
				result.add(KyoukoBot.wrapLinks(text.trim()));
				// result.add(text.trim());
				text = "";
			}
			text += next + "\n\n";
		}
		// result.add(text.trim());
		result.add(KyoukoBot.wrapLinks(text.trim()));
		
		return result;
	}
	
	static ArrayList<String> CurrentProjectDescriptions() {
		return ProjectDescriptions(KyoukoBot.CurrentSongs, "`There are no currently active projects!`", "`Current projects:`\n\n");
	}
	
	@Command(aliases = {"k!proj", "k!project", "k!projects", "k!song"}, usage = "k!proj [name|current]", description = "Searches for an /r/anime sings project or all current ones (with the word \"current\" or no arguments at all).")
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
			ArrayList<String> ProjectDescriptions = CurrentProjectDescriptions();
			KyoukoBot.PostMultipleMessages(ProjectDescriptions, message.getReceiver());
			
			return;
		}
		//searching for just one project
		String[] words = arg.split("\\s+");
		SongProject result = null;
		MatchResult words_matched = new MatchResult();
		for (SongProject proj: KyoukoBot.Songs)
		{ //trying to find the project name which contains all the words as substrings and as many of them "as words" as possible
			MatchResult cur_matched = matchWords(proj.name_text.toLowerCase(), words);
			if (cur_matched.compareTo(words_matched) > 0)
			{
				result = proj;
				words_matched = cur_matched;
			}
		}
		if (words_matched.matches != -1)
		{
			message.reply(result.toString());
			return;
		}
		message.reply("`Project not found >_<`");
    }
}
