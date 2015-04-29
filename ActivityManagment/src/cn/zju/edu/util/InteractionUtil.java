package cn.zju.edu.util;

import java.util.HashMap;
import java.util.List;

import com.sun.media.jfxmedia.logging.Logger;

import activity.web.db.DBRecord;
import cn.zju.edu.blf.dao.LowLevelInteraction;

public class InteractionUtil {
	public final static HashMap<String,String> APP_MAP = new HashMap<String, String>();
	public final static HashMap<String, String> UI_ENG2CHS_MAP = new HashMap<String, String>();
	static
	{
		APP_MAP.put("firefox.exe", "Mozilla Firefox");
		APP_MAP.put("chrome.exe", "Google Chrome");
		APP_MAP.put("iexplore.exe", "Internet Explorer");
		APP_MAP.put("eclipse.exe", "Eclipse");
		APP_MAP.put("javaw.exe", "Eclipse");
		APP_MAP.put("devenv.exe", "Visual Studio");
		APP_MAP.put("WINWORD.exe", "Word Document");
		APP_MAP.put("notepad++.exe", "Notepad++");
		
		UI_ENG2CHS_MAP.put("button", "按钮");
		UI_ENG2CHS_MAP.put("edit", "编辑");
		UI_ENG2CHS_MAP.put("window", "窗口");
		UI_ENG2CHS_MAP.put("pane", "窗格");
		UI_ENG2CHS_MAP.put("dialog", "对话框");
		UI_ENG2CHS_MAP.put("tree item", "树项目");
		UI_ENG2CHS_MAP.put("tab item", "选项卡项目");
		UI_ENG2CHS_MAP.put("split button", "拆分按钮");
		UI_ENG2CHS_MAP.put("title bar", "标题栏");
		UI_ENG2CHS_MAP.put("tool bar", "工具栏");
		UI_ENG2CHS_MAP.put("menu item", "菜单项目");
		UI_ENG2CHS_MAP.put("tree", "树");
		UI_ENG2CHS_MAP.put("image", "图像");
		UI_ENG2CHS_MAP.put("text", "文档");
		UI_ENG2CHS_MAP.put("thumb", "缩略图");
		UI_ENG2CHS_MAP.put("check box", "复选框");
		UI_ENG2CHS_MAP.put("list", "列表");
		UI_ENG2CHS_MAP.put("list item", "列表项目");
		UI_ENG2CHS_MAP.put("tab", "Tab 键");
		UI_ENG2CHS_MAP.put("hyperlink", "超级链接");
	}
	
	public static LowLevelInteraction fromDBRecord(DBRecord rs)
	{
		LowLevelInteraction ui = new LowLevelInteraction();
		ui.setTimestamp(rs.getString("timestamp"));
		ui.setWindow(rs.getString("window"));
		ui.setParentWindow(rs.getString("parent_window"));
		ui.setApplication(rs.getString("application"));
		ui.setPx(rs.getInt("point_x"));
		ui.setPy(rs.getInt("point_y"));
		ui.setwRectLeft(rs.getInt("win_rect_left"));
		ui.setwRectTop(rs.getInt("win_rect_top"));
		ui.setwRectRight(rs.getInt("win_rect_right"));
		ui.setwRectBottom(rs.getInt("win_rect_bottom"));
		ui.setUiName(rs.getString("ui_name"));
		ui.setUiType(rs.getString("ui_type"));
		ui.setUiValue(rs.getString("ui_value"));
		ui.setParentUiName(rs.getString("parent_ui_name"));
		ui.setParentUiType(rs.getString("parent_ui_type"));
		ui.setUiBoundLeft(rs.getInt("ui_bound_left"));
		ui.setUiBoundTop(rs.getInt("ui_bound_top"));
		ui.setUiBoundRight(rs.getInt("ui_bound_right"));
		ui.setUiBoundBottom(rs.getInt("ui_bound_bottom"));
		ui.setHasScreen(rs.getBoolean("has_screen"));
		ui.setScreen(rs.getBufferedImage("screen"));
		
		return ui;
	}
	
	public static boolean isControlType(String type, String value)
	{
		return type.equals(value) || UI_ENG2CHS_MAP.get(type).equals(value);
	}
	
	public static boolean isBrowser(String app)
	{
		return "firefox.exe".equals(app) || "chrome.exe".equals(app) || "iexplore.exe".equals(app);
	}
	
	public static String getWindowName(LowLevelInteraction u)
	{
		String app = u.getApplication();
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		else if("chrome.exe".equals(app))
		{
			return "Chrome Legacy Window".equals(u.getWindow()) || "".equals(u.getWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("WINWORD.EXE".equals(app))
		{
			if("Microsoft Word Document".equalsIgnoreCase(u.getWindow()))
			{
				return u.getParentWindow() + " - Word";
			}
			else if("Ribbon".equals(u.getParentWindow()))
			{
				return "";
			}
			
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("EXCEL.EXE".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("devenv.exe".equals(app))
		{
			String w = "".equals(u.getWindow()) ? u.getParentUiName() : u.getWindow();
			String vsfile = getVSFile(u);
			if(!"".equals(vsfile)) 
			{
				return vsfile + " (" + w + ")";
			}
			
			return w;
		}
		else if("notepad++.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else if("WinEdt.exe".equals(app))
		{
			return  !"".equals(u.getParentWindow()) ? u.getParentWindow() : u.getWindow();
		}
		else
		{
			return !"".equals(u.getWindow()) ? u.getWindow() : u.getParentWindow();
		}
		
	}
	
	public static boolean isWebPage(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String appName = APP_MAP.get(u.getApplication());
		return w.endsWith(" - " + appName);
	}
	
	public static boolean isEclipseMainWindow(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class|jsp|properties|css)\\s\\-\\sEclipse";
		return w.matches(pattern);
	}
	
	public static boolean isWinWORDFile(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		if("Microsoft Word Document".equals(u.getWindow()) || 
				u.getParentWindow().endsWith("Word"))
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isExcelFile(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		if(w.toLowerCase().contains("xls") 
				|| w.toLowerCase().contains("excel"))
				{
					return true;
				}
		
		return false;
	}
	
	public static String getVSFile(LowLevelInteraction u)
	{
		if("Text Editor".equals(u.getUiName()))
		{
			String fileName = u.getParentUiName();
			if(fileName.contains(".cpp") || fileName.contains(".h"))
			{
				if(fileName.endsWith("*"))
				{
					fileName = fileName.substring(0, fileName.length()-1);
				}
				return fileName;
			}
		}
		else if("Solution Explorer".equals(u.getParentUiName()))
		{
			if(u.getUiName().contains(".cpp") || u.getUiName().contains(".h"))
			{
				return u.getUiName();
			}
		}
		
		return "";
	}
	
	public static boolean isWinEdtFile(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		if(w.toLowerCase().contains("winedt"))
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isFoxitReader(LowLevelInteraction u)
	{
		String w = getWindowName(u);
		if(w.toLowerCase().contains("Foxit Reader"))
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean isMainWindow(LowLevelInteraction u)
	{
		if(isBrowser(u.getApplication()))
		{
			return isWebPage(u);
		}
		else if("eclipse.exe".equals(u.getApplication()) || "javaw.exe".equals(u.getApplication()))
		{
			return  isEclipseMainWindow(u);
		}
		else if("WINWORD.EXE".equals(u.getApplication()))
		{
			return isWinWORDFile(u);
		}
		else if("devenv.exe".equals(u.getApplication()))
		{
			return !"".equals(getVSFile(u));
		}
		else if("EXCEL.EXE".equalsIgnoreCase(u.getApplication()))
		{
			return isExcelFile(u);
		}
		else if("WinEdt.exe".equalsIgnoreCase(u.getApplication()))
		{
			return isWinEdtFile(u);
		}
		else if("foxitreader.exe".equalsIgnoreCase(u.getApplication()))
		{
			return isFoxitReader(u);
		}
		else if("explorer.exe".equals(u.getApplication())) 
		{
			return false;
		}
		return true;
	}
	
	public static boolean filterApplication(LowLevelInteraction u)

	
	{
		if("explorer.exe".equals(u.getApplication())) 
		{
			return true;
		}
		else if(isBrowser(u.getApplication()))
		{
			String title = getInteractionTitle(u);
			return !isWebPage(u) || title.equalsIgnoreCase("new tab") || title.startsWith("http");
		}
		/*
		else if("eclipse.exe".equals(u.getApplication()) || "javaw.exe".equals(u.getApplication()))
		{
			return  !isEclipseMainWindow(u);
		}
		else if("WINWORD.EXE".equals(u.getApplication()))
		{
			return !isWinWORDFile(u);
		}
		else if("devenv.exe".equals(u.getApplication()))
		{
			return "".equals(getVSFile(u));
		}
		*/
		
		return false;
	}
	
	public static boolean filterApplicationForCurrent(LowLevelInteraction u)
	{
		if("explorer.exe".equals(u.getApplication())) 
		{
			return true;
		}
		else if(isBrowser(u.getApplication()))
		{
			String title = getInteractionTitle(u);
			return !isWebPage(u) || title.equalsIgnoreCase("new tab") || title.startsWith("http");
		}
		else if("eclipse.exe".equals(u.getApplication()) || "javaw.exe".equals(u.getApplication()))
		{
			return  !isEclipseMainWindow(u);
		}
		else if("WINWORD.EXE".equals(u.getApplication()))
		{
			return !isWinWORDFile(u);
		}
		else if("devenv.exe".equals(u.getApplication()))
		{
			return "".equals(getVSFile(u));
		}
		
		return false;
	}
	
	public static boolean isAggregated(LowLevelInteraction u, LowLevelInteraction v) //u: previous, v: next
	{
		if(v.getApplication() != null && !v.getApplication().equals(u.getApplication())) return false;
		
		String app = u.getApplication();
		String w1 = getInteractionTitle(u);
		String w2 = getInteractionTitle(v);
		
		if(isMainWindow(u) && isMainWindow(v))
		{
			return w1.equals(w2);
		}
		else if(isMainWindow(u) && !isMainWindow(v))
		{
			return true;
		}
		else
		{
			return false;
		}
		
		/*
		if("firefox.exe".equals(app) || "iexplore.exe".equals(app))
		{
			return isWebPage(u) && (w1.equals(w2) || "".equals(w2) || !isWebPage(v));
		}
		else if("chrome.exe".equals(app))
		{
			return w1.equals(w2) || "".equals(w2) || !isWebPage(v);
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			return isEclipseMainWindow(u) && (w1.equals(w2) || "".equals(w2) || !isEclipseMainWindow(v));
		}
		else if("WINWORD.EXE".equals(app))
		{
			return isWinWORDFile(u) && (w1.equals(w2) || "".equals(w2) || !isWinWORDFile(v));
		}
		else if("EXCEL.EXE".equals(app))
		{
			return isExcelFile(u) && (w1.equals(w2) || "".equals(w2) || !isExcelFile(v));
		}
		else if("devenv.exe".equals(app))
		{
			String f1 = getVSFile(u);
			String f2 = getVSFile(v);
			
			return !"".equals(f1) && (w1.equals(w2) || "".equals(f2));
			
		}
		else if("WinEdt.exe".equals(app))
		{
			return isWinEdtFile(u) && (w1.equals(w2) || "".equals(w2) || !isWinEdtFile(v));
		}
		else if("FoxitReader.exe".equals(app))
		{
			return isFoxitReader(u) && (w1.equals(w2) || "".equals(w2) || !isWinEdtFile(v));
		}
		else
		{
			return w1.equals(w2) || "".equals(w2);
		}
		*/
	}
	
	public static String getInteractionTitle(LowLevelInteraction u)
	{
		String title = "";
		
		String app = u.getApplication();
		title = getWindowName(u);
		if(isBrowser(app))
		{
			int index = title.indexOf(" - " + APP_MAP.get(app));
			if(index >= 0)
			{
				title = title.substring(0, index);
				
				if(isControlType("tab item", u.getUiType()))
				{
					title = u.getUiName();
				}
			}
		}
		else if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
		{
			String pattern = ".+\\s\\-\\s.*\\.(java|xml|txt|class|jsp|css)\\s\\-\\sEclipse";
			if(title.matches(pattern))
			{
				try
				{
					int index1 = title.lastIndexOf("/");
					int index2 = title.lastIndexOf(" - ");
					int index3 = title.indexOf(" - ");
					String fileName = title.substring(index1+1, index2);
					String pack = title.substring(index3+3, index1);
					title = fileName + "(" + pack + ")";
				}catch(Exception e)
				{
				}
			}
		}
		else if("WINWORD.EXE".equals(app))
		{
			
		}
		
		return title;
	}
}
