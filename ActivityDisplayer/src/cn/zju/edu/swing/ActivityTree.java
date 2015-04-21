package cn.zju.edu.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import cn.zju.edu.blf.dao.ActivityObject;
import cn.zju.edu.manager.FilterManager;
import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.manager.IconManager;
import cn.zju.edu.util.DateUtil;
import cn.zju.edu.util.InteractionUtil;

public class ActivityTree extends JTree implements ActionListener{
	Logger logger = Logger.getLogger(ActivityTree.class.getName());
	
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode node1;
	protected DefaultMutableTreeNode node2;
	protected DefaultMutableTreeNode node3;
	protected DefaultMutableTreeNode node4;
	protected DefaultMutableTreeNode node5;
	
	protected DefaultTreeModel treeModel;
	
	private int parentId;
	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public ActivityTree(int parentId)
	{
		this.parentId = parentId;
		
		rootNode = new DefaultMutableTreeNode("Root Node");
	    treeModel = new DefaultTreeModel(rootNode);
	    
	    //tree = new JTree(treeModel);
	    setModel(treeModel);
	    setRootVisible(false);
	    setCellRenderer(new ActivityTreeCellRenderer());
	    
	    node1 = new DefaultMutableTreeNode("Eclipse");
	    node2 = new DefaultMutableTreeNode("Browser");
	    node3 = new DefaultMutableTreeNode("Office Document");
	    node4 = new DefaultMutableTreeNode("Visual Studio");
	    node5 = new DefaultMutableTreeNode("Other Applications");
	    
	    rootNode.add(node1);
	    rootNode.add(node2);
	    rootNode.add(node3);
	    rootNode.add(node4);
	    rootNode.add(node5);
	    
	    addContextMenu();
	}
	
	@SuppressWarnings("rawtypes")
	public void createNodes(List<ActivityObject> list)
	{
		node1.removeAllChildren();
		node2.removeAllChildren();
		node3.removeAllChildren();
		node4.removeAllChildren();
		node5.removeAllChildren();
		
		//List<ActivityObject> list = manager.getOrderedMap();
		Collections.sort(list);
		
		for(int i=0; i<list.size(); i++)
		{
			ActivityObject a = list.get(i);
			if("eclipse.exe".equals(a.getApplication()) || "javaw.exe".equals(a.getApplication()))
			{
					treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node1, node1.getChildCount());
			}
			else if(InteractionUtil.isBrowser(a.getApplication()))
			{
				if(FilterManager.getInstance().isFilter(a.getTitle())) continue;
				
				 treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node2, node2.getChildCount());
			}
			else if("WINWORD.EXE".equals(a.getApplication()) || "EXCEL.EXE".equals(a.getApplication()))
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node3, node3.getChildCount());
			}
			else if("devenv.exe".equals(a.getApplication()))
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node4, node4.getChildCount());
			}
			else
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(a) , node5, node5.getChildCount());
			}
		}
		
		treeModel.reload();
		for(int i=0; i<getRowCount(); i++)
		{
			expandRow(i);
		}
	}
	
	 public void addContextMenu()
	 {   
		 final JPopupMenu popup = new JPopupMenu();
		 JMenuItem m = new JMenuItem("Show in Activity Tracker");
		 m.addActionListener(this);
		 m.setActionCommand("show");
		 popup.add(m);
		 
		 addMouseListener(new MouseAdapter() {
	            public void mouseReleased( MouseEvent e )
	            {
	            	TreePath path = getSelectionPath();
	        		if(path == null) return;
	        		
	             	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
	             	Object o = dmtn.getUserObject();
	            	if(o instanceof ActivityObject)
	            	{
		                if (e.isPopupTrigger()) {
		                    popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
		                }
	            	}
	            	else 
	            	{
	            		return;
	            	}
	             	
	            }
		 });
	 }
	
	 public void actionPerformed(ActionEvent ae) {
			TreePath path = getSelectionPath();
			if(path == null) return;
			
	     	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
	     	Object o = dmtn.getUserObject();
	     	ActivityObject a = null;
	     	if(o instanceof ActivityObject)
	     	{
	     		a = (ActivityObject)o;
	     	}
	     	
	     	try
	     	{
		        if (ae.getActionCommand().equals("show")) {
		        	logger.info(a.getTitle() + "/" + a.getApplication());
		        	ActivityTracker.run(a.getTitle(), a.getApplication());
		        }
		        
	     	}catch(Exception e)
	     	{
	     		e.printStackTrace();
	     	}
		}
	 
	class ActivityTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

        ActivityTreeCellRenderer() {
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
        	 super.getTreeCellRendererComponent(tree, value, selected, expanded,
                     leaf, row, hasFocus);
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        	 
        	Object o = node.getUserObject();
            if (o instanceof ActivityObject) {
            	try
            	{
	            	ActivityObject a = (ActivityObject) o;
	            	Icon icon = IconManager.getIcon(a.getApplication());
	            	if(icon == null)
	            	{
	            		icon = IconManager.getIcon("default");
	            	}
	            	
	            	this.setIcon(icon);
	            	this.setText(a.getTitle());
	            	if(HistoryActivityManager.getInstance().hasGroup(a.getTitle(), a.getApplication()))
	            	{
	            		
	            		if(tree instanceof ActivityTree)
	            		{
	            			ActivityTree atree = (ActivityTree)tree;
	            			if(atree.getParentId() == 2)
	            			{
	            				//Color c = new Color(255,255,0);
			            		int day = DateUtil.getIntervalDayUtilNow(a.getLastTime());
			            		
			            		double sig = 1.0 / (1 + Math.exp(0.6 * (day-1)));
			            		
			            		int b = (int)(175 * sig + 135);
			            		b = b>255 ? 255:b;
			            		
			            		int r = 255-b;
			            		
			            		System.out.println(day + "/" + b + "/" + r);
			            		
			            		this.setForeground(new Color(r, r, b));
	            			}
	            			else
	            			{
	            				this.setForeground(Color.BLUE);
	            			}
	            		}
	            		else
	            		{
	            			this.setForeground(Color.BLUE);
	            		}
	            		
	            		
	            	}
            	}catch(Exception e)
            	{
            		e.printStackTrace();
            	}
            	
            } else if(node.getParent() == tree.getModel().getRoot())
            {
            	Icon icon = IconManager.getIcon(node.toString());
            	this.setIcon(icon);
            	this.setText("" + value);
            }	
            else	
            {
            	this.setOpenIcon(this.getDefaultOpenIcon());
            	this.setClosedIcon(this.getDefaultClosedIcon());
                this.setText("" + value);
            }
            return this;
        }
    }
}
