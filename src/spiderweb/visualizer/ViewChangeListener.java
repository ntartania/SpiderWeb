/*
 * File:         ViewChangeListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      09/08/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer;

/**
 * Interface for informing of a change in the view for what vertices are to be displayed.
 * Represents a listener for changing View States.
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public interface ViewChangeListener {
	
	/**
	 * Handle changing to the new passed <code>ViewState</code>
	 * @param view The view to handle changing to.
	 */
	public void viewChanged(ViewState view);
}
