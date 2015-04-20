package activity.web.manager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import jdk.nashorn.internal.parser.JSONParser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.GroupedInteraction;
import activity.web.db.dao.*;

public class EventManager {
	Logger logger = Logger.getLogger(EventManager.class.getName());
	
	public static HashMap<String, String> APP_COLOR = new HashMap<String, String>();
	static
	{
		APP_COLOR.put("eclipse", "#99ABEA");
		APP_COLOR.put("browser", "#FF9966");
		APP_COLOR.put("office", "#8F24B2");
		APP_COLOR.put("vs", "#1987AC");
		APP_COLOR.put("other", "#CCC");
	}
	
	private String user;
	
	private boolean eclipseCheck = true;
	private boolean browserCheck = true;
	private boolean officeCheck = true;
	private boolean vsCheck = true;
	private boolean otherCheck = true;
	private String filterQuery = "";
	
	private boolean useBlacklist = true;
	private ArrayList<String> blackList = new ArrayList<String>();
	
	public EventManager(String user) {
		this.user = user;
		
		readBlacklist();
	}
	
	private void readBlacklist()
	{
		try
		{
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(EventManager.class.getResourceAsStream("/config/blacklist.json"),"UTF-8"));
			
			String out = "";
			String line = reader.readLine();
			while(line != null)
			{
				out += line;
				line = reader.readLine();
			}
	        //System.out.println("jsonTxt.length " + jsonTxt.length());
	
	        JSONObject root = (JSONObject) JSONSerializer.toJSON(out);
	        
	        JSONArray array = root.getJSONArray("list");
	        for(int i=0; i<array.size(); i++)
	        {
	        	JSONObject o = (JSONObject)array.get(i);
	        	String u = o.getString("user");
	        	if(user != null && user.equals(u))
	        	{
	        		JSONArray words = o.getJSONArray("words");
	            	for(int j=0; j<words.size(); j++)
	            	{
	            		blackList.add(words.getString(j));
	            		logger.info(words.getString(j));
	            	}
	            	break;
	        	}
        	
	        }
		}catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}
	}
	
	public EventManager(String eclipseCheck_in, String browserCheck_in, String officeCheck_in, String vsCheck_in, String otherCheck_in)
	{
			if(eclipseCheck_in != null)
				this.eclipseCheck = Boolean.parseBoolean(eclipseCheck_in);
			
			if(browserCheck_in != null)
				this.browserCheck = Boolean.parseBoolean(browserCheck_in);
			
			if(officeCheck_in != null)
				this.officeCheck = Boolean.parseBoolean(officeCheck_in);
			
			if(vsCheck_in != null)
				this.vsCheck = Boolean.parseBoolean(vsCheck_in);
			
			if(otherCheck_in != null)
				this.otherCheck = Boolean.parseBoolean(otherCheck_in);
			
			logger.info(eclipseCheck + "/" + browserCheck + "/" + officeCheck + "/" + vsCheck + "/" + otherCheck);
	}
	
	protected boolean isEclipse(GroupedInteraction e)
	{
		return "eclipse.exe".equals(e.getApplication()) || "javaw.exe".equals(e.getApplication());
	}
	
	protected boolean isBrowser(GroupedInteraction e)
	{
		return "firefox.exe".equals(e.getApplication()) || "chrome.exe".equals(e.getApplication()) 
				|| "iexplore.exe".equals(e.getApplication());
	}
	
	protected boolean isOffice(GroupedInteraction e)
	{
		return "word.exe".equalsIgnoreCase(e.getApplication()) || "excel.exe".equalsIgnoreCase(e.getApplication());
	}
	
	protected boolean isVS(GroupedInteraction e)
	{
		return "devenv.exe".equalsIgnoreCase(e.getApplication());
	}
	
	protected boolean isOthers(GroupedInteraction e)
	{
		return !isEclipse(e) && !isBrowser(e) && !isOffice(e) && !isVS(e);
	}
	
	public boolean filterEvent(GroupedInteraction e)
	{
		if(e.getDuration() < 30) return true;
		
		String title = e.getTitle();
		
		if("".equals(title.trim())) return true;
		
		if(e.getDetails().size() <= 0) return true;
		
		if(!eclipseCheck && isEclipse(e))
		{
			return true;
		}
		
		if(!browserCheck && isBrowser(e))
		{
			return true;
		}
		
		if(!officeCheck && isOffice(e))
		{
			return true;
		}
		
		if(!vsCheck && isVS(e))
		{
			return true;
		}
		
		if(!otherCheck && isOthers(e))
		{
			return true;
		}
		
		if(filterQuery != null && !"".equals(filterQuery.trim()))
		{
			String[] keys = filterQuery.split(" ");
			for(int i=0; i<keys.length; i++)
			{
				if("".equals(keys[i])) continue;
				
				if(!title.toLowerCase().contains(keys[i].toLowerCase()))
				{
					return true;
				}
			}
			
		}
		
		if(this.useBlacklist)
		{
			for(int i=0; i<blackList.size(); i++)
			{
				if(title.toLowerCase().contains(blackList.get(i).toLowerCase()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getColor(GroupedInteraction e)
	{
		if(isEclipse(e))
		{
			return APP_COLOR.get("eclipse");
		}
		else if(isBrowser(e))
		{
			return APP_COLOR.get("browser");
		}
		else if(isOffice(e))
		{
			return APP_COLOR.get("office");
		}
		else if(isVS(e))
		{
			return APP_COLOR.get("vs");
		}
		else
		{
			return APP_COLOR.get("other");
		}
	}

	public boolean isEclipseCheck() {
		return eclipseCheck;
	}

	public void setEclipseCheck(boolean eclipseCheck) {
		this.eclipseCheck = eclipseCheck;
	}
	
	public void setEclipseCheck(String eclipseCheck_in) {
		if(eclipseCheck_in != null)
			this.eclipseCheck = Boolean.parseBoolean(eclipseCheck_in);
	}
	
	public boolean isBrowserCheck() {
		return browserCheck;
	}

	public void setBrowserCheck(boolean browserCheck) {
		this.browserCheck = browserCheck;
	}
	
	public void setBrowserCheck(String browserCheck_in) {
		if(browserCheck_in != null)
			this.browserCheck = Boolean.parseBoolean(browserCheck_in);
	}
	
	public boolean isOfficeCheck() {
		return officeCheck;
	}

	public void setOfficeCheck(boolean officeCheck) {
		this.officeCheck = officeCheck;
	}

	public void setOfficeCheck(String officeCheck_in) {
		if(officeCheck_in != null)
			this.officeCheck = Boolean.parseBoolean(officeCheck_in);
	}
	
	public boolean isVsCheck() {
		return vsCheck;
	}

	public void setVsCheck(boolean vsCheck) {
		this.vsCheck = vsCheck;
	}
	
	public void setVsCheck(String vsCheck_in) {
		if(vsCheck_in != null)
			this.vsCheck = Boolean.parseBoolean(vsCheck_in);
	}
	
	public boolean isOtherCheck() {
		return otherCheck;
	}

	public void setOtherCheck(boolean otherCheck) {
		this.otherCheck = otherCheck;
	}
	
	public void setOtherCheck(String otherCheck_in) {
		if(otherCheck_in != null)
			this.otherCheck = Boolean.parseBoolean(otherCheck_in);
	}
	
	public String getFilterQuery() {
		return filterQuery;
	}

	public void setFilterQuery(String filterQuery) {
		this.filterQuery = filterQuery;
	}
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(EventManager.class.getResourceAsStream("/config/blacklist.json")));
		
		String out = "";
		String line = reader.readLine();
		while(line != null)
		{
			out += line;
			line = reader.readLine();
		}
        //System.out.println("jsonTxt.length " + jsonTxt.length());

        JSONObject root = (JSONObject) JSONSerializer.toJSON(out);
        
        JSONArray array = root.getJSONArray("list");
        for(int i=0; i<array.size(); i++)
        {
        	JSONObject o = (JSONObject)array.get(i);
        	String user = o.getString("user");
        	System.out.println("user: " + user);
        	
        	JSONArray words = o.getJSONArray("words");
        	for(int j=0; j<words.size(); j++)
        	{
        		 System.out.println(words.getString(j));
        	}
        }
	}
}
