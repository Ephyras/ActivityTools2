package cn.zju.edu.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;

import java.awt.GridLayout;
import java.util.Set;

import cn.zju.edu.blf.dao.*;
import cn.zju.edu.manager.*;

public class OpenedPageWindow extends JFrame {

	private JPanel contentPane;
	protected JTree tree;
	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel treeModel;
	    
	private SearchQuery query;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OpenedPageWindow frame = new OpenedPageWindow(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public OpenedPageWindow(SearchQuery query) {
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.query = query;
		
		rootNode = new DefaultMutableTreeNode("Root Node");
		treeModel = new DefaultTreeModel(rootNode);
		
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		panel.add(tree);
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					loadOpenedPages();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r, "loadOpenedPage");
		t.start();
	}
	
	public void loadOpenedPages()
	{
		try
		{
			Set<String> pages = HistoryActivityManager.getInstance().getOpenedWebPage(query);
			for(String p : pages)
			{
				treeModel.insertNodeInto(new DefaultMutableTreeNode(p), rootNode, rootNode.getChildCount());
			}
			treeModel.reload();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
