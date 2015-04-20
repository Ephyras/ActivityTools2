package cn.zju.edu.swing;

import java.awt.AWTEvent;
import java.awt.Event;
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
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import cn.zju.edu.manager.*;
import cn.zju.edu.timeline.TimelineExample;

import java.awt.GridLayout;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.UIManager;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.PointerByReference;

//import com.sun.glass.events.KeyEvent;

public class MainWindow implements HotkeyListener, IntellitypeListener {

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
					MainWindow window = new MainWindow();
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
	public MainWindow() throws Exception
	{
		dm = new DataManager();
		initialize();
		
		registerHotKey();
	}
	
	private void setAppIcon() throws Exception
	{
		URL url = MainWindow.class.getResource("/icons/appicon.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		frame.setIconImage(img);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws Exception
	{
		frame = new JFrame("Activity Tracker");
		frame.setBounds(100, 100, 397, 783);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		frame.setResizable(false);
		setAppIcon();
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);
		
		curScrollPane = new CurrentActivityPane();
		tabbedPane.addTab("Recent Activities", null, curScrollPane, null);
		
		//historyScrollPane = new HistoryActivityPane();
		//tabbedPane.addTab("History Application", null, historyScrollPane, null);
		
		//searchPane = new SearchQueryPane();
		//tabbedPane.addTab("Online Search", null, searchPane, null);
		
		//topicPane = new TopicPane();
		//tabbedPane.addTab("Clustered Topic", null, topicPane, null);
		
		//toolPanel = new JPanel();
		//frame.getContentPane().add(toolPanel, BorderLayout.SOUTH);
		//toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.X_AXIS));
		
		
		//btnTimeline = new JButton("Activity History");
		//toolPanel.add(btnTimeline);
		/*
		btnTimeline.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				TimelineExample test = new TimelineExample();
		        test.setBlockOnOpen(true);
		        test.open();
			}
		});
		*/
		
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
				//groupManager.groupInteractions();
				
				HistoryActivityManager.getInstance().retrieveHistroy();
				
				//historyScrollPane.createNodes();
				
				//searchPane.createNodes();
				//topicPane.createNodes();
				
				//HistoryActivityManager.getInstance().processScreenImage();
				
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
	
	public void registerHotKey()
	{
		// OPTIONAL: check to see if an instance of this application is already
		// running, use the name of the window title of this JFrame for checking
		if (JIntellitype.checkInstanceAlreadyRunning("Activity Tracker")) {
		   System.out.println("An instance of this application is already running");
		   System.exit(1);
		}

		// Assign global hotkeys to Windows+A and ALT+SHIFT+B
		//JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_WIN, (int)'A');
		JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT, (int)'A');

		JIntellitype.getInstance().addHotKeyListener(this);

		JIntellitype.getInstance().addIntellitypeListener(this);
	}
	
	// listen for hotkey
	public void onHotKey(int aIdentifier) 
	{
		char[] buffer = new char[1024 * 2];
		User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, 1024);
        System.out.println("Active window title: " + Native.toString(buffer));
		
        String curWindow = Native.toString(buffer);
        
        //bring front
        frame.toFront();
        frame.repaint();
        //JFrame popup = new JFrame(curWindow);
        
        //popup.setVisible(true);
	}

	//listen for intellitype play/pause command
	public void onIntellitype(int aCommand) {
	      switch (aCommand) {
	        case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
	           System.out.println("Play/Pause message received " + Integer.toString(aCommand));
	           break;
	        default:
	           System.out.println("Undefined INTELLITYPE message caught " + Integer.toString(aCommand));
	           break;
	      }
	}
	
	static class Psapi {
        static { Native.register("psapi"); }
        public static native int GetModuleBaseNameW(Pointer hProcess, Pointer hmodule, char[] lpBaseName, int size);
    }

    static class Kernel32 {
        static { Native.register("kernel32"); }
        public static int PROCESS_QUERY_INFORMATION = 0x0400;
        public static int PROCESS_VM_READ = 0x0010;
        public static native int GetLastError();
        public static native Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, Pointer pointer);
    }

    static class User32DLL {
        static { Native.register("user32"); }
        public static native int GetWindowThreadProcessId(HWND hWnd, PointerByReference pref);
        public static native HWND GetForegroundWindow();
        public static native int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
    }
}
