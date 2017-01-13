package me.kagerou.kyoukobot;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class UpdateCommand implements CommandExecutor {
	static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";
	@Command(aliases = {"k!update"}, description = "Cheesy admin-only command.", usage = "k!update file [filename]", showInHelpPage = false)
    public void onCommand(DiscordAPI api, Message message, String args[])
    {
		if (!message.getAuthor().getId().equals(KyoukoBot.adminID))
		{
			message.reply("Y-you're touching me inappropriately!");
			return;
		}
		if ((args.length == 0) && (message.getAttachments().isEmpty()))
		{
			message.reply("`Link or attach a file.`");
			return;
		}
		URL link = null;
		String FileName = null;
		if (!message.getAttachments().isEmpty())
		{
			link = message.getAttachments().iterator().next().getUrl();
			if (args.length > 0)
				FileName = message.getContent().substring(message.getContent().indexOf(' ') + 1).trim();
		}
		else
			try {
				link = new URL(args[0]);
				if (args.length > 1)
				{
					int first_space = message.getContent().indexOf(' ');
					FileName = message.getContent().substring(message.getContent().indexOf(' ', first_space + 1) + 1).trim();
				}	
			}
			catch (MalformedURLException e)
			{
				link = null;
			}
		if (link == null)
		{
			message.reply("`Link or attach a file.`");
			return;
		}
		HttpURLConnection leConnection;
		String type;
		try {
			/*HttpURLConnection httpCon = (HttpURLConnection) link.openConnection();
		    httpCon.addRequestProperty("User-Agent", userAgent);
		    httpCon.setInstanceFollowRedirects(true);
		    httpCon.setUseCaches(true);
		    httpCon.setRequestMethod("GET");
		    type = httpCon.getContentType();
		    InputStream leStream = httpCon.getInputStream();*/
			leConnection = (HttpURLConnection) link.openConnection();
			leConnection.addRequestProperty("User-Agent", userAgent);
			type = leConnection.getContentType();
			if (type == null)
				type = KyoukoBot.leTika.detect(link);
				//type = KyoukoBot.leTika.detect(leStream);
			if (FileName == null)
			{
				FileName = FilenameUtils.getName(link.getPath());
				if (FilenameUtils.getExtension(link.getPath()).isEmpty())
					FileName += KyoukoBot.DefaultMimeTypes.forName(type).getExtension();
			}
			if (!FileName.equalsIgnoreCase("KyoukoBot.jar"))
			{
				//FileUtils.copyInputStreamToFile(leStream, new File(FileName));
				if (!FileName.equalsIgnoreCase("changelog.txt"))
				{
					FileUtils.copyURLToFile(link, new File(FileName));
					message.reply("`Successfully downloaded the file " + FileName + " of the type " + type + "!`");
				}
				else
				{
					FileName = "changelog.txt";
					FileUtils.copyURLToFile(link, new File(FileName));
					KyoukoBot.ChangeLog = KyoukoBot.InitChangeLog(FileName);
					message.reply("`Successfully downloaded the file " + FileName + " and updated the changelog!`");
				}
			}
			else
			{
				FileName = "KyoukoBot.jar";
				FileUtils.copyURLToFile(link, new File("KyoukoBot.tmp"));
				message.reply("`Successfully downloaded the file " + FileName + "! Updating the bot...`");
				Thread.sleep(1000);
				//Runtime.getRuntime().exec("java -jar update.jar KyoukoBot.tmp KyoukoBot.jar");
				KyoukoBot.coc.stop();
				new ProcessBuilder("java", "-jar", "update.jar", "KyoukoBot.tmp", "KyoukoBot.jar").redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
				System.exit(0); //TODO make Kyouko tell me she's back after updating
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed to connect to " + link + " or download the file.");
			e.printStackTrace();
			message.reply("`Failed to download the file.`");
			return;
		}
    }
}
