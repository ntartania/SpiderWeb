package spiderweb.visualizer.eventplayer;

/**
 * @author   Matty
 */
public enum PlayState {
	/**
	 * @uml.property  name="fASTREVERSE"
	 * @uml.associationEnd  
	 */
	FASTREVERSE("Fast Reverse"), 
	/**
	 * @uml.property  name="rEVERSE"
	 * @uml.associationEnd  
	 */
	REVERSE("Reverse"), 
	/**
	 * @uml.property  name="pAUSE"
	 * @uml.associationEnd  
	 */
	PAUSE("Pause"), 
	/**
	 * @uml.property  name="fORWARD"
	 * @uml.associationEnd  
	 */
	FORWARD("Forward"), 
	/**
	 * @uml.property  name="fASTFORWARD"
	 * @uml.associationEnd  
	 */
	FASTFORWARD("Fast Forward");
	
	/**
	 * 
	 * @param str
	 */
	private PlayState(String str) {
		this.str = str;
	}
	private final String str; // The String representation of the enumerated state.
	
	/**
	 * Returns the string value of the given state.
	 * @return A <code>String</code> representation of the enumerated state.
	 */
	public String toString() {
		return str;
	}
}