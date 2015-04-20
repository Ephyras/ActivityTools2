package activity.web.postproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;
import activity.web.db.dao.JSTreeDao;
import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.Key;
import cn.zju.edu.blf.dao.KeyPair;
import cn.zju.edu.blf.dao.SearchQuery;
import cn.zju.edu.util.CommonUtil;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.InteractionUtil;
import cn.zju.edu.util.QuerySeqUtil;

public class QueryProcesser {
	
	private List<String> queries = new ArrayList<String>();
	List<Key> _keys = new ArrayList<Key>();
	private List<KeyPair> _keypairs = new ArrayList<KeyPair>();
	
	public String process(String timestamp, List<GroupedInteraction> interactions)
	{
		//HashMap<String, Integer> map = getKeywords(getQueries(timestamp, interactions));
		this.process1(timestamp, interactions);
		this.process2();
		
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("keys", _keys);
		jsonMap.put("pairs", _keypairs);
		
		JSONObject jsonObject = JSONObject.fromObject(jsonMap); 
		
		return jsonObject.toString();
	}
	
	public void process2()
	{
		for(int i=0; i<queries.size(); i++)
		{
			String q = queries.get(i);
			List<String> keys = QuerySeqUtil.segQuery(q);
			for(int j=0; j<keys.size(); j++)
			{
				Key key = new Key();
				key.setKey(keys.get(j));
				int n = _keys.indexOf(key);
				if(n<0)
				{
					_keys.add(key);
				}
				else
				{
					_keys.get(n).setNumber(_keys.get(n).getNumber()+1);
				}
				
				for(int k=j+1; k<keys.size(); k++)
				{
					KeyPair kp = new KeyPair();
					kp.setK1(keys.get(j));
					kp.setK2(keys.get(k));
					
					int m = _keypairs.indexOf(kp);
					if(m < 0)
					{
						_keypairs.add(kp);
					}
					else
					{
						_keypairs.get(m).setNumber(_keypairs.get(m).getNumber() + 1);
					}
				}
			}
		}
	}
	
	public void process1(String timestamp, List<GroupedInteraction> interactions)
	{
		for(GroupedInteraction g : interactions)
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
				
				SearchQuery q = getSearchQuery(g);
				if(q == null) continue;
				
				if(queries.indexOf(q.getQuery()) < 0)
				{
					queries.add(q.getQuery());
				}
			}
		}
		
	}
	
	public static SearchQuery getSearchQuery(GroupedInteraction g)
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
				index = title.indexOf("_°Ù¶ÈËÑË÷");
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
}
