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

import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
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
 * 
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 11/08/2011 
 */
public class LogEventTable extends JTable implements ChangeListener {
	
	/**eclipse generated serial UID*/
	private static final long serialVersionUID = 304553330811497820L;
	public static final Object[] titles = { "Time", "Type", "Param 1", "Param 2", "Param 3" };
	
	protected EventPlayer eventThread;
	
	public LogEventTable(List<LogEvent> logEvents, EventPlayer eventThread) {
		super();
		this.eventThread = eventThread;
		init(logEvents);
	}

	private void init(List<LogEvent> logEvents) {
		
		Object[][] table = new Object[logEvents.size()][5];
		int i=0;
		for(LogEvent evt : logEvents) {
			table[i] = evt.toArray();
			i++;
		}
		
		this.setModel(new DefaultTableModel(table, titles));
		
		setBackground(Color.LIGHT_GRAY);
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setEnabled(false);
		setColumnSelectionAllowed(false);
		setVisible(true);
	}
	
	public void addEvents(List<LogEvent> events) {
		DefaultTableModel table = (DefaultTableModel) dataModel;
		table.removeRow(table.getRowCount()-1);
		for(LogEvent evt : events) {
			table.addRow(evt.toArray());
		}
	}
	
	public JPanel getInPanel() {
		JScrollPane listScroller = new JScrollPane(this);
		listScroller.setWheelScrollingEnabled(true);
		listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
		listScroller.setSize(getWidth(),getHeight());
		
		JPanel tablePanel = new JPanel(new GridLayout(1,1));
		tablePanel.add(listScroller);
		tablePanel.setBorder(BorderFactory.createTitledBorder("Log Events"));
				
		return tablePanel;
	}
	
	@Override
	public void stateChanged(ChangeEvent ce) {
		if(isVisible()) {
			clearSelection();
			addRowSelectionInterval(0, eventThread.getCurrentIndex()-1);
			scrollRectToVisible(getCellRect(eventThread.getCurrentIndex()-1, 0, true));
		}
	}
}
