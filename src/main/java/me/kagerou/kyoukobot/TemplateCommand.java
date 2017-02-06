package me.kagerou.kyoukobot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.impl.ImplUser;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.CommandExecutor;

public class TemplateCommand implements CommandExecutor {
	static class Triangle
	{
		Point2D.Double A, B, C;
		Triangle(Point2D.Double A, Point2D.Double B, Point2D.Double C)
		{
			this.A = A;
			this.B = B;
			this.C = C;
		}
		Triangle(double Ax, double Ay, double Bx, double By, double Cx, double Cy)
		{
			this(new Point2D.Double(Ax, Ay), new Point2D.Double(Bx, By), new Point2D.Double(Cx, Cy));
		}
		Triangle()
		{
			this(0, 0, 0, 0, 0, 0);
		}
	}
	
	enum StretchOption
	{
		STRETCH, SCALE_MIN, SCALE_MAX;
	}
	
	private BufferedImage template;
	private int width, height;
	private Triangle[] triangles;
	private StretchOption stretch;
	
	TemplateCommand (String template_link, StretchOption stretch, Triangle... triangles)
	{
		this.triangles = triangles;
		try {
			template = ImageIO.read(new URL(template_link));
			width = template.getWidth();
			height = template.getHeight();
			this.stretch = stretch;
		}
		catch (Exception e)
		{
			template = null;
			System.out.println("Failed to load the template " + template_link + " >_<");
		}
	}
	
	public static AffineTransform TriangleToTriangle (Point2D.Double A, Point2D.Double B, Point2D.Double C,
			Point2D.Double A1, Point2D.Double B1, Point2D.Double C1)
	{        
		double x11 = A.getX();
		double x12 = A.getY();
		double x21 = B.getX();
		double x22 = B.getY();
		double x31 = C.getX();
		double x32 = C.getY();
		double y11 = A1.getX();
		double y12 = A1.getY();
		double y21 = B1.getX();
		double y22 = B1.getY();
		double y31 = C1.getX();
		double y32 = C1.getY();

		double a1 = ((y11-y21)*(x12-x32)-(y11-y31)*(x12-x22))/
                ((x11-x21)*(x12-x32)-(x11-x31)*(x12-x22));
		double a2 = ((y11-y21)*(x11-x31)-(y11-y31)*(x11-x21))/
                ((x12-x22)*(x11-x31)-(x12-x32)*(x11-x21));
		double a3 = y11-a1*x11-a2*x12;
		double a4 = ((y12-y22)*(x12-x32)-(y12-y32)*(x12-x22))/
                ((x11-x21)*(x12-x32)-(x11-x31)*(x12-x22));
		double a5 = ((y12-y22)*(x11-x31)-(y12-y32)*(x11-x21))/
                ((x12-x22)*(x11-x31)-(x12-x32)*(x11-x21));
		double a6 = y12-a4*x11-a5*x12;
		return new AffineTransform(a1, a4, a2, a5, a3, a6);
	}
	
	void DrawOnTop(Graphics2D graphics, BufferedImage image, Point2D.Double A, Point2D.Double B, Point2D.Double C, StretchOption stretch)
	{
		AffineTransform transform;
		BufferedImage img = image;
		if (stretch == StretchOption.STRETCH)
			transform = TriangleToTriangle(new Point2D.Double(), new Point2D.Double(image.getWidth(), 0.0), new Point2D.Double(0.0, image.getHeight()), A, B, C);
		else
		{
			double ratio = A.distance(B) / A.distance(C);
			System.out.println("Ratio = " + ratio);
			double x, y;
			if (stretch == StretchOption.SCALE_MIN)
			{
				ratio = 16.0 / 9.0; //Potato's request, bad code
				x = Math.max(image.getHeight() * ratio, image.getWidth()) / 2.0;
				y = Math.max(image.getWidth() / ratio, image.getHeight()) / 2.0;
			}
			else
			{
				x = Math.min(image.getHeight() * ratio, image.getWidth()) / 2.0;
				y = Math.min(image.getWidth() / ratio, image.getHeight()) / 2.0;
				img = image.getSubimage(Math.max((int) (image.getWidth() / 2.0 - x), 0), Math.max((int) (image.getHeight() / 2.0 - y), 0), Math.min((int) (2 * x), image.getWidth()), Math.min((int) (2 * y), image.getHeight()));
			}
			transform = TriangleToTriangle(new Point2D.Double(-x, -y), new Point2D.Double(x, -y), new Point2D.Double(-x, y), A, B, C);
			transform.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
		}
		graphics.drawImage(img, transform, null);
	}
	
    public void onCommand(DiscordAPI api, Message message, Server server, String[] args) { //TODO do something with https://2static2.fjcdn.com/thumbnails/comments/Yes+_12bb1d42b9794b90b53cae6196c9baed.png ??
    	//message.getReceiver().type();
		if (template == null)
		{
			message.reply("`Failed to load the template >_<`");
			return;
		}
		message.getReceiver().type();
		URL imgURL = null;
		String original_arg = "";
		if (args.length > 0)
			original_arg = message.getContent().split(" ", 2)[1].trim();
		String arg = original_arg.toLowerCase();
		if (!message.getAttachments().isEmpty())
			imgURL = message.getAttachments().iterator().next().getUrl();
		if (imgURL == null)
		{
			User target = null;
			if (arg.isEmpty())
				target = message.getAuthor();
			if (target == null)
				if (!message.getMentions().isEmpty())
					target = message.getMentions().get(0);
			if (target == null)
				target = KyoukoBot.findUserOnServer(arg, server, message.getAuthor());
			if (target != null)
				imgURL = getAvatarUrl(target);
				//imgURL = target.getAvatarUrl();
		}
		if (imgURL == null)
			try {
				imgURL = new URL(original_arg);
			}
			catch (MalformedURLException e)
			{
				imgURL = null;
			}
		boolean foundImage;
		String format = "";
		try {
			format = KyoukoBot.leTika.detect(imgURL);
			foundImage = (imgURL != null) && format.startsWith("image");
		}
		catch (Exception e)
		{
			foundImage = false;
		}
		if (!foundImage)
		{
			message.reply("`Couldn't find the user/image >_<`");
			return;
		}
		BufferedImage image;
		try {
			image = ImageIO.read(imgURL);
		}
		catch (IOException e)
		{
			message.reply("`Couldn't read the image >_<`");
			return;
		}
		
		BufferedImage result = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = result.createGraphics();
		graphics.setColor(new Color(255, 255, 255));
		graphics.fillRect(0, 0, width, height);
		
		for (Triangle tri: triangles)
			DrawOnTop(graphics, image, tri.A, tri.B, tri.C, stretch);
		/*DrawOnTop(graphics, image, new Point2D.Double(121, 622), new Point2D.Double(278, 605), new Point2D.Double(169, 842));
		DrawOnTop(graphics, image, new Point2D.Double(284, 557), new Point2D.Double(444, 567), new Point2D.Double(331, 769));
		DrawOnTop(graphics, image, new Point2D.Double(224, 858), new Point2D.Double(494, 858), new Point2D.Double(253, 1011));
		
		DrawOnTop(graphics, image, new Point2D.Double(498, 561), new Point2D.Double(714, 513), new Point2D.Double(539, 731));
		DrawOnTop(graphics, image, new Point2D.Double(730, 517), new Point2D.Double(843, 508), new Point2D.Double(772, 675));
		DrawOnTop(graphics, image, new Point2D.Double(693, 707), new Point2D.Double(878, 681), new Point2D.Double(725, 859));
		DrawOnTop(graphics, image, new Point2D.Double(643, 897), new Point2D.Double(815, 864), new Point2D.Double(667, 1012));*/ //new Point2D.Double(662, 992));
		graphics.drawImage(template, 0, 0, null); //TODO maybe integration with Google Image Search??
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(result, "PNG", os);
		 	message.getReceiver().sendFile(new ByteArrayInputStream(os.toByteArray()), "image" + KyoukoBot.DefaultMimeTypes.forName(format).getExtension());
		}
		catch (Exception e)
		{
			message.reply("`An error occured >_<`");
		 	return;
		}
	}

	private URL getAvatarUrl(User user) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("https://discordapp.com/api/users/" + user.getId()).openConnection();
			conn.setRequestProperty("Authorization", "Bot " + KyoukoBot.token);
			conn.setRequestProperty("Content-Type", "application/json");
			JSONObject userJSON = new JSONObject(IOUtils.toString(conn.getInputStream(), "UTF-8"));
			ImplUser real_user = (ImplUser) user;
			real_user.setAvatarId(userJSON.getString("avatar"));
		}
		catch (Exception e)
		{e.printStackTrace();}
		return user.getAvatarUrl();
	}
}
