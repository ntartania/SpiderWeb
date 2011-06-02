/**
 * Contains the class definition for a PeerDocumentVertex in the P2P Network Graph project.
 * @author Matthew Smith
 * @version May 19, 2011
 */
package spiderweb;

/**
 * <p>A class which represents a Peer and Document as a vertex in the P2P network visualization.</p>
 * <ul>
 * <li>A document is drawn differently than a peer, as well, it has fewer states.</li>
 * <li>Has a state which describes if it was hit by a query or not.</li>
 * <li>Stores the key of it's publisher so it knows where it came from.</li>
 * </ul>
 * @author Matthew Smith
 * @version May 19, 2011
 */
public class PeerDocumentVertex extends P2PVertex {
	
	//[start] Private Attributes
	private int peer; // The peer number of the peer who published this document.
	private boolean queryHit; // for state changing when a this document matches a query.
	//[end] Private Attributes
	
	//[start] Constructors
	/**
	 * Creates a vertex which represents a Document in the P2P network visualization.
	 * @param peer	The key of the Peer which published this document.
	 * @param document	The key value of this document.
	 */
	public PeerDocumentVertex(int peer, Integer document) {
		super(2000+document.intValue());
		this.peer = peer;
		this.label = new Integer(document.intValue() % 2000);
		this.queryHit = false;
	}
	//[end] Constructors
	
	//[start] Getters and Setters
	/**
	 * Returns the Peer Number for the publisher of this document.
	 * @return The PeerNumber of this document's publisher.
	 */
	public int getPeerNumber() {
		return peer;
	}
	
	public Integer getDocumentNumber() {
		return key % 2000;
	}
	//[end] Getters and Setters
	
	//[start] State Methods
		
	/**
	 * Sets the document to be a query hit or not for drawing purposes.
	 * @param isQueryHit	<code>true</code> when a query reached the publisher of this document and it matches this document.
	 */
	public void setQueryHit(boolean isQueryHit) {
		queryHit = isQueryHit;
	}
	
	/**
	 * Checks if the document was hit by a query.
	 * @return	<code>true</code> when the document matches a query which hit the publisher.
	 */
	public boolean isQueryHit() {
		return queryHit;
	}
	
	//[end] State Methods
	
	//[start] Overridden Methods
	@Override
	public String toString() {
		return "P"+peer+":D"+getDocumentNumber();
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof PeerDocumentVertex) {
			return ( (this.peer == ((PeerDocumentVertex)other).peer) && super.equals(other) );
		}
		return false; 
	}
	
	@Override
	public int compareTo(P2PVertex other) {
		if(other instanceof PeerDocumentVertex)
			return ( super.compareTo(other) );
		else 
		return 0;
	}
	//[end] Overridden Methods
}