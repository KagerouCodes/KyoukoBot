package main.java.me.kagerou.kyoukobot;

import java.io.File;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//posts a random picture from the "memes" directory
public class LeMemeCommand implements CommandExecutor {
    static String dirName = "memes";
    @Command(aliases = {"k!meme"}, description = "Posts a \"meme\" picture of questionable hilarity.")
    public void onCommand(Message message) {
        message.getReceiver().type();
        //post Shiyu.png on April Fools
        if (KyoukoBot.isAprilFools())
        {
            KyoukoBot.postFile(message, ShiyuCommand.ShiyuFace, "shiyu");
            return;
        }
        File result = KyoukoBot.memeBase.getMeme(); //all the magic happens there
        if (result == null)
            message.reply("`I-I'm out of memes >_< But you can always k!donate them to me!`");
        else
            message.getReceiver().sendFile(result);
    }
}
