package spiderweb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimeCounter implements ActionListener {
	
	private long lowerBound;
	private long upperBound; 
	
	private long time;
	private long increment;
	
	/** 
	 *<p>Default Constructor</p>
	 *<ul>
	 *<li>Sets initial time as <code>0</code></li>
	 *<li>Sets increment as <code>1</code></li>
	 *<li>Sets lower bound as <code>0</code></li>
	 *<li>Sets upper bound as <code>100</code></li>
	 *</ul>
	 */
	public TimeCounter() {
		this(1);
	}
	
	/** 
	 *<ul>
	 *<li>Sets initial time as <code>0</code></li>
	 *<li>Sets increment as passed increment value</li>
	 **<li>Sets lower bound as <code>0</code></li>
	 *<li>Sets upper bound as <code>100</code></li>
	 *</ul>
	 *@param increment The value to increment the time as for each call of run.
	 */
	public TimeCounter(long increment) {
		this(increment, 0);
	}
	
	/**
	 *<ul>
	 *<li>Sets initial time as passed start time</li>
	 *<li>Sets increment as passed increment value</li>
	 **<li>Sets lower bound as <code>0</code></li>
	 *<li>Sets upper bound as <code>100</code></li>
	 *</ul>
	 * @param increment The value to increment the time as for each call of run.
	 * @param startTime The initial value of the counter.
	 */
	public TimeCounter(long increment, long startTime) {
		this(increment,startTime,0,100);
	}
	
	/**
	 *<ul>
	 *<li>Sets initial time as passed start time</li>
	 *<li>Sets increment as passed increment value</li>
	 **<li>Sets lower bound as passed lower bound</li>
	 *<li>Sets upper bound as passed upper bound</li>
	 *</ul>
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
	
	public long getTime() {
		return time;
	}
	
	public synchronized void setTime(long time) {
		this.time = time;
	}
	
	public long getIncrement() {
		return increment;
	}
	
	public long getLowerBound() {
		return lowerBound;
	}
	
	public long getUpperBound() {
		return upperBound;
	}
	
	public void setIncrement(long increment) {
		this.increment = increment;
	}
	
	public void setLowerBound(long bound) {
		lowerBound = bound;
	}
	public void setUpperBound(long bound) {
		upperBound = bound;
	}
	
	public void doIncrement() {
		setTime(time + increment);
		if(time < lowerBound) {
			time = lowerBound;
		}
		else if(time > upperBound) {
			time = upperBound;
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		doIncrement();
	}
}