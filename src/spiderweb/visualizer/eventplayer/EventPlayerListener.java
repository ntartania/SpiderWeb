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
	 * Notify that the PlayState is now FASTREVERSE.
	 */
	public void playbackFastReverse();

	/**
	 * Notify that the PlayState is now REVERSE.
	 */
	public void playbackReverse();

	/**
	 * Notify that the PlayState is now PAUSE.
	 */
	public void playbackPause();

	/**
	 * Notify that the PlayState is now FORWARD.
	 */
	public void playbackForward();

	/**
	 * Notify that the PlayState is now FASTFORWARD.
	 */
	public void playbackFastForward();

	/**
	 * Notify that a repaint call is needed.
	 */
	public void doRepaint();
}
