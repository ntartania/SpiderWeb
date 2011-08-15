/*
 * File:         LoadingListener.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

/**
 * The Loading Listener interface allows for classes which are 
 * interested in something that is loading to be notified as 
 * specific loading events happen.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public interface LoadingListener {
	
	/**
	 * The loading is starting, being anything that is based around the loading.
	 * (Turn on the loading bar for example)
	 * 
	 * The loading amount can be any arbitrary number, the progress 
	 * from the loadingProgress method will be used to calculate a 
	 * percentage of the loadingAmount.
	 * @param loadingAmount	The maximum value that needs to be loaded.
	 * @param whatIsLoading	A String identifier of what will be loading.
	 */
	public void loadingStarted(int loadingAmount, String whatIsLoading);
	
	/**
	 * Updates the Progress through the maximum loadingAmount to calculate
	 * a percentage of how far through loading it is.
	 * 
	 * @param progress	The progress through the loadingAmount.
	 */
	public void loadingProgress(int progress);
	
	/**
	 * The loading has completed, wrap up anything that is based around the loading.
	 * (Turn off the loading bar for example)
	 */
	public void loadingComplete();	
}
