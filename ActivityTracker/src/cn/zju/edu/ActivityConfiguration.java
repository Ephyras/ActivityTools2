package cn.zju.edu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.db.DBImpl;


public class ActivityConfiguration {
	static Logger logger = Logger.getLogger(ActivityConfiguration.class.getName());
	
	private String URL = "jdbc:mysql://155.69.147.247:3306/hci";
	private String USER_NAME = "blf";
	private String PASSWORD = "123456";
	private String WEB_APPLICATION = "http://155.69.147.247:8080/ActivityManagment";
	private boolean JAVAFX = true;
	
	private static ActivityConfiguration instance = null;
	
	private void readDBConfig()
	{
		try
		{
			InputStreamReader reader = null;
			
			File f = new File("db.txt");
			if(f.exists())
			{
				logger.info("read from current directory");
				reader = new InputStreamReader(new FileInputStream(f));
			}
			else
			{
				reader = new InputStreamReader(DBImpl.class.getResourceAsStream("/config/db.txt")); 
			}
			
			BufferedReader br = new BufferedReader(reader);
			
			String line = br.readLine();

	        while (line != null) {
	            String[] params = line.split("=");
	        	if("HOST".equals(params[0]))
	        	{
	        		URL = params[1];
	        		int index = URL.indexOf("://");
	        		if( index < 0)
	        		{
	        			throw new Exception("url is invlid: " + URL);
	        		}
	        		else 
	        		{
	        			URL = "jdbc:mysql" + URL.substring(index);
	        		}
	        	}
	        	else if("USER".equals(params[0]))
	        	{
	        		USER_NAME = params[1];
	        	}
	        	else if("PASSWORD".equals(params[0]))
	        	{
	        		PASSWORD = params[1];
	        	}
	        	else if("WEBAPPLICATION".equals(params[0]))
	        	{
	        		WEB_APPLICATION = params[1];
	        	}
	        	else if("JAVAFX".equals(params[0]))
	        	{
	        		JAVAFX = "TRUE".equalsIgnoreCase(params[1]) ? true : false;
	        	}
	            
	            line = br.readLine();
	        }
		}catch(Exception e)
		{
			logger.info("read db config error: ", e);
		}
	}
	
	private ActivityConfiguration()
	{
		readDBConfig();
	}
	
	public static ActivityConfiguration getInstance()
	{
      if(instance == null) {
    	  synchronized(ActivityConfiguration.class) 
    	  {
    		ActivityConfiguration temp = instance;
            if(temp == null) {
               temp = new ActivityConfiguration();
               instance = temp;
            }
         }
      }

      return instance;
   }

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getUSER_NAME() {
		return USER_NAME;
	}

	public void setUSER_NAME(String uSER_NAME) {
		USER_NAME = uSER_NAME;
	}

	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	public String getWEB_APPLICATION() {
		return WEB_APPLICATION;
	}

	public void setWEB_APPLICATION(String wEB_APPLICATION) {
		WEB_APPLICATION = wEB_APPLICATION;
	}

	public boolean isJAVAFX() {
		return JAVAFX;
	}

	public void setJAVAFX(boolean jAVAFX) {
		JAVAFX = jAVAFX;
	}
	
}
