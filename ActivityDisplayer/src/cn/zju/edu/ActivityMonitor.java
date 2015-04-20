package cn.zju.edu;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.List;

import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import cn.zju.edu.DataManager;
import cn.zju.edu.blf.dao.CResource;

import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import cn.zju.edu.manager.*;
import cn.zju.edu.swing.CurrentActivityPane;
import cn.zju.edu.swing.HistoryActivityPane;
import cn.zju.edu.swing.SearchQueryPane;
import cn.zju.edu.swing.TopicPane;
import cn.zju.edu.timeline.TimelineExample;

import java.awt.GridLayout;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.UIManager;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;

public class ActivityMonitor {

	private JFrame frame;
	
	private DataManager dm;
	
	protected CurrentActivityPane curScrollPane;
	protected HistoryActivityPane historyScrollPane;
	protected SearchQueryPane searchPane;
	protected TopicPane topicPane;
	
	protected GroupInteractionMananger groupManager = new GroupInteractionMananger();
	private JPanel panel;
	private JTextField textField;
	private JButton btnNewButton;
	private JPanel panel_1;
	private JPanel toolPanel;
	private JButton btnTimeline;
	private JButton settingButton;
	//protected HistoryActivityManager histManager = HistoryActivityManager.getInstance();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActivityMonitor window = new ActivityMonitor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ActivityMonitor() throws Exception
	{
		dm = new DataManager();
		initialize();
	}
	
	private void setAppIcon() throws Exception
	{
		URL url = ActivityMonitor.class.getResource("/icons/appicon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		frame.setIconImage(img);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws Exception
	{
		frame = new JFrame("Activity Monitor&Finder");
		frame.setBounds(100, 100, 397, 783);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		frame.setResizable(false);
		setAppIcon();
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);
		
		curScrollPane = new CurrentActivityPane();
		tabbedPane.addTab("Current Activity", null, curScrollPane, null);
		
		historyScrollPane = new HistoryActivityPane();
		tabbedPane.addTab("History Application", null, historyScrollPane, null);
		
		searchPane = new SearchQueryPane();
		tabbedPane.addTab("Online Search", null, searchPane, null);
		
		topicPane = new TopicPane();
		tabbedPane.addTab("Clustered Topic", null, topicPane, null);
		
		toolPanel = new JPanel();
		frame.getContentPane().add(toolPanel, BorderLayout.SOUTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnTimeline = new JButton("Activity Timeline");
		toolPanel.add(btnTimeline);
		
		settingButton = new JButton("Setting");
		settingButton.setIcon(new ImageIcon(ActivityMonitor.class.getResource("/icons/setting.png")));
		settingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		toolPanel.add(settingButton);
		btnTimeline.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				TimelineExample test = new TimelineExample();
		        test.setBlockOnOpen(true);
		        test.open();
			}
		});
		
		
		Runnable r = new Runnable() {
		      public void run() {
		        try {
		        	runBackground();
		        } catch (Exception x) {
		          x.printStackTrace();
		        }
		      }
		 };

	    Thread internalThread = new Thread(r, "background thread");
	    internalThread.start();
	}
	
	public void runBackground()
	{
		int INTERVAL_TIME = 60 * 60 * 1000;
		try
		{
			while(true)
			{
				System.out.println("run background process....");
				groupManager.groupInteractions();
				
				//HistoryActivityManager.getInstance().retrieveHistroy();
				
				historyScrollPane.createNodes();
				
				searchPane.createNodes();
				topicPane.createNodes();
				
				HistoryActivityManager.getInstance().processScreenImage();
				
				Thread.sleep(INTERVAL_TIME);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
