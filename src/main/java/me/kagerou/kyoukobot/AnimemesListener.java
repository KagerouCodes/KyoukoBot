package main.java.me.kagerou.kyoukobot;

import java.lang.reflect.Method;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.sdcf4j.Command;
import main.java.me.kagerou.kyoukobot.MemeBase.MemeResult;

public class AnimemesListener implements MessageCreateListener {
    String[] uploadPrefixes = {}; //is this even needed??
    
    AnimemesListener()
    {
        try {
            Method onCommand = null;
            for (Method meth: UploadCommand.class.getMethods())
                if (meth.getName().equals("onCommand"))
                    onCommand = meth;
            uploadPrefixes = onCommand.getAnnotation(Command.class).aliases();
        }
        catch (Exception e)
        {
            System.out.println("fial");
        }
    }
    
    public void onMessageCreate(DiscordAPI api, Message message) {
        if (message.isPrivateMessage() || message.getAuthor().isBot() || !message.getChannelReceiver().getName().endsWith("emes"))
            return;
        if (message.getContent().toLowerCase().startsWith("k!"))
            return;
        String args[] = message.getContent().split(" |\\r\\n|\\n|\\r");
        /*if (args.length > 0) 
            for (String prefix: uploadPrefixes)
                if (args[0].equalsIgnoreCase(prefix))
                    return;*/
        int uploaded = 0, dupes = 0;
        for (String arg: args)
        {
            MemeResult result = KyoukoBot.memeBase.DownloadImage(arg);
            if (result == MemeResult.DR_OK)
                uploaded++;
            if (result == MemeResult.DR_DUPE)
                dupes++;
        }
        for (MessageAttachment attachment: message.getAttachments())
        {
            MemeResult result = KyoukoBot.memeBase.DownloadImage(attachment.getUrl());
            if (result == MemeResult.DR_OK)
                uploaded++;
            if (result == MemeResult.DR_DUPE)
                dupes++;
        }
        if (uploaded > 0)
            System.out.println("Uploaded " + uploaded + " image(s)!");
        if (dupes > 0)
            System.out.println("Found " + dupes + " duplicate(s)!");
    }
}
