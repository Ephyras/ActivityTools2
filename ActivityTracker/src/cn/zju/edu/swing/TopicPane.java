package cn.zju.edu.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.carrot2.core.Cluster;

import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.manager.IconManager;
import cn.zju.edu.util.*;

public class TopicPane extends JPanel implements ActionListener{
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode node;
	protected DefaultMutableTreeNode node2;
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	
	private int beforeDay = 14;
	
	public TopicPane() throws Exception
	{
		super(new BorderLayout());
		
		rootNode = new DefaultMutableTreeNode("Root Node");
	    treeModel = new DefaultTreeModel(rootNode);
	    tree = new JTree(treeModel);
	    tree.setRootVisible(false);
	    tree.setCellRenderer(new TopicTreeCellRenderer());
	    
	    node = new DefaultMutableTreeNode("Clustered Topic");
	    node2 = new DefaultMutableTreeNode("Clustered Topic(Nearest " + beforeDay +" days)");
	    
	    rootNode.add(node);
	    rootNode.add(node2);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane, BorderLayout.CENTER);
	    
	    addContextMenu();
	}
	
	public void createNodes()
	{
		node.removeAllChildren();
		
		try
		{
			List<Cluster> clustersByTopic = HistoryActivityManager.getInstance().clusterWebTitleTopic(null);
		
			for(int i=0; i<clustersByTopic.size(); i++)
		     {
		    	 Cluster c = clustersByTopic.get(i);
		    	 
		    	 if(c.getAllDocuments().size() > 10 && !"Other Topics".equalsIgnoreCase(c.getLabel()))
		    	 {
		    		 DefaultMutableTreeNode varNode = new DefaultMutableTreeNode(c.getLabel());
		    		 treeModel.insertNodeInto(varNode , node, node.getChildCount());
		    		 for(int j=0; j<c.getAllDocuments().size(); j++)
		    		 {
		    			 treeModel.insertNodeInto(new DefaultMutableTreeNode(c.getAllDocuments().get(j).getContentUrl()), varNode, varNode.getChildCount());
		    		 }
		    	 }
		    	 
		    	 //System.out.println(c.getLabel() + " : " +c.getAllDocuments().size());
		     }
			
			setClusterTopicByDay();
			
			treeModel.reload();
			//for(int i=0; i<tree.getRowCount(); i++)
			//{
			//	tree.expandRow(i);
			//}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void setClusterTopicByDay() throws Exception
	{
		node2.removeAllChildren();
		
		String dateFilter = DateUtil.getDayBeforeOrAfter(new Date(), -beforeDay);
		List<Cluster> clustersByTopic2 = HistoryActivityManager.getInstance().clusterWebTitleTopic(dateFilter);
		
		int num = 0;
		for(int i=0; i<clustersByTopic2.size(); i++)
	    {
	    	 Cluster c = clustersByTopic2.get(i);
	    	 
	    	 if(c.getAllDocuments().size() > 1 && num < 10 && !"Other Topics".equalsIgnoreCase(c.getLabel()))
	    	 {
	    		 DefaultMutableTreeNode varNode = new DefaultMutableTreeNode(c.getLabel());
	    		 treeModel.insertNodeInto(varNode , node2, node2.getChildCount());
	    		 for(int j=0; j<c.getAllDocuments().size(); j++)
	    		 {
	    			 treeModel.insertNodeInto(new DefaultMutableTreeNode(c.getAllDocuments().get(j).getContentUrl()), varNode, varNode.getChildCount());
	    		 }
	    		 
	    		 num++;
	    	 }
	     }
	}
	
	public void addContextMenu()
	{
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Set Day Number for Clustering");
		mi.addActionListener(this);
		mi.setActionCommand("setday");
		popup.add(mi);
		 
		
		tree.addMouseListener(new MouseAdapter() {
	            public void mouseReleased( MouseEvent e )
	            {
	            	TreePath path = tree.getSelectionPath();
	        		if(path == null) return;
	        		
	             	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
	             	Object o = dmtn.getUserObject();
	             	String label = o.toString();
	             	if(label.startsWith("Clustered Topic(Nearest"))
	             	{
		            	if(e.isPopupTrigger())
		            	{
		            		popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
		            	}
	             	}
	            }
		});
	            
	}
	
	 public void actionPerformed(ActionEvent ae) 
	 {
		TreePath path = tree.getSelectionPath();
		if(path == null) return;
		
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
		try
		{
			if(ae.getActionCommand().equals("setday"))
			{
				beforeDay = Integer.parseInt(JOptionPane.showInputDialog("Enter a Number for Day"));
				setClusterTopicByDay();
				
				treeModel.reload();
				dmtn.setUserObject("Clustered Topic(Nearest " + beforeDay +" days)");
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	 }
	 
	 class TopicTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

		 TopicTreeCellRenderer() {
	     }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
        	 super.getTreeCellRendererComponent(tree, value, selected, expanded,
                     leaf, row, hasFocus);
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        	 
        	if(node.getParent() == node.getRoot())
        	{
        		Icon icon = IconManager.getIcon("Star");
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
