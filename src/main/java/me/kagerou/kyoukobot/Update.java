package me.kagerou.kyoukobot;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

public class Update {

	public static void main(String[] args) {
		if ((args.length != 2) && (args.length != 3))
		{
			System.out.println("Usage: java -jar update.jar (tmpfile/reboot) jarfile [beta]");
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
		if (args[0].equalsIgnoreCase("reboot"))
			System.out.println("Restarting " + args[1] + "...");
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
