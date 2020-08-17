package main.java.me.kagerou.kyoukobot;

import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.listener.user.UserChangeNameListener;
//fixes the database if a user changes their username
public class NameChangeListener implements UserChangeNameListener {
	@Override
	public void onUserChangeName(DiscordAPI api, User user, String old_name) {
		DataBase.Person IDedPerson = KyoukoBot.Database.get(user.getId());
		Map.Entry<String, DataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(user.getName());
		if (IDedPerson != null)
		{
			KyoukoBot.Database.setName(user.getId(), user.getName());
			if (unIDedEntry != null) //matched the user with their IDed entry, the unIDed one isn't needed anymore
				KyoukoBot.Database.removeEntry(unIDedEntry.getKey());
		}
		else
			if (unIDedEntry != null) //found the ID for the unIDed entry
				KyoukoBot.Database.changeID(unIDedEntry.getKey(), user.getId());
	}

}
