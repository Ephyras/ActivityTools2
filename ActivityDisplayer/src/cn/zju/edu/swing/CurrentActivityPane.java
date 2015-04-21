package cn.zju.edu.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import cn.zju.edu.blf.dao.ActivityObject;
import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.db.DBImpl;
import cn.zju.edu.manager.*;
import cn.zju.edu.util.*;
import cn.zju.edu.swt.table.*;

public class CurrentActivityPane extends JPanel{
	
	//protected HashMap<String, DefaultMutableTreeNode> appNodes;
	
	protected DefaultTreeModel treeModel;
	//protected JTree tree;
	
	ActivityTree tree;
	
	protected CurrentActivityManager manager = new CurrentActivityManager();
	protected HistoryActivityManager hist;
	
	private Thread internalThread;
	
	public CurrentActivityPane() throws Exception
	{
	    super(new BorderLayout());
	    
	    tree = new ActivityTree(1);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane, BorderLayout.CENTER);
	    
	    Runnable r = new Runnable() {
		      public void run() {
		        try {
		          runWork();
		        } catch (Exception x) {
		          x.printStackTrace();
		        }
		      }
		 };

	    internalThread = new Thread(r, "current activity");
	    internalThread.start();
	    
	}
	
	
	public void runWork()
	{
		try
		{
			if(manager.retrieveInteractions())
			{
				tree.createNodes(manager.getOrderedMap());
				//createNodes();
			}
			
			int i = 0;
			while(true)
			{
				Thread.sleep(5 * 1000);
				
				if(manager.retrieveInteractions())
				{
					tree.createNodes(manager.getOrderedMap());
				}
				
				i++;
				this.repaint();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	 
}
