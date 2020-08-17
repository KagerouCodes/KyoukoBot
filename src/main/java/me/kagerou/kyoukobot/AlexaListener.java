package main.java.me.kagerou.kyoukobot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

// handles the "Kyouko play X" easter egg, reacts if Kyouko is replaced by Alexa as well
// TODOKETE this still gets triggered by commands (k!img kyouko play despacito) somehow
// TODOKETE add this to the changelog
public class AlexaListener extends YouTubeSearcher implements MessageCreateListener {
    private Pattern pattern;
    static final String DespacitoLink = "https://www.youtube.com/watch?v=Wy4H-EfWY9s";
    
    AlexaListener()
    {
        super(1, true);
        pattern = Pattern.compile("(kyouko|alexa)[^\\w0-9]+play", Pattern.CASE_INSENSITIVE); // TODOKETE \\w only cares for ASCII, make it ignore other languages too
    }

    boolean react(DiscordAPI api, Message message)
    {
        // TODOKETE the code below is being duplicated, fix!
        String msg = message.getContent().toLowerCase();
        // ignore bots and commands
        if (message.getAuthor().isBot() || msg.startsWith("k!")) { 
            return false;
        }
    
        Matcher match = pattern.matcher(msg);
        if (!match.find()) {
            return false;
        }
        
        message.getReceiver().type();
        String query = msg.substring(match.end());
        if (query.trim().equals("despacito")) {
            message.reply(DespacitoLink);
            return true;
        }
        
        try {
            if (query.isEmpty()) {
                throw new Exception("Empty query");
            }
            // TODOKETE this makes "alexa play all star" play some "disney all star music" no one cares about
            JSONArray searchResult = doSearch(query + " music");
            if (searchResult.length() != 0) {
                message.reply("https://www.youtube.com/watch?v=" + searchResult.getJSONObject(0).getJSONObject("id").getString("videoId"));
                return true;
            }
        } catch (Exception e) {
        }
        
        message.reply("`Anou... Did you mean Despacito?`\n" + DespacitoLink);
        return true;
    }
    
    public void onMessageCreate(DiscordAPI api, Message message)
    {
        react(api, message);
    }
}
