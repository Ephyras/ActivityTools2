package cn.zju.edu.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.manager.IconManager;
import cn.zju.edu.swing.HistoryActivityPane.HistoryTreeCellRenderer;
import cn.zju.edu.blf.dao.*;
import cn.zju.edu.util.QuerySeqUtil;
import cn.zju.edu.util.CommonUtil;

public class SearchQueryPane extends JPanel implements ActionListener{
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode node1;
	protected DefaultMutableTreeNode node2;
	protected DefaultMutableTreeNode node3;
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	
	JTextField textField;
	
	protected HashMap<SearchQuery, String> queries;
	
	public SearchQueryPane() throws Exception
	{
		super(new BorderLayout());
	    
		//hist = HistoryActivityManager.getInstance();
		
	    rootNode = new DefaultMutableTreeNode("Root Node");
	    treeModel = new DefaultTreeModel(rootNode);
	    tree = new JTree(treeModel);
	    tree.setRootVisible(false);
	    tree.setCellRenderer(new QueryTreeCellRenderer());
	   
	    node1 = new DefaultMutableTreeNode("Queries");
	    node2 = new DefaultMutableTreeNode("Most Used Keywords");
	    node3 = new DefaultMutableTreeNode("Most Used Keyword Pairs");
	    
	    rootNode.add(node1);
	    rootNode.add(node2);
	    rootNode.add(node3);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane, BorderLayout.CENTER);
	    
	    JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("Search");
		panel.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            	setFilter(textField.getText());
                //Execute when button is pressed
                System.out.println("You clicked the button");
            }
        });      
		
	    //createNodes();
	    addContextMenu();
	}
	
	public void createNodes()
	{
		node1.removeAllChildren();
		node2.removeAllChildren();
		node3.removeAllChildren();
		
		HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
		HashMap<KeyPair, Integer> keypairMap = new HashMap<KeyPair, Integer>();
		
		try
		{
			queries = HistoryActivityManager.getInstance().getSearchQueries();
			for(Entry<SearchQuery, String> entry : queries.entrySet())
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(entry.getKey()) , node1, node1.getChildCount());
				
				List<String> keys = QuerySeqUtil.segQuery(entry.getKey().getQuery());
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
						if(k.equals(k2)) continue;
						
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
			
			HashMap<String, Integer> map2 = CommonUtil.sortByValuesDesc(keyMap);
			for(Entry<String, Integer> entry2 : map2.entrySet())
			{
				if(entry2.getValue() > 5)
				{
					treeModel.insertNodeInto(new DefaultMutableTreeNode(entry2.getKey() + "(" + entry2.getValue() + ")") , node2, node2.getChildCount());
				}
			}
			
			HashMap<KeyPair, Integer> map3 = CommonUtil.sortByValuesDesc(keypairMap);
			for(Entry<KeyPair, Integer> entry3 : map3.entrySet())
			{
				if(entry3.getValue() > 5)
				{
					treeModel.insertNodeInto(new DefaultMutableTreeNode(entry3.getKey().getK1() + "/" + entry3.getKey().getK2() + "(" + entry3.getValue() + ")") , node3, node3.getChildCount());
				}
			}
			
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
	
	public void setFilter(String filter)
	{
		node1.removeAllChildren();
	
		for(Entry<SearchQuery, String> entry : queries.entrySet())
		{
			if(filter == null || "".equals(filter))
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(entry.getKey()) , node1, node1.getChildCount());
			}
			else
			{
				String[] keys = filter.split(" ");
				
				for(int i=0; i<keys.length; i++)
				{
					if(entry.getKey().getQuery().contains(keys[i]))
					{
						treeModel.insertNodeInto(new DefaultMutableTreeNode(entry.getKey()) , node1, node1.getChildCount());
						break;
					}
				}
			}
			
		}
		
		treeModel.reload();
		for(int i=0; i<tree.getRowCount(); i++)
		{
			tree.expandRow(i);
		}
	}
	
	public void addContextMenu()
	{
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Show Opened Webpage");
		mi.addActionListener(this);
		mi.setActionCommand("webpage");
		popup.add(mi);
		 
		
		tree.addMouseListener(new MouseAdapter() {
	            public void mouseReleased( MouseEvent e )
	            {
	            	TreePath path = tree.getSelectionPath();
	        		if(path == null) return;
	        		
	             	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
	             	Object o = dmtn.getUserObject();
	             	if(o instanceof SearchQuery)
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
		Object o = dmtn.getUserObject();
		SearchQuery s = null;
		if(o instanceof SearchQuery)
		{
			s = (SearchQuery)o;
		}
		
		try
		{
			if(ae.getActionCommand().equals("webpage"))
			{
				OpenedPageWindow w = new OpenedPageWindow(s);
				w.setVisible(true);
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	 }
	
	class QueryTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

		QueryTreeCellRenderer() {
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
        		return this; 
        	}
        	
        	Object o = node.getUserObject();
            if (o instanceof SearchQuery) {
            	SearchQuery g = (SearchQuery) o;
            	Icon icon = IconManager.getIcon(g.getEngine());
            	if(icon == null)
            	{
            		icon = IconManager.getIcon("default");
            	}
            	
            	this.setIcon(icon);
            	this.setText(g.getQuery());
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
