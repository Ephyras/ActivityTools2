package cn.zju.edu.util;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.dao.KeyPair;
import cn.zju.edu.blf.dao.SearchQuery;
import cn.zju.edu.blf.db.DBImpl;

public class QuerySeqUtil {

	public static List<String> segQuery(String query)
	{
		List<String> keys = new ArrayList<String>();
		try
		{
			Reader r = new StringReader(query);
			
			IKSegmenter seg = new IKSegmenter(r, true);
			
			Lexeme t = seg.next();
			while(t != null)
			{
				keys.add(t.getLexemeText());
				
				t = seg.next();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return keys;
	}
	
	public static void main(String[] args) throws Exception
	{
		HashSet<SearchQuery> queries = new HashSet<SearchQuery>();
		HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
		HashMap<KeyPair, Integer> keypairMap = new HashMap<KeyPair, Integer>();
		
		DBImpl db = new DBImpl();
		List<GroupedInteraction> list = db.getGroupInteractionsGreaterThan(null, "baolingfeng");
		
		for(int i=0; i<list.size(); i++)
		{
			GroupedInteraction g = list.get(i);
			
			SearchQuery sq = getSearchQuery(g);
			if(g.getDuration() > 1 && sq != null)
			{
				queries.add(sq);
			}
		}
		
		for(SearchQuery sq : queries)
		{
			List<String> keys = segQuery(sq.getQuery());
			for(int i=0; i<keys.size(); i++)
			{
				String k = keys.get(i);
				if(keyMap.containsKey(k))
				{
					keyMap.put(k, keyMap.get(k)+1);
				}
				else
				{
					keyMap.put(k, 1);
				}
				
				for(int j=i+1; j<keys.size(); j++)
				{
					String k2 = keys.get(j);
					KeyPair pair = new KeyPair(k, k2);
					if(keypairMap.containsKey(pair))
					{
						keypairMap.put(pair, keypairMap.get(pair)+1);
					}
					else
					{
						keypairMap.put(pair, 1);
					}
				}
			}
		}
		
		PrintWriter writer = new PrintWriter("D:/temp/node.csv", "gb2312");
		writer.println("Node;Label");
		for(Entry<String, Integer> entry : keyMap.entrySet())
		{
			writer.println(entry.getKey()+";"+entry.getKey());
		}
		writer.close();
		
		writer = new PrintWriter("D:/temp/edge.csv", "gb2312");
		writer.println("Source;Target;Type;Label;Weight");
		//HashMap<KeyPair, Integer> map2 = CommonUtil.sortByValuesDesc(keypairMap);
		for(Entry<KeyPair, Integer> entry : keypairMap.entrySet())
		{
			if(entry.getValue() > 1)
			{
				writer.println(entry.getKey().getK1()+";"+entry.getKey().getK2()+";"+"Undirected;;"+entry.getValue());
			}
		}
		writer.close();
	}
	
	protected static SearchQuery getSearchQuery(GroupedInteraction g)
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
