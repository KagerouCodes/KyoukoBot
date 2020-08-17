package main.java.me.kagerou.kyoukobot;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
//checks if there are Twitch emote names from the KyoukoBot.Emotes list in a message and, if so, responds with those emotes
class TwitchListener implements MessageCreateListener
{
    Random rnd;
    TwitchListener()
    {
        rnd = new Random(System.currentTimeMillis()); //probably not needed but i was trying to save time on initialising Random every time
    }
    static int EmoteLimit = 3; //max amounts of emotes to post
    //checks if a certain index of a string is out of bounds or a symbol there satisfies a predicate
    boolean CheckSymbol(String str, int index, Predicate<Character> pred)
    {
        boolean result = (index < 0) || (index >= str.length()) || pred.test((str.charAt(index))); 
        return result;
    }
    //a class to store each occurence of an emote in a message
    //they're stored in a priority queue and the emotes found earlier in the message are prioritised 
    class IndexPair implements Comparable<IndexPair>
    {
        int emote_index, symb_index;
        IndexPair(int emote_index, int symb_index)
        {
            this.emote_index = emote_index;
            this.symb_index = symb_index;
        }
        public int compareTo(IndexPair pair)
        {
            if (symb_index != pair.symb_index)
                return symb_index - pair.symb_index;
            return emote_index - pair.emote_index;
        }
    }
    //the same as onMessageCreate but returns boolean depending on whether the message was responded
    public boolean react(DiscordAPI api, Message message)
    {
        String msg = message.getContent().toLowerCase();
        if (message.getAuthor().isBot() || msg.startsWith("k!")) //ignore bots and commands
            return false;
        PriorityQueue<IndexPair> Indexes = new PriorityQueue<IndexPair>(); //a queue to store all the detected emotes in
        for (int emote_index = 0; emote_index < KyoukoBot.Emotes.size(); emote_index++)
        {
            Emote emote = KyoukoBot.Emotes.get(emote_index);
            if (emote.name.equals("goldenkappa")) //getting golden kappa shouldn't be that easy
                continue;
            int index = -1;
            do {  //searching for an emote name (not case-sensitive) which doesn't have letters, digits or two colons next to it
                index = msg.indexOf(emote.name, index + 1);
                if ((index != -1) && CheckSymbol(msg, index - 1, (ch) -> !Character.isLetterOrDigit(ch)) &&
                        CheckSymbol(msg, index + emote.name.length(), (ch) -> !Character.isLetterOrDigit(ch)) &&
                        (CheckSymbol(msg, index - 1, (ch) -> ch != ':') ||
                        CheckSymbol(msg, index + emote.name.length(), (ch) -> ch != ':')))
                {
                    Indexes.add(new IndexPair(emote_index, index));
                    break;
                }
            } while (index != -1);
        }
        int to_post = (Indexes.size() < EmoteLimit) ? Indexes.size() : EmoteLimit; //how many emotes need to be reposted
        if (to_post == 0)
            return false;
        for (int i = 0; i < to_post; i++)
        {
            if (i > 0)
                try {
                    Thread.sleep(200); //gotta guarantee the correct order
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            //grab the next emote, replace Kappa with the golden one 1% of the time, then post it
            IndexPair pair = Indexes.poll();
            Emote emote = KyoukoBot.Emotes.get(pair.emote_index);
            if (emote.name.equals("kappa") && (rnd.nextInt(100) == 0))
                emote = Iterables.find(KyoukoBot.Emotes, (x) -> x.name.equals("goldenkappa"), emote);
            try {
                message.getReceiver().sendFile(emote.toFile(true));
            }
            catch (Exception e)
            {
                System.out.println("Failed to post the " + emote.name + "emote.");
                e.printStackTrace();
                message.reply(emote.url);
            }
        }
        return true;
    }
    
    @Override
    public void onMessageCreate(DiscordAPI api, Message message) {
        react(api, message);
    }
}