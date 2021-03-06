package main.java.me.kagerou.kyoukobot;

import java.util.ArrayList;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
//listens for certain phrases and responds to them, used for easter eggs
public class PhrasesListener implements MessageCreateListener {
    
    enum PhraseType //can't put it inside Reaction, wut??
    {
        EXACT, //message matches the phrase exactly
        START, //message starts with the phrase
        CONTAINS; //message contains the phrase
    }
    //describes a reaction to a phrase
    class Reaction
    {
        String value, response;
        PhraseType type;
        Reaction(String value, PhraseType type, String response)
        {
            this.value = value.toLowerCase();
            this.type = type;
            this.response = response;
        }
    }
    
    private ArrayList<Reaction> reactions;
    //adds default phrases/responses to the pool
    PhrasesListener()
    {
        reactions = new ArrayList<Reaction>();
        reactions.add(new Reaction("wake me up", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("wake me up!", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("i can't wake up", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("i can't wake up!", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("i cant wake up", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("i cant wake up!", PhraseType.EXACT, "Wake me up inside!"));
        reactions.add(new Reaction("katyusha", PhraseType.EXACT, "https://www.youtube.com/watch?v=d4H70U26HxM"));
        reactions.add(new Reaction("катюша", PhraseType.EXACT, "https://www.youtube.com/watch?v=d4H70U26HxM"));
        
        reactions.add(new Reaction("<:Dab:271137562494500864>", PhraseType.START, "<:Dab2:271137400837767168>"));
        reactions.add(new Reaction("<:Dab2:271137400837767168>", PhraseType.START, "<:Dab:271137562494500864>"));
    }
    //checks if the string matches a reaction
    public boolean matches(String str, Reaction r)
    {
        str = str.toLowerCase();
        switch (r.type)
        {
            case EXACT:
                return str.equals(r.value);
            case START:
                return str.startsWith(r.value);
            case CONTAINS:
                return str.contains(r.value);
        }
        return false; //compiler, please
    }
    //reacts to a message using all the reactions in the pool, returns true if there was at least one appropriate reaction
    public boolean react(DiscordAPI api, Message message)
    {
        String content = message.getContent();
        boolean result = false;
        for (Reaction r: reactions)
            if (matches(content, r))
            {
                message.reply(r.response);
                result = true;
            }
        return result;
    }

    @Override
    public void onMessageCreate(DiscordAPI api, Message message) {
        react(api, message);
    }

}
