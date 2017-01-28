package me.kagerou.kyoukobot;

import java.util.Map;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.listener.user.UserChangeNameListener;

public class NameChangeListener implements UserChangeNameListener {
	@Override
	public void onUserChangeName(DiscordAPI api, User user, String old_name) {
		NewDataBase.Person IDedPerson = KyoukoBot.Database.get(user.getId());
		Map.Entry<String, NewDataBase.Person> unIDedEntry = KyoukoBot.Database.findUnIDedEntry(user.getName());
		if (IDedPerson != null)
		{
			KyoukoBot.Database.setName(user.getId(), user.getName());
			if (unIDedEntry != null)
				KyoukoBot.Database.removeEntry(unIDedEntry.getKey());
		}
		else
			if (unIDedEntry != null)
				KyoukoBot.Database.changeID(unIDedEntry.getKey(), user.getId());
	}

}
