package cn.zju.edu.swing;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import cn.zju.edu.manager.FilterManager;
import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.manager.IconManager;
import cn.zju.edu.blf.dao.*;
import cn.zju.edu.util.*;

public class HistoryActivityPane  extends JPanel{
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode node1;
	protected DefaultMutableTreeNode node2;
	protected DefaultMutableTreeNode node3;
	protected DefaultMutableTreeNode node4;
	protected DefaultMutableTreeNode node5;
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	
	//protected HistoryActivityManager hist;
	
	public HistoryActivityPane() throws Exception
	{
		super(new GridLayout(1, 0));
	    
		//hist = HistoryActivityManager.getInstance();
		
	    rootNode = new DefaultMutableTreeNode("Root Node");
	    treeModel = new DefaultTreeModel(rootNode);
	    tree = new JTree(treeModel);
	    tree.setRootVisible(false);
	    tree.setCellRenderer(new HistoryTreeCellRenderer());
	    
	    //tree.setCellRenderer(new ActivityTreeCellRenderer());
	   
	    node1 = new DefaultMutableTreeNode("Web page");
	    node2 = new DefaultMutableTreeNode("Eclipse");
	    node3 = new DefaultMutableTreeNode("Visual Studio");
	    node4 = new DefaultMutableTreeNode("Document");
	    node5 = new DefaultMutableTreeNode("Other Applications");
	    
	    rootNode.add(node1);
	    rootNode.add(node2);
	    rootNode.add(node3);
	    rootNode.add(node4);
	    rootNode.add(node5);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane);
	    
	    createNodes();
	}
	
	public void createNodes() throws Exception
	{
		node1.removeAllChildren();
		node2.removeAllChildren();
		node3.removeAllChildren();
		node4.removeAllChildren();
		node5.removeAllChildren();
		
		HistoryActivityManager.getInstance().retrieveHistroy();
		int numOfBrowser = 0;
		int numOfEclipse = 0;
		int numOfVS = 0;
		int numOfWord = 0;
		int numOfOther = 0;
		
		List<GroupedInteraction> groups = HistoryActivityManager.getInstance().getAggrGroup();
		for(int i=0; i<groups.size(); i++)
		{
			GroupedInteraction g = groups.get(i);
			if(InteractionUtil.isBrowser(g.getApplication()))
			{
				if(FilterManager.getInstance().isFilter(g.getTitle())) continue;
				
				if(numOfBrowser > 10) continue;
				
				treeModel.insertNodeInto(new DefaultMutableTreeNode(g) , node1, node1.getChildCount());
				//node1.add(new DefaultMutableTreeNode(g));
				numOfBrowser++;
			}
			else if("eclipse.exe".equals(g.getApplication()) || "javaw.exe".equals(g.getApplication()))
			{
				if(numOfEclipse > 10) continue;
				
				treeModel.insertNodeInto(new DefaultMutableTreeNode(g) , node2, node2.getChildCount());
				//node2.add(new DefaultMutableTreeNode(g));
				numOfEclipse++;
			}
			else if("devenv.exe".equals(g.getApplication()))
			{
				if(numOfVS > 10) continue;
				
				treeModel.insertNodeInto(new DefaultMutableTreeNode(g) , node3, node3.getChildCount());
				//node3.add(new DefaultMutableTreeNode(g));
				numOfVS++;
			}
			else if("WINWORD.EXE".equals(g.getApplication()))
			{
				if(numOfWord > 10) continue;
				
				treeModel.insertNodeInto(new DefaultMutableTreeNode(g) , node4, node4.getChildCount());
				//node4.add(new DefaultMutableTreeNode(g));
				numOfWord++;
			}
			else
			{
				if(numOfOther > 10) continue;
				
				treeModel.insertNodeInto(new DefaultMutableTreeNode(g) , node5, node5.getChildCount());
				//node5.add(new DefaultMutableTreeNode(g));
				numOfOther++;
			}
		}
		
		treeModel.reload();
		for(int i=0; i<tree.getRowCount(); i++)
		{
			tree.expandRow(i);
		}
	}
	
	class HistoryTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

		HistoryTreeCellRenderer() {
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
        	 super.getTreeCellRendererComponent(tree, value, selected, expanded,
                     leaf, row, hasFocus);
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        	 
        	Object o = node.getUserObject();
            if (o instanceof GroupedInteraction) {
            	GroupedInteraction g = (GroupedInteraction) o;
            	Icon icon = IconManager.getIcon(g.getApplication());
            	if(icon == null)
            	{
            		icon = IconManager.getIcon("default");
            	}
            	
            	this.setIcon(icon);
            	this.setText(g.getTitle());
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
