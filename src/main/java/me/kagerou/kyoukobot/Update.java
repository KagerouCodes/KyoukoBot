package me.kagerou.kyoukobot;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
//this class is compiled into a jar separately and is needed to reboot the bot or update it
//Usage: java -jar update.jar (tmpfile/reboot/manreboot) jarfile [beta]
//if the first parameter is reboot, the bot is rebooted
//if it's manreboot, the bot is rebooted and notifies the owner when it goes online
//if it's none of the above, it should be a name of a file with the new version of the bot; update.jar will delete the old one and replace it with tmpfile,
//launch it and notify the owner
//jarfile is the name of a jar file with the bot
//if the beta parameter is present, it is passed to the bot jar and it uses the beta authorisation token
public class Update {

	public static void main(String[] args) {
		if ((args.length != 2) && (args.length != 3))
		{
			System.out.println("Usage: java -jar update.jar (tmpfile/reboot/manreboot) jarfile [beta]");
			return;
		}
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			System.out.println("Interrupted!");
			return;
		}
		File jarFile = new File(args[1]);
		boolean beta = (args.length > 2) && (args[2].equals("beta"));
		List<String> commands = new ArrayList<String>();
		commands.add("java");
		commands.add("-jar");
		commands.add(args[1]);
		if (beta)
			commands.add("beta");
		if (args[0].equalsIgnoreCase("reboot") || args[0].equalsIgnoreCase("manreboot"))
		{
			System.out.println("Restarting " + args[1] + "...");
			commands.add("rebooted");
			if (args[0].equalsIgnoreCase("manreboot"))
				commands.add("hello");
		}
		else
		{
			File tmpFile = new File(args[0]);
			if (!tmpFile.exists())
			{
				System.out.println("File " + args[0] + " not found.");
				return;
			}
			if (jarFile.delete())
				System.out.println("Deleted " + args[1] + " successfully!");
			else
				System.out.println("File " + args[1] + " not found.");
			if (tmpFile.renameTo(jarFile))
				System.out.println("Renamed " + args[0] + " to " + args[1] + ".");
			else
				System.out.println("Failed to rename " + args[0] + " to " + args[1] + ".");
			commands.add("updated");
		}
		try {
			new ProcessBuilder(commands).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
