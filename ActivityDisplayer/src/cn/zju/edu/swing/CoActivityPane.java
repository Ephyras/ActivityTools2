package cn.zju.edu.swing;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTree;

import cn.zju.edu.manager.HistoryActivityManager;
import cn.zju.edu.manager.IconManager;

public class CoActivityPane extends JPanel  {
	
	String title;
	String app;
	JLabel label;
	ActivityTree tree;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CoActivityPane frame = new CoActivityPane();
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
	public CoActivityPane() {
		setLayout(new BorderLayout(0, 0));
		 
		tree = new ActivityTree();
		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		
		label = new JLabel("<press Alt+A to activate coordinated activies in application windows>");
		add(label, BorderLayout.NORTH);
		
		
		//add(tree, BorderLayout.CENTER);
	}
	
	public void setFocusWindow(String title, String app)
	{
		if(title == null || app == null)
		{
			label.setText("<press Alt+A to activate coordinated activies in application windows>");
			label.setIcon(null);
			return;
		}
		
		this.title = title;
		label.setText(title);
		label.setIcon(IconManager.getIcon(app));
		this.app = app;
		this.requestFocus();
		this.invalidate();
	}
	
	public void createTree()
	{
		try
        {
			System.out.println("Active window title: " + title + " / " + app);
			tree.createNodes(HistoryActivityManager.getInstance().getCoordinatedActivities(title, app));
			
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
}
