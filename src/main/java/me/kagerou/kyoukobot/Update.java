package me.kagerou.kyoukobot;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

public class Update {

	public static void main(String[] args) {
		if ((args.length != 2) && (args.length != 3))
		{
			System.out.println("Usage: java -jar update.jar tmpfile jarfile [beta]");
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
		try {
			boolean beta = (args.length > 2) && (args[2].equals("beta"));
			if (!beta)
				new ProcessBuilder("java", "-jar", args[1], "updated").redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
			else
				new ProcessBuilder("java", "-jar", args[1], "updated", "beta").redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
