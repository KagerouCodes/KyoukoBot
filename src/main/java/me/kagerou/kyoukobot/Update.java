package me.kagerou.kyoukobot;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

public class Update {

	public static void main(String[] args) {
		if (args.length != 2)
		{
			System.out.println("Usage: java -jar update.jar tmpfile jarfile");
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
		File tmpFile = new File(args[0]);
		File jarFile = new File(args[1]);
		if (jarFile.delete())
			System.out.println("Deleted " + args[1] + " successfully!");
		else
			System.out.println("File " + args[1] + " not found.");
		if (tmpFile.renameTo(jarFile))
			System.out.println("Renamed " + args[0] + " to " + args[1] + ".");
		else
			System.out.println("Failed to rename " + args[0] + " to " + args[1] + ".");
		try {
			//Runtime.getRuntime().exec("java -jar " + args[1]);
			new ProcessBuilder("java", "-jar", args[1]).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
