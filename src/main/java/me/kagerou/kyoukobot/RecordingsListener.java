package me.kagerou.kyoukobot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Iterables;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class RecordingsListener implements MessageCreateListener
{
	private String recordingsID;
	
	RecordingsListener(String recordingsID)
	{
		this.recordingsID = recordingsID;
	}
	
	boolean react(DiscordAPI api, Message message)
	{
		if (message.isPrivateMessage() || !message.getChannelReceiver().getId().equals(recordingsID))
			return false;
		for (MessageAttachment attachment: message.getAttachments())
		{
			try {
				String type = KyoukoBot.leTika.detect(attachment.getUrl());
				if (type.startsWith("audio"))
					return false;
			} catch (IOException e) {
				System.out.println("Failed to read the attachment: " + attachment.getUrl());
				e.printStackTrace();
			}
		}
		if ((message.getContent().indexOf("http://") != -1) || (message.getContent().indexOf("https://") != -1))
			return false;
		
		// pins are parsed by Javacord as empty messages
		if (message.getContent().isEmpty() && message.getAttachments().isEmpty()) {
			return false;
		}
		
		System.out.println("A message without a link/attachment has been detected in #recordings.");
		boolean can_manage = Iterables.any(api.getYourself().getRoles(message.getChannelReceiver().getServer()), (role) -> role.getPermissions().getState(PermissionType.MANAGE_MESSAGES) == PermissionState.ALLOWED);
		if (can_manage)
			System.out.println("I am allowed to delete messages here.");
		else
			System.out.println("I'm NOT allowed to delete messages here!");
		boolean deleted = false;
		
		try {
			message.delete().get();
			deleted = true;
		}
		catch (ExecutionException | InterruptedException e)
		{	
			if (can_manage)
			{
				System.out.println("Failed to delete the message " + message.getId() + ".");
				e.printStackTrace();
			}
		}
		
		String warning = "`Please don't chat in #recordings.";
		if (deleted)
			warning += " Your message has been deleted.";
		warning += '`';
		message.getAuthor().sendMessage(warning);
		return true;
	}
	
	@Override
	public void onMessageCreate(DiscordAPI api, Message message)
	{
		react(api, message);
	}
}
