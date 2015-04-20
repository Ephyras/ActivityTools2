package cn.zju.edu.test;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

import cn.zju.edu.blf.dao.GroupedInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.util.InteractionUtil;

public class Carrot2Test {
	public static void main(String[] args) throws Exception
	{
		DBImpl db = new DBImpl();
		
		HashSet<String> titleSet = new HashSet<String>();
		List<GroupedInteraction> list = db.getGroupInteractionsGreaterThan(null, "baolingfeng");
		
		for(int i=0; i<list.size(); i++)
		{
			GroupedInteraction g = list.get(i);
			if(!InteractionUtil.isBrowser(g.getApplication()))
			{
				titleSet.add(list.get(i).getTitle());
			}
		}
		
		PrintWriter writer = new PrintWriter("D:/temp/titles2.txt");
		for(String s : titleSet)
		{
			writer.println(s);
		}
		writer.close();
	}
}
