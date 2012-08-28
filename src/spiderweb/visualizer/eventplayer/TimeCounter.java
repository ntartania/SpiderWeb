/*
 * File:         TimeCounter.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 20/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.visualizer.eventplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TimeCounter maintains a time with both upper and lower bounds. 
 * 
 * To increment the counter the actionPerformed or the doIncrement methods can be called.
 * actionPerformed allows for the counter to be used with event systems such as timers.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 20/07/2011 
 */
public class TimeCounter implements ActionListener {
	
	private long lowerBound;
	private long upperBound; 
	private long time;
	private long increment;
	
	/** 
	 * <ul>
	 * <li>Sets initial time as <code>0</code></li>
	 * <li>Sets increment as <code>1</code></li>
	 * <li>Sets lower bound as <code>0</code></li>
	 * <li>Sets upper bound as <code>100</code></li>
	 * </ul>
	 */
	public TimeCounter() {
		this(1);
	}
	
	/** 
	 * <ul>
	 * <li>Sets initial time as <code>0</code></li>
	 * <li>Sets increment as passed increment value</li>
	 * <li>Sets lower bound as <code>0</code></li>
	 * <li>Sets upper bound as <code>100</code></li>
	 * </ul>
	 * @param increment The value to increment the time as for each call of run.
	 */
	public TimeCounter(long increment) {
		this(increment, 0);
	}
	
	/**
	 * <ul>
	 * <li>Sets initial time as passed start time</li>
	 * <li>Sets increment as passed increment value</li>
	 * <li>Sets lower bound as <code>0</code></li>
	 * <li>Sets upper bound as <code>100</code></li>
	 * </ul>
	 * @param increment The value to increment the time as for each call of run.
	 * @param startTime The initial value of the counter.
	 */
	public TimeCounter(long increment, long startTime) {
		this(increment,startTime,0,100);
	}
	
	/**
	 * <ul>
	 * <li>Sets initial time as passed start time</li>
	 * <li>Sets increment as passed increment value</li>
	 * <li>Sets lower bound as passed lower bound</li>
	 * <li>Sets upper bound as passed upper bound</li>
	 * </ul>
	 * @param increment The value to increment the time as for each call of run.
	 * @param startTime The initial value of the counter.
	 * @param lowerBound The lower bound on the Counter.
	 * @param upperBound The upper bound on the Counter.
	 */
	public TimeCounter(long increment, long startTime, long lowerBound, long upperBound) {
		this.increment = increment;
		this.time = startTime;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	/**
	 * Get the current time of the counter.
	 * @return The counter's current time.
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * Sets the time of the time counter.
	 * 
	 * Will still follow upper and lower bound rules.
	 * @param time The new time of the counter.
	 */
	public synchronized void setTime(long time) {
		this.time = time;
		doBound();
	}
	
	/**
	 * Get the counter's increment value.
	 * @return The time counter's increment value.
	 */
	public long getIncrement() {
		return increment;
	}
	
	/**
	 * Get the lower bound on the time counter.
	 * @return The time counter's lower bound.
	 */
	public long getLowerBound() {
		return lowerBound;
	}
	
	/**
	 * Get the upper bound on the time counter.
	 * @return The time counter's upper bound.
	 */
	public long getUpperBound() {
		return upperBound;
	}
	
	/**
	 * Sets the amount the time will increase/decrease with each call of doIncrement()
	 * @param increment the new Increment value.
	 */
	public void setIncrement(long increment) {
		this.increment = increment;
	}
	
	/**
	 * Set the lower bound of the time counter.
	 * @param lowerBound The new lower bound
	 */
	public void setLowerBound(long lowerBound) {
		this.lowerBound = lowerBound;
	}
	/**
	 * Set the upper bound of the time counter.
	 * @param upperBound The new upper bound.
	 */
	public void setUpperBound(long upperBound) {
		this.upperBound = upperBound;
	}
	
	/**
	 * Increments the time according to the increment value.
	 * If the time goes out of bounds, the increment will be set to the respective bound.
	 */
	public void doIncrement() {
		setTime(time + increment);
		doBound();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		doIncrement();
	}
	
	/**
	 * Checks if the time is above the upper bound or below the lower bound and adjusts the time accordingly.
	 */
	private void doBound() {
		if(time < lowerBound) {
			time = lowerBound;
		}
		else if(time > upperBound) {
			time = upperBound;
		}
	}
}