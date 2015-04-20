package cn.zju.edu.manager;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.LanguageCode;
import org.carrot2.core.ProcessingResult;

import cn.zju.edu.blf.dao.ActivityObject;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.dao.CodeChange;
import cn.zju.edu.blf.dao.SearchQuery;
import cn.zju.edu.blf.dao.ActionDetail;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.timeline.model.TimelineEvent;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.CompareUtil;
import cn.zju.edu.util.InteractionUtil;
import cn.zju.edu.util.CommonUtil;
import cn.zju.edu.util.ImageUtil;
import de.jaret.util.ui.timebars.model.DefaultRowHeader;
import de.jaret.util.ui.timebars.model.DefaultTimeBarModel;
import de.jaret.util.ui.timebars.model.DefaultTimeBarRowModel;

public class HistoryActivityManager {
	private DBImpl db;
	private String lastTime;
	private String user = System.getProperty("user.name");
	
	private List<GroupedInteraction> groups = new ArrayList<GroupedInteraction>();
	private List<GroupedInteraction> aggrGroups = new ArrayList<GroupedInteraction>();
	//private List<SearchQuery> queries = new ArrayList<SearchQuery>();
	private HashMap<SearchQuery, String> queries = new HashMap<SearchQuery, String>();
	//protected HashMap<GroupedInteraction, Double> aggrGroups;
	
	private List<String> CLUSTER_FILTER_WORDS = new ArrayList<String>();
	
	private static HistoryActivityManager instance = null;

	private HistoryActivityManager() throws Exception
	{ 
		user = System.getProperty("user.name");
		db = new DBImpl();
		
		//readFilterWords();
	}
	
	private void readFilterWords()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(HistoryActivityManager.class.getResourceAsStream("/config/cluster_filter_words.txt"))); 
			
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
	
	public static HistoryActivityManager getInstance() throws Exception
	{
      if(instance == null) {
    	  synchronized(HistoryActivityManager.class) 
    	  {
        	 HistoryActivityManager temp = instance;
            if(temp == null) {
               temp = new HistoryActivityManager();
               instance = temp;
            }
         }
      }

      return instance;
   }
	
	public void retrieveHistroy() throws Exception
	{
		groups = db.getGroupInteractionsGreaterThan(null, user);
		//groups.addAll(list);
		aggrGroups.clear();
		
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			String from = g.getDetails().get(0).getTime();
			String to = g.getDetails().get(g.getDetails().size()-1).getTime();
			
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
		System.out.println("acitivies size: " + groups.size() + "/" + aggrGroups.size());
		
		//Collections.sort(aggrGroups);
		//Collections.reverse(aggrGroups);
		
		//clusterWebTitleTopic();
	}
	
	public SearchQuery getSearchQuery(GroupedInteraction g)
	{
		SearchQuery sq = null;
		if(InteractionUtil.isBrowser(g.getApplication()))
		{
			String title = g.getTitle();
			int index = title.indexOf(" - Google Search");
			if(index >=0)
			{
				sq = new SearchQuery();
				sq.setQuery(title.substring(0, index));
				sq.setEngine("Google");
				sq.setApplication(g.getApplication());
				return sq;
			}
			else
			{
				index = title.indexOf("_百度搜索");
				if(index >= 0)
				{
					sq = new SearchQuery();
					sq.setQuery(title.substring(0, index));
					sq.setEngine("Baidu");
					sq.setApplication(g.getApplication());
					return sq;
				}
			}
		}
		
		return sq;
	}
	
	public Set<String> getOpenedWebPage(SearchQuery s) throws Exception
	{
		Set<String> openedWebpages = new LinkedHashSet<String>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g1 = groups.get(i);
			if(g1.getApplication().equals(s.getApplication()))
			{
				String title = s.getQuery();
				if("Google".equals(s.getEngine()))
				{
					title += " - Google Search";
				}
				else if("Baidu".equals(s.getEngine()))
				{
					title += "_百度搜索";
				}
				
				if(title.equals(g1.getTitle()))
				{
					if(g1.getDuration() < 2 || g1.getDetails().size()<=0) continue;
					
					String t1 = g1.getDetails().get(0).getTime();
					for(int j=i+1; j<groups.size(); j++)
					{
						GroupedInteraction g2 = groups.get(j);
						if(g2.getDetails().size() <= 0 || !g2.getApplication().equals(g1.getApplication())) continue;
						
						String t2 = g2.getDetails().get(0).getTime();
						if(DateUtil.calcInterval(t1, t2) > 60) break;
						
						openedWebpages.add(g2.getTitle());
					}
				}
			}
		}
		return openedWebpages;
	}
	
	public List<GroupedInteraction> getAggrGroup()
	{
		return this.aggrGroups;
	}
	
	public boolean hasGroup(String title, String app)
	{
		for(int i=0; i<aggrGroups.size(); i++)
		{
			if(aggrGroups.get(i).getTitle().equals(title) && aggrGroups.get(i).getApplication().equals(app))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<ActivityObject> getCoordinatedActivities(String title, String app) throws Exception
	{
		int MAX_INTERVAL = 1 * 60;
		String limitDate = DateUtil.getDayBeforeOrAfter(new Date(), -7);
		
		List<ActivityObject> res = new ArrayList<ActivityObject>();
		
		int idx = -1;
		for(int i=0; i<aggrGroups.size(); i++)
		{
			GroupedInteraction g = aggrGroups.get(i);
			
			if( g.getTitle().equals(title) &&  g.getApplication().equals(app))
			{
				for(int j=0; j<g.getTimeslots().size(); j+=2)
				{
					String from1 = g.getTimeslots().get(j);
					String to1 = g.getTimeslots().get(j+1);
					
					if(DateUtil.calcInterval(from1, limitDate) > 0) continue;
					
					for(int m=0; m<aggrGroups.size(); m++)
					{
						GroupedInteraction g2 = aggrGroups.get(m);
						
						//boolean flag = false;
						for(int n=0; n<g2.getTimeslots().size(); n+=2)
						{
							String from2 = g2.getTimeslots().get(n);
							String to2= g2.getTimeslots().get(n+1);
							
							if(DateUtil.calcInterval(from2, to2) < 2) continue;
							
							if(DateUtil.calcInterval(from2, limitDate) > 0) continue;
							
							if(!(DateUtil.calcInterval(to1, from2) > MAX_INTERVAL ||  
									DateUtil.calcInterval(to2, from1) >MAX_INTERVAL))
							{
								ActivityObject ao = new ActivityObject();
								ao.setApplication(g2.getApplication());
								ao.setTitle(g2.getTitle());
								ao.setLastTime(g2.getTimeslots().get(g2.getTimeslots().size()-1));
								if(res.indexOf(ao)<0)
								{
									res.add(ao);
								}
								//flag = true;
								break;
							}
						}
						//if(flag) break;
					}
				}
			}
		}
		
		
		return res;
	}
	
	public List<LowLevelInteraction> getLLInteractions(String title, String app) throws Exception
	{
		List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				for(int j=0; j<g.getDetails().size(); j++)
				{
					LowLevelInteraction ll = db.getAnInteractions(g.getDetails().get(j).getTime(), user);
					if(ll != null) list.add(ll);
				}
			}
		}
		return list;
	}
	
	public List<ActionDetail> getLLInteractionsForDetail(String title, String app) throws Exception
	{
		List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
		for(int i=0; i<aggrGroups.size(); i++)
		{
			GroupedInteraction g = aggrGroups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				for(int j=0; j<g.getDetails().size(); j++)
				{
					LowLevelInteraction ll = db.getAnInteractionsWithScreen2(g.getDetails().get(j).getTime(), user, true);
					if(ll != null) 
					{
						
						list.add(ll);
					}
					
				}
				break;
			}
		}
		
		Set<ActionDetail> actions = new LinkedHashSet<ActionDetail>();
		for(int i=0; i<list.size(); i++)
		{
			LowLevelInteraction ll = list.get(i);
			
			if(InteractionUtil.isControlType("tab item", ll.getUiType()) || 
					InteractionUtil.isControlType("pane", ll.getUiType()) ||
					InteractionUtil.isControlType("window", ll.getUiType())) 
				continue;
			
			if("eclipse.exe".equals(app) || "javaw.exe".equals(app))
			{
				if(InteractionUtil.isControlType("edit", ll.getUiType()) && 
						(ll.getParentUiName().contains(".java") || "Source".equalsIgnoreCase(ll.getParentUiName())))
				{
					continue;
				}
			}
			
			ActionDetail ad = new ActionDetail();
			ad.setControlType(ll.getUiType());
			ad.setParent(ll.getParentUiName());
			ad.setTime(ll.getTimestamp());
			String action = "";
			if("".equals(ll.getUiName()) && "".equals(ll.getUiValue()))
			{
				action = "No Accessibility Information";
			}
			else if("".equals(ll.getUiValue()))
			{
				action += ll.getUiName();
			}
			else if("".equals(ll.getUiName()))
			{
				action += ll.getUiValue();
			}
			else
			{
				action += ll.getUiName() + "(" + ll.getUiValue() + ")";
			}
			ad.setAction(action);
			
			if("No Accessibility Information".equals(action) || (InteractionUtil.isControlType("button", ll.getUiType())))
			{
				int left = ll.getUiBoundLeft();
				int top = ll.getUiBoundTop();
				int right = ll.getUiBoundRight();
				int bottom = ll.getUiBoundBottom();
				int w = right - left;
				int h = bottom - top;
				
				if(ll.isHasScreen())
				{
					BufferedImage img = ll.getScreen();
					if(InteractionUtil.isControlType("button", ll.getUiType()))
					{
						img = img.getSubimage(left, top, w, h);
					}
					else
					{
						img = ImageUtil.drawCircleOnImage(img, ll.getPx(), ll.getPy(), 10);
						img = ImageUtil.drawRectOnImage(img, left, top, w, h);
					}
					ad.setImg(img);
				}
				else
				{
					for(int j=i-1; j>=0; j--)
					{
						if(list.get(j).isHasScreen())
						{
							BufferedImage img = list.get(j).getScreen();
							if(InteractionUtil.isControlType("button", ll.getUiType()))
							{
								img = img.getSubimage(left, top, w, h);
							}
							else
							{
								img = ImageUtil.drawCircleOnImage(img, ll.getPx(), ll.getPy(), 10);
								img = ImageUtil.drawRectOnImage(img, left, top, w, h);
							}
							ad.setImg(img);
							break;
						}
					}
				}
			}
			System.out.println("action hash code:" + ad.hashCode());
			actions.add(ad);
		}
		
		List<ActionDetail> list2 = new ArrayList<ActionDetail>(actions);
		
		Collections.sort(list2);
		
		return list2;
	}
	
	public List<LowLevelInteraction> getLLInteractionsWithScreen(String title, String app) throws Exception
	{
		List<LowLevelInteraction> list = new ArrayList<LowLevelInteraction>();
		for(int i=0; i<aggrGroups.size(); i++)
		{
			GroupedInteraction g = aggrGroups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				for(int j=0; j<g.getDetails().size(); j++)
				{
					LowLevelInteraction ll = db.getAnInteractionsWithScreen2(g.getDetails().get(j).getTime(), user, true);
					if(ll != null) list.add(ll);
				}
				break;
			}
		}
		return list;
	}
	
	public List<CodeChange> getJavaCodeChange(String title, String app) throws Exception
	{
		List<CodeChange> changes = new ArrayList<CodeChange>();
		for(int i=0; i<aggrGroups.size(); i++)
		{
			GroupedInteraction g = aggrGroups.get(i);
			if(g.getTitle().equals(title) && g.getApplication().equals(app))
			{
				int j = g.getDetails().size()-1;
				for(; j>=0; j--)
				{
					LowLevelInteraction ll = db.getAnInteractions(g.getDetails().get(j).getTime(), user);
					if(ll != null && (InteractionUtil.isControlType("edit", ll.getUiType()) && ll.getParentUiName().contains(".java")))
					{
						if(changes.size() <= 0)
						{
							CodeChange c = new CodeChange();
							c.setChange("");
							c.setTime(ll.getTimestamp());
							String source = ll.getUiValue().replaceAll("\\\\n", "\n");
							source = source.replaceAll("\\\\t", "\t");
							c.setSource(source);
							changes.add(c);
						}
						else
						{
							String lasttime = changes.get(changes.size()-1).getTime();
							String lastSource = changes.get(changes.size()-1).getSource();
							if(DateUtil.calcInterval(ll.getTimestamp(), lasttime) > 10 * 60)
							{
								String source = ll.getUiValue().replaceAll("\\\\n", "\n");
								source = source.replaceAll("\\\\t", "\t");
								
								String changeText = CompareUtil.compareText(source, lastSource);
								if(!"".equals(changeText))
								{
									CodeChange c = new CodeChange();
									c.setChange(changeText);
									c.setTime(ll.getTimestamp());
									c.setSource(source);
									changes.add(c);
								}
							}
						}
						
						if(changes.size() >= 10) break;
					}
					
				}
				break;
			}
		}
		return changes;
	}
	
	public HashMap<SearchQuery, String> getSearchQueries()
	{
		return CommonUtil.sortByValuesDesc(queries);
	}
	
	public void processScreenImage() throws Exception
	{
		for(int i=0; i<aggrGroups.size(); i++)
		{
			BufferedImage pre = null;
			for(int j=0; j<aggrGroups.get(i).getDetails().size(); j++)
			{
				String time = aggrGroups.get(i).getDetails().get(j).getTime();
				int screenStatus = aggrGroups.get(i).getDetails().get(j).getScreenStatus();
				if(screenStatus > 0)
				{
					continue;
				}
				
				if(j > 0)
				{
					//...
				}
				
				BufferedImage cur = null;
				screenStatus = 2;
				
				LowLevelInteraction ll = db.getAnInteractionsWithScreen(time, user);
				if(ll == null)
				{
					System.out.println(time);
				}
				
				if(ll != null && ll.isHasScreen())
				{
					if(pre != null)
					{
						double thr = CompareUtil.compareImage(pre, ll.getScreen());
						if(thr < 0.8)
						{
							screenStatus = 1;
							cur = ll.getScreen();
						}
						System.out.println("correlation: " +thr);
					}
					else
					{
						cur = ll.getScreen();
						screenStatus = 1;
					}
					pre = ll.getScreen();
				}
				
				db.updateGroupDetail(user, time, screenStatus, cur);
				aggrGroups.get(i).getDetails().get(j).setScreenStatus(screenStatus);
			}
		}
	}
	
	public DefaultTimeBarModel createTimeModel() throws Exception
	{
		DefaultTimeBarModel model = new DefaultTimeBarModel();
		DefaultTimeBarRowModel row = new DefaultTimeBarRowModel(new DefaultRowHeader("events"));
		for(int i=0; i<groups.size(); i++)
		{
			if(groups.get(i).getDuration() < 10) 
			 {
				 continue;
			 }
			 
			 TimelineEvent event = new TimelineEvent();
			 
			 String title = groups.get(i).getTitle();
			 event.setTitle(title);
		     event.setContent(title);
		     
		     int sz = groups.get(i).getDetails().size();
		     if(sz > 0)
		     {
			     String start = groups.get(i).getDetails().get(0).getTime();
			     String end = groups.get(i).getDetails().get(sz-1).getTime();
			     event.setBegin(DateUtil.toJaretDate(start));
			     event.setEnd(DateUtil.toJaretDate(end));
			     event.setDuration(false);
			     event.setApplication(groups.get(i).getApplication());
		     }
		     //System.out.println(event.getSeconds());
		     
		     row.addInterval(event);
		}
		model.addRow(row);
		return model;
	}
	
	public List<Cluster> clusterWebTitleTopic(String timestamp) throws Exception
	{
		ArrayList<Document> documents = new ArrayList<Document>();
		
		for(GroupedInteraction g : aggrGroups)
		{
			if(InteractionUtil.isBrowser(g.getApplication()))
			{
				if(timestamp != null && !"".equals(timestamp))
				{
					if(g.getDetails().size() >= 0)
					{
						String gt = g.getDetails().get(g.getDetails().size()-1).getTime();
						
						if( DateUtil.calcInterval(timestamp, gt) < 0 )
						{
							continue;
						}
					}
				}
				
				String title = g.getTitle();
				for(int i=0; i<CLUSTER_FILTER_WORDS.size(); i++)
				{
					if(g.getTitle().contains(CLUSTER_FILTER_WORDS.get(i)))
					{
						title = title.replaceAll(CLUSTER_FILTER_WORDS.get(i), "");
					}
				}
				if(title.startsWith("http://"))
				{
					continue;
				}
				
				Document doc = new Document(title,title,g.getTitle());
			    doc.setLanguage(LanguageCode.CHINESE_SIMPLIFIED);
			    documents.add(doc);
			}
		}
		
		final Controller controller = ControllerFactory.createSimple();
		 
		 LingoClusteringAlgorithm al = new LingoClusteringAlgorithm();
		 al.desiredClusterCountBase = 30;
		 
		 Map<String, Object> attributes = new HashMap<String, Object>(); 
		
		 attributes.put("LingoClusteringAlgorithm.desiredClusterCountBase", 10); 
		 
         controller.init(attributes);
         
         final ProcessingResult byTopicClusters = controller.process(documents, "*",LingoClusteringAlgorithm.class);
		 
		         
		 return byTopicClusters.getClusters();
		 
	}
	
	public static void main(String[] args) throws Exception
	{
		HistoryActivityManager hist = new HistoryActivityManager();
		
		hist.retrieveHistroy();
	}
}
