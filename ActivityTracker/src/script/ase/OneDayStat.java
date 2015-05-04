package script.ase;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.util.DateUtil;

public class OneDayStat {
	String day;
	String user;
	
	List<GroupedInteraction> groups;
	List<GroupedInteraction> aggrGroups;
	
	double totalDuration = 0;
	double numAction = 0;
	int multiGroup = 0;
	int numScreen = 0;
	String from;
	String to;
	HashMap<String, Integer> appTransit = new HashMap<String, Integer>();
	
	public OneDayStat(String day, String user)
	{
		this.day = day;
		this.user = user;
		
		from = day + " 24:00:00.000";
		to = day + " 00:00:00.000";
		
		try
		{
			DBImpl db = new DBImpl();
			
			groups = db.getGroupInteractionsInDay(day, user);
			
			Collections.sort(groups, new Comparator<GroupedInteraction>(){
				public int compare(GroupedInteraction g1, GroupedInteraction g2) {
					try
					{
						int sz1 = g1.getDetails().size();
						int sz2 = g2.getDetails().size();
					
					
						double interval = DateUtil.calcInterval(g1.getDetails().get(sz1-1).getTime(), 
								g2.getDetails().get(sz2-1).getTime());
						
						if(interval < 0)
						{
							return 1;
						}
						else if(interval == 0)
						{
							return 0;
						}
						else
						{
							return -1;
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					return -1;
				}
			});
			
			/*
			for(int i=0; i<groups.size()-1; i++)
			{
				//int sz = groups.get(i).getDetails().size();
				//System.out.println(groups.get(i).getDetails().get(sz-1).getTime() + "/ " + groups.get(i).getApplication());
				
				String fromApp = Stat.getAppCategory(groups.get(i).getApplication());
				String toApp = Stat.getAppCategory(groups.get(i+1).getApplication());
				
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
			*/
			
			aggrGroups = Stat.aggrGroups(groups);
			
			for(int i=0; i<aggrGroups.size(); i++)
			{
				GroupedInteraction g = aggrGroups.get(i);
				
				totalDuration += g.getDuration();
				int sz = g.getDetails().size();
				numAction += sz;
				
				int numG = 1;
				for(int j=0; j<sz; j++)
				{
					String t = g.getDetails().get(j).getTime();
					if(from.compareTo(t)>0)
					{
						from = t;
					}
					
					if(to.compareTo(t)<0)
					{
						to = t;
					}
					
					if(g.getDetails().get(j).getScreenStatus() == 1)
					{
						numScreen += 1;
					}
					
					if(j<sz-1)
					{
						if(g.getDetails().get(j).getGroupId() != g.getDetails().get(j+1).getGroupId())
						{
							numG += 1;
						}
					}
				}
				if(numG > 1)
				{
					multiGroup += 1;
				}
				
			}
			
			db.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getNumScreen() {
		return numScreen;
	}

	public void setNumScreen(int numScreen) {
		this.numScreen = numScreen;
	}

	public HashMap<String, Integer> getAppTransit()
	{
		return appTransit;
	}
	
	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<GroupedInteraction> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupedInteraction> groups) {
		this.groups = groups;
	}

	public List<GroupedInteraction> getAggrGroups() {
		return aggrGroups;
	}

	public void setAggrGroups(List<GroupedInteraction> aggrGroups) {
		this.aggrGroups = aggrGroups;
	}

	public double getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(double totalDuration) {
		this.totalDuration = totalDuration;
	}

	public double getNumAction() {
		return numAction;
	}

	public void setNumAction(double numAction) {
		this.numAction = numAction;
	}

	public int getMultiGroup() {
		return multiGroup;
	}

	public void setMultiGroup(int multiGroup) {
		this.multiGroup = multiGroup;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	
}
