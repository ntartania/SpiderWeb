/*
 * File:         LogEventTable.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      11/08/2011
 * Last Changed: Date: 11/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import spiderweb.graph.LogEvent;
import spiderweb.visualizer.eventplayer.EventPlayer;

/**
 * LogEventTable is a swing component which has the list of events 
 * which represent the activity on the graph.  The table highlights 
 * each event as it happens.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 11/08/2011 
 */
public class LogEventTable extends JTable implements ChangeListener, ActionListener {
	
	/**eclipse generated serial UID*/
	private static final long serialVersionUID = 304553330811497820L;
	
	/**The headers for the columns*/
	public static final Object[] titles = { "Time", "Type", "Param 1", "Param 2", "Param 3" };
	
	protected EventPlayer player;
	protected JButton goToRecentEventButton;
	protected JCheckBox followEventsCheckBox;
	
	/**
	 * Constructs a JTable which displays the passed Log events and highlights each 
	 * event as it happens through the playback.
	 * @param logEvents The events to be displayed in the table
	 * @param player The EventPlayer which is playing through the list of events.
	 */
	public LogEventTable(List<LogEvent> logEvents, EventPlayer player) {
		super();
		this.player = player;
		init(logEvents);
	}

	/**
	 * helper method for initializing the table and all the 
	 * other components which it depends on.
	 * @param logEvents
	 */
	private void init(List<LogEvent> logEvents) {
		
		//Converts the list of log events to an object[][] for use in the TableModel
		Object[][] data = new Object[logEvents.size()][5];
		int i=0;
		for(LogEvent evt : logEvents) {
			data[i] = evt.toArray();
			i++;
		}
		this.setModel(new DefaultTableModel(data, titles)); //create the table model
		
		//set various options for the table
		setBackground(Color.LIGHT_GRAY);
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setEnabled(false);
		setColumnSelectionAllowed(false);
		
		//create the option buttons
		goToRecentEventButton = new JButton("Go To Latest Event");
		goToRecentEventButton.addActionListener(this);
		followEventsCheckBox = new JCheckBox("Follow Events");
		
		setVisible(true);
	}
	
	/**
	 * Adds the passed events to the table model for viewing in the table.
	 * @param events The events to add.
	 */
	public void addEvents(List<LogEvent> events) {
		DefaultTableModel table = (DefaultTableModel) dataModel;
		table.removeRow(table.getRowCount()-1);
		for(LogEvent evt : events) {
			table.addRow(evt.toArray());
		}
	}
	
	/**
	 * Returns the Event table and the view options in a scrollable JPanel for easier integration.
	 * @return pane containing the log table and view options.
	 */
	public JPanel getInPanel() {
		//Create a scroll pane for the table so that it is easier to view all the events
		JScrollPane listScroller = new JScrollPane(this);
		listScroller.setWheelScrollingEnabled(true);
		listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
		listScroller.setSize(getWidth(),getHeight());
		
		//
		JPanel optionsPanel = new JPanel(new GridLayout(2,1));
		optionsPanel.add(goToRecentEventButton);
		optionsPanel.add(followEventsCheckBox);
		
		//
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(listScroller, BorderLayout.CENTER);
		tablePanel.add(optionsPanel,BorderLayout.SOUTH);
		
		tablePanel.setBorder(BorderFactory.createTitledBorder("Log Events"));
				
		return tablePanel;
	}
	
	/**
	 * If the table of events are visible then the table highlights the latest event to happen.
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {
		if(isVisible()) {
			clearSelection(); //clear the old selection so that when playing backwards events become unselected
			addRowSelectionInterval(0, player.getCurrentIndex()-1);
			if(followEventsCheckBox.isSelected()) {
				scrollToLatest();
			}
		}
	}
	
	/**
	 * When the "Go To Latest Event" is pressed this action is performed and it scrolls 
	 * the panel to the event that happened most recently
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		scrollToLatest();
	}
	
	/**
	 * helper method for scrolling the table to the latest event to happen.
	 */
	private void scrollToLatest() {
		scrollRectToVisible(getCellRect(player.getCurrentIndex()+10, 0, true));
	}
}
