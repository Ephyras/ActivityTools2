package cn.zju.edu.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;

import javax.swing.JTextField;

import cn.zju.edu.blf.dao.LowLevelInteraction;
import cn.zju.edu.blf.dao.ScreenImage;
import cn.zju.edu.manager.*;
import cn.zju.edu.util.*;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class ActivityScreenWindow extends JFrame{

	private JPanel contentPane;
	private JTextField textField;
	private JLabel imgLabel;
	private ScabledLabelPanel scaledLabelPane;
	
	
	private List<ScreenImage> images;
	private Thread loadThread;
	private String title;
	private String app;
	
	private int currentIndex = -1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActivityScreenWindow frame = new ActivityScreenWindow("test", "firefox.exe");
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
	public ActivityScreenWindow(String title, String app) {
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//images = fromLLInteractions(interactions);
		
		this.title = title;
		this.app = app;
		
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		scaledLabelPane = new ScabledLabelPanel(null, true);
		
		//imgLabel = new JLabel("Loading....");
		panel.add(scaledLabelPane, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton = new JButton("Previous");
		panel_1.add(btnNewButton);
		
		textField = new JTextField();
		textField.setEditable(false);
		panel_1.add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("Next");
		panel_1.add(btnNewButton_1);
		
		Runnable r = new Runnable() {
		      public void run() {
		        try {
		        	loading();
		        } catch (Exception x) {
		          x.printStackTrace();
		        }
		      }
		 };

	    Thread internalThread = new Thread(r, "loading image");
	    internalThread.start();
	}
	
	protected void loading()
	{
		try
		{
			List<LowLevelInteraction> interactions = HistoryActivityManager.getInstance().getLLInteractionsWithScreen(title, app);
			images = fromLLInteractions(interactions);
			
			if(images.size() > 0)
			{
				//Image scaledImg = ImageUtil.getScaledImage(images.get(0).getImage(), imgLabel.getWidth(), imgLabel.getHeight());
				
				scaledLabelPane.setMaster(images.get(0).getImage());
				scaledLabelPane.repaint();
				
				//ImageIcon icon = new ImageIcon(scaledImg);
				//imgLabel.setIcon(icon);
				currentIndex = 0;
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	public List<ScreenImage> fromLLInteractions(List<LowLevelInteraction> interactions)
	{
		List<ScreenImage> list = new ArrayList<ScreenImage>();
		if(interactions == null) return list;
		
		for(int i=0; i<interactions.size(); i++)
		{
			LowLevelInteraction interaction = interactions.get(i);
			if(interaction.isHasScreen())
			{
				ScreenImage newSi = new ScreenImage();
				newSi.setImage(interaction.getScreen());
				newSi.addInteraction(interaction);
				newSi.setTime(interaction.getTimestamp());
				list.add(newSi);
			}
		}
		
		return list;
	}
}
