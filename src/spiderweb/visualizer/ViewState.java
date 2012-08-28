/*
 * File:         ViewState.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      17/08/2011
 * Last Changed: Date: 17/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

/**
 * View enumerates meaningful names for which can be used to 
 * determine the state that the visualizer is currently showing.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 17/08/2011 
 */
public enum ViewState {

	FullView("Full View"), 
	CollapsedPeerView("Collapsed Peer View"), 
	CollapsedDocumentView("Collapsed Document View"), 
	CollapsedPeerAndDocumentView("Collapsed Peer and Document View");

	/**
	 * Sets a String value of the View
	 * @param str A String representation of the View
	 */
	private ViewState(String str) {
		this.str = str;
	}

	private final String str; // The String representation of the enumerated state.

	@Override
	public String toString() {
		return str;
	}

	public static ViewState getFromString(String view) {
		if(view.equals("Collapsed Peer View")) {
			return CollapsedPeerView;
		}
		else if(view.equals("Collapsed Document View")) {
			return CollapsedDocumentView;
		}
		else if(view.equals("Collapsed Peer and Document View")) {
			return CollapsedPeerAndDocumentView;
		}
		return FullView;
	}
}