/*
 * File:         ChangeViewListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      09/08/2011
 * Last Changed: Date: 13/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

/**
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 */
public class ChangeViewListener implements ActionListener {
	
	private NetworkGraphVisualizer visualizer;
	
	public ChangeViewListener(NetworkGraphVisualizer visualizer) {
		this.visualizer = visualizer;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		JRadioButton source = (JRadioButton) ae.getSource();
		if(source.getText().equals("Full View")) {
			visualizer.setFullView();
		} else if(source.getText().equals("Collapsed Peer View")) {
			visualizer.setCollapsedPeerView();
		} else if(source.getText().equals("Collapsed Document View")) {
			visualizer.setCollapsedDocumentView();
		} else if(source.getText().equals("Collapsed Peer and Document View")) {
			visualizer.setCollapsedPeerAndDocumentView();
		}
	}
}
