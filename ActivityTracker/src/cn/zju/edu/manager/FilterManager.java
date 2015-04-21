package cn.zju.edu.manager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import cn.zju.edu.blf.db.DBImpl;

public class FilterManager {
	private static FilterManager instance = null;
	private String user;
	//private DBImpl db;
	private Set<String> blackListOfWeb = new HashSet<String>();
	
	private FilterManager() 
	{ 
		user = System.getProperty("user.name");
		
		initBlackListOfWeb();
	}

	public static FilterManager getInstance()
	{
      if(instance == null) {
    	  synchronized(FilterManager.class) 
    	  {
    		  FilterManager temp = instance;
            if(temp == null) {
               temp = new FilterManager();
               instance = temp;
            }
         }
      }

      return instance;
   }
	
	private void initBlackListOfWeb()
	{
		try
		{
			//java.net.URL url = FilterManager.class.getResource("/config/black.txt");
			//BufferedReader br = new BufferedReader(new FileReader(url.getPath()));
			BufferedReader br = new BufferedReader(new InputStreamReader(DBImpl.class.getResourceAsStream("/config/black.txt"))); 
			
			String line = br.readLine();
			while(line != null)
			{
				if(!"".equals(line.trim()))
				{
					blackListOfWeb.add(line);
				}
				line = br.readLine();
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			
		}
	}
	
	public boolean isFilter(String title)
	{
		for(String s : blackListOfWeb)
		{
			if(title.contains(s))
			{
				//System.out.println(title + ": " + s);
				return true;
			}
		}
		return false;
	}
}
