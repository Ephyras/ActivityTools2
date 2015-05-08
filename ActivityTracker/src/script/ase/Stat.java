package script.ase;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.opencsv.CSVWriter;

import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.util.CommonUtil;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.InteractionUtil;

public class Stat {
	private static List<String> CLUSTER_FILTER_WORDS = new ArrayList<String>();
	public static String folder = "D:/temp/ase/";
	
	public static HashMap<String, GroupStat> all_stats = new LinkedHashMap<String, GroupStat>();
	
	public static void main(String[] args) throws Exception
	{
		readFilterWords();
		
		DBImpl db = new DBImpl();
		List<String> users = db.getAllUsers();
		
		String startDay = "2015-04-15";
		int totalDay = 30;
		
		for(String user : users)
		{
			doStat(user, startDay, totalDay);
		}
		
		//all_stats.put("all", new GroupStat(totalDay));
		
		String fileName3 = folder  + "all_"+startDay + "_" + totalDay + ".csv";
		CSVWriter writer3 = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName3), "UTF-8"));
		
		String title = "Application#Number of Activity#1 Day#2-4 Day#5-7 Day#8-10 Day#>10 Day";
		writer3.writeNext(title.split("#"));
		
		for(Entry<String, GroupStat> entry : all_stats.entrySet())
		{
			int total = entry.getValue().total();
			String s = entry.getKey() + "#" + total + "#";
			s += entry.getValue().level1 + "(" + new DecimalFormat("#.##").format(entry.getValue().level1*100.0/total) + "%)#"
					+entry.getValue().level2+ "(" + new DecimalFormat("#.##").format(entry.getValue().level2*100.0/total)  + "%)#"
					+entry.getValue().level3+ "(" + new DecimalFormat("#.##").format(entry.getValue().level3*100.0/total)  + "%)#"
					+entry.getValue().level4+ "(" + new DecimalFormat("#.##").format(entry.getValue().level4*100.0/total)  + "%)#"
					+entry.getValue().level5+ "(" + new DecimalFormat("#.##").format(entry.getValue().level5*100.0/total)  + "%)#";
			
			writer3.writeNext(s.split("#"));
		}
		writer3.close();
		
		db.close();
	}
	
	public static void doStat(String user, String startDay, int totalDay) throws Exception
	{
		
		//String folder = "D:/temp/ase/";
		
		String fileName = folder + user + "_"+startDay + "_" + totalDay + ".csv";
		
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		String title = "Date#From#To#Number of Activity#Number of Distinct Activity#"
				+ "Estimated Activty Duration(Hour)#Work Hour#Number of Low Level Action#Number of Activity Happen more than once";
		writer.writeNext(title.split("#"));
		
		String curDay = startDay;
		List<GroupedInteraction> allGroups = new ArrayList<GroupedInteraction>();
		int numDay = 0;
		int numAction = 0;
		int numScreen = 0;
		double duration1 = 0;
		double duration2 = 0;
		String res = "";
		
		for(int i=0; i<totalDay; i++)
		{
			OneDayStat dayStat = new OneDayStat(curDay, user);
			allGroups.addAll(dayStat.getGroups());
			
			if(dayStat.getGroups().size()<=0) 
			{
				curDay = DateUtil.nextDay(curDay);
				continue;
			}
			
			numDay += 1;
			numAction += dayStat.getNumAction();
			numScreen += dayStat.getNumScreen();
			duration1 += dayStat.getTotalDuration();
			duration2 += DateUtil.calcInterval(dayStat.getFrom(), dayStat.getTo());
			
			res = curDay + "#";
			res += dayStat.getFrom() + "#";
			res += dayStat.getTo() + "#";
			
			res += dayStat.getGroups().size()+"#";
			res += dayStat.getAggrGroups().size()+"#";
			res += dayStat.getTotalDuration()/3600+"#";
			res += DateUtil.calcInterval(dayStat.getFrom(), dayStat.getTo())/3600+"#";
			res += dayStat.getNumAction()+"#";
			res += dayStat.getMultiGroup()+"#";
			
			writer.writeNext(res.split("#"));
			
			/*
			for(Entry<String, Integer> entry : dayStat.getAppTransit().entrySet())
			{
				res = entry.getKey() + "#" + entry.getValue();
				writer.writeNext(res.split("#"));
			}
			*/
			curDay = DateUtil.nextDay(curDay);
		}
		writer.writeNext(new String[]{""});
		
		HashMap<String, Integer> appTransit = new HashMap<String, Integer>();
		int innerTransit = 0;
		int outerTransit = 0;
		for(int i=0; i<allGroups.size()-1; i++)
		{
			String fromApp = getAppCategory(allGroups.get(i).getApplication());
			String toApp = getAppCategory(allGroups.get(i+1).getApplication());
			
			if(fromApp.equals(toApp)) 
			{
				innerTransit += 1;
			}
			else
			{
				outerTransit += 1;
			}
			
			String key = fromApp + " -> " + toApp;
			if(appTransit.containsKey(key))
			{
				appTransit.put(key, appTransit.get(key)+1);
			}
			else
			{
				appTransit.put(key, 1);
			}
		}
		
		for(Entry<String, Integer> entry : appTransit.entrySet())
		{
			res = entry.getKey() + "#" + entry.getValue();
			writer.writeNext(res.split("#"));
		}
		
		writer.writeNext(new String[]{""});
		
		List<GroupedInteraction> agroups = aggrGroups(allGroups);
		
		res = numDay + "#";
		res += allGroups.size() + "#";
		res += agroups.size() + "#";
		res += numAction + "#";
		res += numScreen + "#";
		res += duration1/3600 + "#";
		res += duration1/3600/numDay + "#";
		res += duration2/3600 + "#";
		res += duration2/3600/numDay + "#";
		res += innerTransit + "#";
		res += outerTransit + "#";
		writer.writeNext(res.split("#"));
		
		writer.close();
		
		String fileName2 = folder + user + "_"+startDay + "_" + totalDay + "_2.csv";
		CSVWriter writer2 = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName2), "UTF-8"));
		
		title = "Activity Title#Day Number";
		writer2.writeNext(title.split("#"));
		
		HashMap<GroupedInteraction, Integer> groupMap = new HashMap<GroupedInteraction, Integer>();
		for(int i=0; i<agroups.size(); i++)
		{
			GroupedInteraction g = agroups.get(i);
			
			int num = 1;
			for(int j=0; j<g.getDetails().size(); j++)
			{
				String t1 = g.getDetails().get(j).getTime();
				
				if(j<g.getDetails().size()-1)
				{
					String t2 = g.getDetails().get(j+1).getTime();
					
					if(!DateUtil.isSameDay(t1, t2))
					{
						num += 1;
					}
				}
			}
			
			groupMap.put(g, num);
		}
		
		HashMap<GroupedInteraction, Integer> sortedGroupMap = CommonUtil.sortByValuesDesc(groupMap);
		
		HashMap<String, GroupStat> stats = new LinkedHashMap<String, GroupStat>();
		stats.put("all", new GroupStat(numDay));
		if(!all_stats.containsKey("all"))
		{
			all_stats.put("all", new GroupStat(numDay));
		}
		
		for(Entry<GroupedInteraction, Integer> entry : sortedGroupMap.entrySet())
		{
			int days = entry.getValue();
			
			String key = getAppCategory(entry.getKey().getApplication());
			if(!stats.containsKey(key))
			{
				stats.put(key, new GroupStat(numDay));
			}
			
			if(!all_stats.containsKey(key))
			{
				all_stats.put(key, new GroupStat(numDay));
			}
			
			if(days == 1)
			{
				stats.get("all").level1 += 1; 
				stats.get(key).level1 += 1; 
				
				all_stats.get("all").level1 += 1; 
				all_stats.get(key).level1 += 1; 
			}
			else if(days>=2 && days<=4)
			{
				stats.get("all").level2 += 1; 
				stats.get(key).level2 += 1; 
				
				all_stats.get("all").level2 += 1; 
				all_stats.get(key).level2 += 1; 
			}
			else if(days>=5 && days<=7)
			{
				stats.get("all").level3 += 1; 
				stats.get(key).level3 += 1; 
				
				all_stats.get("all").level3 += 1; 
				all_stats.get(key).level3 += 1; 
			}
			else if(days>=8 && days<=10)
			{
				stats.get("all").level4 += 1; 
				stats.get(key).level4 += 1; 
				
				all_stats.get("all").level4 += 1; 
				all_stats.get(key).level4 += 1; 
			}
			else
			{
				stats.get("all").level5 += 1; 
				stats.get(key).level5 += 1; 
				
				all_stats.get("all").level5 += 1; 
				all_stats.get(key).level5 += 1; 
			}
			
			if(entry.getValue() <= 1) continue;
			
			res = entry.getKey().getApplication()+"#";
			res += entry.getKey().getTitle()+"#";
			res += entry.getValue().toString();
			
			writer2.writeNext(res.split("#"));
		}
		
		writer2.close();
		
		String fileName3 = folder + user + "_"+startDay + "_" + totalDay + "_3.csv";
		CSVWriter writer3 = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName3), "UTF-8"));
		
		title = "Application#Number of Activity#1 Day#2-4 Day#5-7 Day#8-10 Day#>10 Day";
		writer3.writeNext(title.split("#"));
		
		for(Entry<String, GroupStat> entry : stats.entrySet())
		{
			int total = entry.getValue().total();
			String s = entry.getKey() + "#" + total + "#";
			s += entry.getValue().level1 + "(" + new DecimalFormat("#.##").format(entry.getValue().level1*100.0/total) + "%)#"
					+entry.getValue().level2+ "(" + new DecimalFormat("#.##").format(entry.getValue().level2*100.0/total)  + "%)#"
					+entry.getValue().level3+ "(" + new DecimalFormat("#.##").format(entry.getValue().level3*100.0/total)  + "%)#"
					+entry.getValue().level4+ "(" + new DecimalFormat("#.##").format(entry.getValue().level4*100.0/total)  + "%)#"
					+entry.getValue().level5+ "(" + new DecimalFormat("#.##").format(entry.getValue().level5*100.0/total)  + "%)#";
			
			writer3.writeNext(s.split("#"));
		}
		writer3.close();
	}
	
	public static List<GroupedInteraction> aggrGroups(List<GroupedInteraction> groups)
	{
		List<GroupedInteraction> aggrGroups = new ArrayList<GroupedInteraction>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			String from = g.getDetails().get(0).getTime();
			String to = g.getDetails().get(g.getDetails().size()-1).getTime();
			
			if(g.getDuration() < 5 || isFilter(g.getTitle()))
			{
				//System.out.println(g.getTitle() + " - short - " + g.getDuration());
				continue;
			}
			
			int k = aggrGroups.indexOf(g);
			if(k >= 0)
			{
				aggrGroups.get(k).setDuration(aggrGroups.get(k).getDuration() + g.getDuration());
				aggrGroups.get(k).addDetail(g.getDetails());
				
				aggrGroups.get(k).addTimeslot(from, to);
			}
			else
			{
				GroupedInteraction newG = new GroupedInteraction();
				newG.setApplication(g.getApplication());
				newG.setTitle(g.getTitle());
				newG.setDuration(g.getDuration());
				newG.addDetail(g.getDetails());
				
				newG.addTimeslot(from, to);
				aggrGroups.add(newG);
			}
			
		}
		
		return  aggrGroups;
	}
	
	public static boolean isFilter(String title)
	{
		if("".equals(title.trim())) return true;
		
		for(int i=0; i<CLUSTER_FILTER_WORDS.size(); i++)
		{
			if(title.contains(CLUSTER_FILTER_WORDS.get(i)))
			{
				//System.out.println(title);
				return true;
			}
		}
		
		return false;
	}
	
	public static void readFilterWords()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(Stat.class.getResourceAsStream("/script/ase/filter.txt"))); 
			
			String line = br.readLine();
			while(line != null && !"".equals(line))
			{
				CLUSTER_FILTER_WORDS.add(line);
				
				line = br.readLine();
			}
			
			br.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getAppCategory(String app)
	{
		if(InteractionUtil.isBrowser(app))
		{
			return "Browser";
		}
		else if("eclipse.exe".equalsIgnoreCase(app) || "javaw.exe".equalsIgnoreCase(app))
		{
			return "Eclipse";
		}
		else if("devenv.exe".equalsIgnoreCase(app))
		{
			return "Visual Studio";
		}
		else if("winword.exe".equalsIgnoreCase(app) || "excel.exe".equalsIgnoreCase(app) || "POWERPNT.EXE".equals(app))
		{
			return "Office Document";
		}
		else
		{
			//System.out.println(app);
			return "Other Applications";
		}
			
	}
	
}
