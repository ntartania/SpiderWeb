/*
 * File:         ProgressAdapter.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      26/07/2011
 * Last Changed: Date: 21/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

import java.util.LinkedList;

/**
 * Progress adapter is an abstract class which has methods for notifying loading listeners 
 * of progress through a task. The class maintains a Linked List of listeners and methods which act
 * on the listeners informing of the state of the progress.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 26/07/2011 
 */
public abstract class ProgressAdapter {
	
	/**
	 * The list of LoadingListeners for the methods to act on.
	 */
	protected LinkedList<LoadingListener> progressListeners;
	
	/**
	 * Initializes the List of LoadingListeners.
	 */
	public ProgressAdapter() {
		progressListeners = new LinkedList<LoadingListener>();
	}
	
	/**
	 * Appends the LoadingListener to the end of this list. 
	 * @param listenerToAdd The LoadingListener to be appended to the list
	 * @return <code>true</code> if the collection was changed as a result of the call.
	 */
	public boolean addProgressListener(LoadingListener listenerToAdd) {
		return progressListeners.add(listenerToAdd);
	}
	
	/**
	 * Removes the first occurrence of the specified element from this list of LoadingListeners, if it is present. 
	 * If this list does not contain the element, it is unchanged. 
	 * Returns <code>true</code> if this list contained the specified element 
	 * (or equivalently, if this list changed as a result of the call). 
	 * 
	 * @param listenerToRemove Listener to be removed from the list, if present	
	 * @return <code>true</code> if this list contained the specified element
	 */
	public boolean removeProgressListener(LoadingListener listenerToRemove) {
		return progressListeners.remove(listenerToRemove);
	}

	/**
	 * Informs all LoadingListeners in the list that the task has started 
	 * and progress will be starting with the passed number of lines.
	 * 
	 * @param numberLines	the value that is going to be loaded
	 * @param whatIsLoading	the name of the task which the progress has changed to
	 */
	protected void taskStarted(int numberLines, String whatIsLoading) {
		for(LoadingListener l : progressListeners) {
			l.loadingStarted(numberLines, whatIsLoading);
		}
	}

	/**
	 * Informs all LoadingListeners in the list of the progress on the present task.
	 * progress/numberLines is a percentage of the progress through the task.
	 * @param progress The progress through the task. 
	 */
	protected void progress(int progress) {
		for(LoadingListener l : progressListeners) {
			l.loadingProgress(progress);
		}
	}

	/**
	 * Informs all LoadingListeners in the list that the task has changed 
	 * and progress will be re-starting with a new number of lines.
	 * 
	 * @param numberLines	the new value that is going to be loaded
	 * @param whatIsLoading	the name of the task which the progress has changed to
	 */
	/*protected void taskChanged(int numberLines, String whatIsLoading) {
		for(LoadingListener l : progressListeners) {
			l.loadingChanged(numberLines, whatIsLoading);
		}
	}*/

	/**
	 * Informs all LoadingListeners in the list that the task who's progress was being monitored is complete
	 */
	protected void taskComplete() {
		for(LoadingListener l : progressListeners) {
			l.loadingComplete();
		}
	}
}
