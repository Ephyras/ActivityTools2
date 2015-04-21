package cn.zju.edu.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.zju.edu.blf.dao.*;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import activity.web.db.dao.EventDao;

public class CommonUtil {
	
	private CommonUtil() 
	{
		
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;        
	  }  
	  return true;  
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static HashMap sortByValuesDesc(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	
	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	
	/*
	public static org.eclipse.swt.graphics.Color getHSLColor(org.eclipse.swt.graphics.Device device, int h, int s, int l)
    {
    	float r, g, b;
    	float var1, var2;
    	
    	if(s == 0)
    	{
    		r = l;
    		g = l;
    		b = l;
    	}
    	else
    	{
    		if(l < 128)
    		{
    			var2 = (l * (s + 256)) / 256;
    		}
    		else
    		{
    			var2 = (l + s) - (s * l) / 256;
    		}
    		
    		if(var2 > 255)
    		{
    			var2 = Math.round(var2);
    		}
    		if(var2 > 254)
    		{
    			var2 = 255;
    		}
    		
    		var1 = 2 * l - var2;
    		r = RGBFromHue(var1, var2, h + 120);
    		g = RGBFromHue(var1, var2, h);
    		b = RGBFromHue(var1, var2, h - 120);
    	}
    	
    	r = r<0 ? 0 : r;
    	r = r>255 ? 255 : r;
    	g = g<0 ? 0 : g;
    	g = g>255 ? 255 : g;
    	b = b<0 ? 0 : b;
    	b = b>255 ? 255 : b;
    	
    	return new org.eclipse.swt.graphics.Color(device, (int)r, (int)g, (int)b);
    }
    */
	
    public static float RGBFromHue(float a, float b, float h) {
            if (h < 0) {
                h += 360;
           }
           if (h >= 360) {
                h -= 360;
          }
          if (h < 60) {
                return a + ((b - a) * h) / 60;
           }
            if (h < 180) {
                return b;
            }
     
            if (h < 240) {
                return a + ((b - a) * (240 - h)) / 60;
            }
            return a;
   }
    
    public static int LevenshteinDistance(String s0, String s1) {

        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;  
        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++)
            cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {

            // initial cost of skipping prefix in String s1
            newcost[0] = j - 1;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {

                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete),
                        cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
    
    public static double pecentageOfTextMatch(String s0, String s1) 
    {                       // Trim and remove duplicate spaces
        double percentage = 0;
        s0 = s0.trim().replaceAll("\\s+", " ");
        s1 = s1.trim().replaceAll("\\s+", " ");
        percentage=(1 - (float) LevenshteinDistance(s0, s1) * 1.0 / (float) (s0.length() + s1.length()));
        return percentage;
    }
    
    public static String toJson(List<EventDao> events)
	{
		JsonConfig jsonConfig = new JsonConfig();
		
		/*
		jsonConfig.registerJsonPropertyNameProcessor(EventDao.class,  
				new PropertyNameProcessor()
				{
					public String processPropertyName(Class beanClass, String name) 
					{
						if("detail".equals(name))
						{
							return "description";
						}
						else if("app".equals(name))
						{
							return "icon";
						}
						
						return name;
					}
				}
		);
		*/
		
		//jsonConfig.registerJsonValueProcessor(EventDao.class, "app", new EventAppJsonValueProcessor());

		jsonConfig.setJsonPropertyFilter(new PropertyFilter()
		{
			public boolean apply(Object source, String name, Object value) {
				if(source instanceof EventDao)
				{
					if(value == null || "".equals(value))
					{
						return true;
					}
				}
				return false;
			}
		});
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("events", events);
		map.put("dateTimeFormat", "Gregorian");
		
		JSONObject jsonObject = JSONObject.fromObject( map, jsonConfig);  
		return jsonObject.toString();
	}
    
    public static String toJson4CodeChange(List<CodeChangeDetail> details)
	{
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.setJsonPropertyFilter(new PropertyFilter()
		{
			public boolean apply(Object source, String name, Object value) {
				if(source instanceof CodeChangeDetail)
				{
					if(value == null || "".equals(value))
					{
						return true;
					}
				}
				return false;
			}
		});
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("data", details);
		
		JSONObject jsonObject = JSONObject.fromObject( map, jsonConfig);  
		return jsonObject.toString();
	}
    
    public static String toJson4Action(List<ActionDetail> details)
    {
    	JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.setJsonPropertyFilter(new PropertyFilter()
		{
			public boolean apply(Object source, String name, Object value) {
				if(source instanceof CodeChangeDetail)
				{
					if(value == null || "".equals(value))
					{
						return true;
					}
				}
				return false;
			}
		});
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("data", details);
		
		JSONObject jsonObject = JSONObject.fromObject( map, jsonConfig);  
		return jsonObject.toString();
    }
    
    public static void main(String[] args)
    {
    	String s1 = "Test ssss - 1st Page";
    	String s2 = "Test ssss";
    	
    	System.out.println(pecentageOfTextMatch(s1, s2));
    }
}
