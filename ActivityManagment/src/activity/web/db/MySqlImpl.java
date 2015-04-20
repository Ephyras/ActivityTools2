package activity.web.db;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.util.InteractionUtil;

public class MySqlImpl {
	Logger logger = Logger.getLogger(MySqlImpl.class.getName());
	
	private static String URL = "jdbc:mysql://155.69.147.247:3306/hci";
	private static String USER_NAME = "blf";
	private static String PASSWORD = "123456";
	
	private Connection connection;
	
	private void readDBConfig()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(MySqlImpl.class.getResourceAsStream("/config/db.txt"))); 
			String line = br.readLine();

	        while (line != null) {
	            String[] params = line.split("=");
	        	if("HOST".equals(params[0]))
	        	{
	        		URL = params[1];
	        	}
	        	else if("USER".equals(params[0]))
	        	{
	        		USER_NAME = params[1];
	        	}
	        	else if("PASSWORD".equals(params[0]))
	        	{
	        		PASSWORD = params[1];
	        	}
	            
	            line = br.readLine();
	        }
		}catch(Exception e)
		{
			logger.info("read db config error: " + e.getMessage());
		}
	}
	
	public MySqlImpl()
	{
		try
		{
			readDBConfig();
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager
			          .getConnection(URL + "?user=" +  USER_NAME + "&password=" + PASSWORD +"&useUnicode=true&characterEncoding=utf8");
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public DBResultSet retrieveResultSet(String sql)
	{	
		Statement stmt = null;
		ResultSet rs = null;
		
		DBResultSet drs = new DBResultSet();
		
		try
		{
			stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			
			rs = stmt.executeQuery(sql);
			
			ResultSetMetaData rsmd = rs.getMetaData();
			
			while(rs.next())
			{
				DBRecord r = new DBRecord();
				for(int i=1; i<=rsmd.getColumnCount(); i++)
				{
					String columnName = rsmd.getColumnLabel(i);
					String columnType = rsmd.getColumnTypeName(i);
					
					drs.addColumn(columnName, columnType);
					if(columnType.contains("BLOB"))
					{
						try
						{
							BufferedImage img = ImageIO.read(rs.getBinaryStream(columnName));
							r.add(columnName, img);
						}catch(Exception e)
						{
							r.add(columnName, rs.getBinaryStream(columnName));
						}
						
					}
					else
					{
						r.add(columnName, rs.getObject(columnName));
					}
				}
				drs.addRecord(r);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(stmt != null) stmt.close();
				if(rs != null) rs.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return drs;
	}
	
	public boolean insert(String sql, Object... params)
	{
		//logger.info("insert database: " + sql + " #params: " + params);
		try
		{
			PreparedStatement pStat = connection.prepareStatement(sql);
			
			for(int i=1; i<=params.length; i++)
			{
				Object o = params[i-1];
				if(o instanceof Integer)
				{
					pStat.setInt(i, (int)o);
				}
				else if(o instanceof String)
				{
					pStat.setString(i, (String)o);
				}
				else if(o instanceof Double)
				{
					pStat.setDouble(i, (double)o);
				}
				else 
				{
					pStat.setObject(i, o);
				}
			}
			
			pStat.executeUpdate();
			pStat.close();
		}catch(Exception e)
		{
			logger.info(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public int insertWithAutoId(String sql, Object... params)
	{
		//logger.info("insert database: " + sql + " #params: " + params);
		try
		{
			PreparedStatement pStat = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			for(int i=1; i<=params.length; i++)
			{
				Object o = params[i-1];
				if(o instanceof Integer)
				{
					pStat.setInt(i, (int)o);
				}
				else if(o instanceof String)
				{
					pStat.setString(i, (String)o);
				}
				else if(o instanceof Double)
				{
					pStat.setDouble(i, (double)o);
				}
				else 
				{
					pStat.setObject(i, o);
				}
			}
			
			pStat.executeUpdate();
			
			ResultSet rs = pStat.getGeneratedKeys();
			int autoId = -1;
			if(rs.next())
			{
				autoId =  rs.getInt(1);
			}
			
			rs.close();
			pStat.close();
			
			return autoId;
		}catch(Exception e)
		{
			logger.info(e.getMessage());
			return -1;
		}
	}
	
	public boolean update(String sql, Object... params)
	{
		PreparedStatement pStat = null;
		try
		{
			pStat = connection.prepareStatement(sql);
			
			for(int i=1; i<=params.length; i++)
			{
				Object o = params[i-1];
				if(o instanceof Integer)
				{
					pStat.setInt(i, (int)o);
				}
				else if(o instanceof String)
				{
					pStat.setString(i, (String)o);
				}
				else if(o instanceof Double)
				{
					pStat.setDouble(i, (double)o);
				}
				else 
				{
					pStat.setObject(i, o);
				}
			}
			
			pStat.executeUpdate();
			
			pStat.close();
			
			return true;
		}catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean executeCall(String sql, Object... params)
	{
		CallableStatement cs = null;
		try
		{
			cs = connection.prepareCall(sql);
			
			for(int i=1; i<=params.length; i++)
			{
				Object o = params[i-1];
				if(o instanceof Integer)
				{
					cs.setInt(i, (int)o);
				}
				else if(o instanceof String)
				{
					cs.setString(i, (String)o);
				}
				else if(o instanceof Double)
				{
					cs.setDouble(i, (double)o);
				}
				else if(o instanceof BufferedImage)
				{
					InputStream is = null;
					if(o != null)
					{
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write((BufferedImage)o, "png", os);
						is = new ByteArrayInputStream(os.toByteArray());
					}
					cs.setBlob(4, is);
				}
				else 
				{
					cs.setObject(i, o);
				}
			}
			
			cs.executeUpdate();
			
			return true;
		}catch(Exception e)
		{
			return false;
		}finally
		{
			try{ cs.close(); }catch(Exception e){logger.info(e.getMessage());};
		}
	}
	
	public void close()
	{
		try
		{
			this.connection.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		MySqlImpl db = new MySqlImpl();
		
		DBResultSet rs = db.retrieveResultSet("select * from tbl_interactions where user_name = 'baolingfeng' and timestamp > '2015-03-20 17:56:54.411' order by timestamp limit 0,10");
		
		for(Entry<String, String> e : rs.getcMeta().entrySet())
		{
			System.out.println(e.getKey() + "/" + e.getValue());
		}
		
		LowLevelInteraction u = InteractionUtil.fromDBRecord(rs.getRecords().get(0));
		
	}
}
