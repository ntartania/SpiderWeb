/*
 * File:         TransformerChangeListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.transformers;

/**
 * Interface for informing of a change in the options for what vertices are going to look like.
 * Represents a listener for transformer change events.
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public interface TransformerChangeListener {
	
	/**
	 * Handle the passed transformer change event.
	 * @param options the <code>TransformerChangeEvent</code> to handle
	 */
	public void changeOptions(TransformerChangeEvent options);
}
