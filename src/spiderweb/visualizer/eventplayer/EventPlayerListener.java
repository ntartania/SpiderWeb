/*
 * File:         EventPlayerListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.eventplayer;

/**
 * The EventPlayerListener interface allows the EventPlayer to 
 * notify any classes which are interested in the state of the
 * player and its graphs.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 20/07/2011 
 */
public interface EventPlayerListener {

	/**
	 * The PlayState of the event player has changed to the passed state.
	 * @param state The new PlayState of the event player
	 */
	public void stateChanged(PlayState state);

	/**
	 * Notify that a repaint call is needed.
	 */
	public void doRepaint();
}
