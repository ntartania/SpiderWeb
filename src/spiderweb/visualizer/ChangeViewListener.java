package spiderweb.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

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
