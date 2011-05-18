package spiderweb;

/**
 * 
 */
public enum PlayState {
	FASTREVERSE("Fast Reverse"), 
	REVERSE("Reverse"), 
	PAUSE("Pause"), 
	FORWARD("Forward"), 
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