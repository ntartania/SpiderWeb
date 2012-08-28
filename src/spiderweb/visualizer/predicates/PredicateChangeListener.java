/*
 * File:         PredicateChangeListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 19/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.predicates;

/**
 * Interface for informing of a change in the options for what vertices are to be displayed.
 * Represents a listener for predicate change events.
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 19/08/2011 
 */
public interface PredicateChangeListener {
	
	/**
	 * Handle the passed predicate change event.
	 * @param options the <code>PredicateChangeEvent</code> to handle
	 */
	public void changeOptions(PredicateChangeEvent options);
}
