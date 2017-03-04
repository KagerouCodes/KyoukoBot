package me.kagerou.kyoukobot;

import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
//no longer used
@Deprecated
public class PokeAbilCommand implements CommandExecutor {
    @Command(aliases = {"k!pokeabil", "k!abil", "k!ability", "k!pokemonability"}, description = "Describes a Pokemon ability.")
    public String onCommand(String command, String[] args) {
    	if (args.length == 0)
    		return "Enter an ability: \"k!abil <ability>\"";
    	try {
    		String ability = "";
    		for (int i = 0; i < args.length; i++)
    		{
    			if (i != 0)
    				ability += '-';
    			ability += args[i].toLowerCase();
    		}
    		JSONObject json = new JSONObject(IOUtils.toString(new URL("http://pokeapi.co/api/v2/ability/" + ability), Charset.forName("UTF-8")));
    		String[] splitName = json.getString("name").split("-");
    		String name = "";
    		for (String split: splitName)
    			name += Character.toUpperCase(split.charAt(0)) + split.substring(1) + ' ';
    		String effect = json.getJSONArray("effect_entries").getJSONObject(0).getString("effect");
    		return "Ability: " + name + "\n\nEffect: " + effect;
    	}
    	catch (Exception e)
    	{
    		return "Ability not found or couldn't access the database.";
    	}
    }
}
