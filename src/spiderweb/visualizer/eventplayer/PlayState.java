/*
 * File:         PlayState.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.eventplayer;

/**
 * PlayState enumerates meaningful names for which can be used to 
 * determine what the state of the event player is.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 20/07/2011 
 */
public enum PlayState {

	FASTREVERSE("Fast Reverse"), 
	REVERSE("Reverse"), 
	PAUSE("Pause"), 
	FORWARD("Forward"), 
	FASTFORWARD("Fast Forward");
	
	/**
	 * Sets a String of the PlayState
	 * @param str A String representation of the PlayState
	 */
	private PlayState(String str) {
		this.str = str;
	}
	private final String str; // The String representation of the enumerated state.
	
	@Override
	public String toString() {
		return str;
	}
}