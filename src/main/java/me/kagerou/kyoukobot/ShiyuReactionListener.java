package me.kagerou.kyoukobot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Reaction;
import de.btobastian.javacord.listener.message.ReactionAddListener;

public class ShiyuReactionListener implements ReactionAddListener {
	@Override
	public void onReactionAdd(DiscordAPI api, Reaction reaction, User user) {
		System.out.println("Reaction created!");
		if (!user.isBot() && (reaction.getCustomEmoji() != null) && (reaction.getCustomEmoji().getName().equals("Shiyu")))
			reaction.getMessage().addCustomEmojiReaction(reaction.getCustomEmoji());
	}

}
