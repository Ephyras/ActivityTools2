package cn.zju.edu.manager;

import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.eclipse.swt.graphics.Image;

public class IconManager {
	private static HashMap<String, ImageIcon> ICONS_MAP = new HashMap<String, ImageIcon>();
	
	static
	{
		ICONS_MAP.put("Eclipse", createImageIcon("/icons/eclipse.png"));
		ICONS_MAP.put("Visual Studio", createImageIcon("/icons/vs.png"));
		ICONS_MAP.put("Browser", createImageIcon("/icons/web.png"));
		ICONS_MAP.put("Office Document", createImageIcon("/icons/word.png"));
		ICONS_MAP.put("eclipse.exe", createImageIcon("/icons/java.png"));
		ICONS_MAP.put("firefox.exe", createImageIcon("/icons/firefox.png"));
		ICONS_MAP.put("iexplore.exe", createImageIcon("/icons/ie.png"));
		ICONS_MAP.put("chrome.exe", createImageIcon("/icons/chrome.png"));
		ICONS_MAP.put("devenv.exe", createImageIcon("/icons/cpp.png"));
		ICONS_MAP.put("WINWORD.EXE", createImageIcon("/icons/word.png"));
		ICONS_MAP.put("Baidu", createImageIcon("/icons/baidu.png"));
		ICONS_MAP.put("Google", createImageIcon("/icons/google.png"));
		ICONS_MAP.put("Star", createImageIcon("/icons/star.png"));
		ICONS_MAP.put("default", createImageIcon("/icons/unknown.png"));
	}
	
	protected static ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = IconManager.class.getResource(path);
	    //System.out.println(IconManager.class.getResource(path).toString());
	    if (imgURL != null) {
	      return new ImageIcon(imgURL);
	    } else 
	    {
	      System.err.println("Couldn't find file: " + path);
	      return null;
	    }
	}
	
	public static ImageIcon getIcon(String name)
	{
		if(ICONS_MAP.containsKey(name))
		{
			return ICONS_MAP.get(name);
		}
		return null;
	}
	
	public static void main(String args[])
	{
		createImageIcon("/icons/eclipse.png");
	}
}
