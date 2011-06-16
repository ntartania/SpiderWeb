package spiderweb.networking;

import java.io.InputStream;

public interface NetworkListener {
	
	public void incomingLogEvents(InputStream inStream);
	
	public void incomingGraph(InputStream inStream);
}
