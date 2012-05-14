/*
 * File:         P2PNetworkGraphSaver.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 26/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import spiderweb.graph.*;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * The P2PNetworkGraphSaver is a class that will take a Network Graph
 * and Log Events and save it to an XML File.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public class P2PNetworkGraphSaver extends ProgressAdapter {
	private long currentTime;
	private List<LogEvent> logList;
	private P2PNetworkGraph graph;
	
	//[start] Constructors
	public P2PNetworkGraphSaver(P2PNetworkGraph graph) {
		this(graph, null, 0);
	}
	
	public P2PNetworkGraphSaver(List<LogEvent> events, long currentTime) {
		this(null, events, currentTime);
	}
	
	public P2PNetworkGraphSaver(P2PNetworkGraph graph, List<LogEvent> events, long currentTime) {
		super();
		this.currentTime = currentTime;
		this.logList = events;
		this.graph = graph;
	}
	//[end] Constructors
	
	//[start] Saver Method
	public void doSave() {
		Thread saverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String path = chooseSaveFile().getAbsolutePath();
				if(path != null) {
					Document doc = buildDoc();
					if(outputDocumentToFile(doc, path)) {
						JOptionPane.showMessageDialog(null, "Success: File Saved", "Success", JOptionPane.INFORMATION_MESSAGE);
					}
					taskComplete();
				}
			}
		});
		saverThread.start();
	}
	
	public static String saveGraphForWeb(P2PNetworkGraph graph, long simulationTime) {
		P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(graph);
		Document doc = saver.buildDoc();
		doc.getRootElement().setAttribute("currentTime",Long.toString(simulationTime));
		XMLOutputter outputter = new XMLOutputter();
	    try {
	    	return outputter.outputString(doc);   
	    }
	    catch (Exception e) {e.printStackTrace();}
	    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<network currentTime=\""+simulationTime+"\"/>";
	}
	
	public static String saveEventsForWeb(List<LogEvent> events, long requestTime, long simulationTime) {
		if(!events.isEmpty()) {
			try {
				P2PNetworkGraphSaver saver = new P2PNetworkGraphSaver(events,requestTime);
				Document doc = saver.buildDoc();
				doc.getRootElement().setAttribute("requestTime",Long.toString(requestTime));
				doc.getRootElement().setAttribute("currentTime",Long.toString(simulationTime));
				
				XMLOutputter outputter = new XMLOutputter();
		    
		    	return outputter.outputString(doc);   
		    }
		    catch (Exception e) {e.printStackTrace();}
		}
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<network requestTime=\""+requestTime+"\" currentTime=\""+simulationTime+"\"/>";
	}
	//[end] Saver Method
	
	//[start] Document Builder Methods
	private Document buildDoc() {
		Element networkElement = new Element("network");
		
		if(graph != null && graph.getVertexCount()!=0) {
			//[start] Creating Graph Elements
			
			//[start] compile separate lists for peers, documents and peerdocuments
			LinkedList<PeerVertex> peers = new LinkedList<PeerVertex>();
			LinkedList<DocumentVertex> documents = new LinkedList<DocumentVertex>();
			LinkedList<P2PConnection> edges = new LinkedList<P2PConnection>(graph.getEdges());
			int count = 0;
			taskStarted(graph.getVertices().size(),"Compiling Vertex Lists");
			for(P2PVertex vertex : graph.getVertices()) {
				if(vertex.getClass().equals(PeerVertex.class)) {
					peers.addLast((PeerVertex) vertex);
				}
				else if(vertex.getClass().equals(DocumentVertex.class)) {
					documents.addLast((DocumentVertex) vertex);
				}
				progress(count);
				count++;
			}
			//[end] compile separate lists for peers, documents and peerdocuments
			
	        Element graphElement = new Element("graph");

	        graphElement.addContent(new Comment("Description of a P2PNetworkGraph")); //xml comment
	        
	        //[start] Create vertices
	        Element nodemap = new Element("nodemap");
	        count=0;
	        taskStarted(peers.size(), "Peer Vertices");
			for(PeerVertex peer : peers) { //write out all the peer information
				Element node = new Element("node");
		        node.setAttribute("type", "PeerVertex");
		       
		        Element key = new Element("key");
		        key.addContent(Integer.toString(peer.getKey())); 
		        node.addContent(key);
		        
		        nodemap.addContent(node);
		        progress(count);
		        count++;
			}
			count=0;
			taskStarted(documents.size(), "Document Vertices");
			for(DocumentVertex doc : documents) {//write out all the document information
				Element node = new Element("node");
		        node.setAttribute("type", "DocumentVertex");
		        
		        Element key = new Element("key");
		        key.addContent(Integer.toString(doc.getDocumentNumber())); 
		        node.addContent(key);
		        
		        nodemap.addContent(node);
		        
		        progress(count);
		        count++;
			}
			graphElement.addContent(nodemap);
			//[end] Create vertices
			
			//[start] Create Edges
			Element edgemap = new Element("edgemap");
			count=0;
			taskStarted(edges.size(), "Edges");
			for(P2PConnection e : edges) {//write out all the edge information
				Element edge = new Element("edge");
				
				switch(e.getType()) {
				case P2PConnection.P2P:
					edge.setAttribute("type", "PeerToPeer");
					break;
				case P2PConnection.P2DOC:
					edge.setAttribute("type", "PeerToDocument");
					break;
				case P2PConnection.DOC2DOC:
					edge.setAttribute("type", "DocumentToDocument");
					break;
				default:
					continue;
				}
					        
		        Pair<P2PVertex> ends = graph.getEndpoints(e);
		        
		        String key1 = Integer.toString(ends.getFirst().getKey());
		        String key2 = Integer.toString(ends.getSecond().getKey());
		        Element v1 = new Element("v1");
		        v1.addContent(key1);
		        edge.addContent(v1);
		        Element v2 = new Element("v2");
		        v2.addContent(key2);
		        
		        edge.addContent(v2);
		        
		        edgemap.addContent(edge);
		        
		        progress(count);
		        count++;
			}
			//[end] Create Edges
			
			graphElement.addContent(edgemap);
			networkElement.addContent(graphElement);
			//[end] Creating Graph Elements
		}
		if(logList != null) {
			//[start] Creating Log Event Elements
			Element logEventsElement = new Element("logevents");
			int count=0;
			taskStarted(logList.size(), "LogEvents");
			for(LogEvent ev : logList) {
				Element event = new Element("event");
				event.setAttribute("type", ev.getType());
		        
		        Element timeDifference = new Element("timedifference");
		        timeDifference.addContent(Integer.toString((int)(ev.getTime()-currentTime))); 
		        event.addContent(timeDifference);
		        
		        Element paramOne = new Element("param1");
		        paramOne.addContent(Integer.toString(ev.getParam(1)));
		        event.addContent(paramOne);
		        
		        if(ev.hasParamTwo()) {
		        	Element paramTwo = new Element("param2");
		        	paramTwo.addContent(Integer.toString(ev.getParam(2)));
			        event.addContent(paramTwo);
		        }
		        
		        if(ev.hasParamThree()) {
		        	Element paramThree = new Element("param3");
		        	paramThree.addContent(Integer.toString(ev.getParam(3)));
			        event.addContent(paramThree);
		        }
		        
		        logEventsElement.addContent(event);
		        progress(count);
		        count++;
			}
			
			networkElement.addContent(logEventsElement);
			//[end] Creating Log Event Elements
		}
		
		return new Document(networkElement);
	}
	//[end] Document Builder Methods
		
	//[start] XML outputter
    /**
     * This method shows how to use XMLOutputter to output a JDOM document to
     * a file located at xml/myFile.xml.
     * @param myDocument the JDOM document built from Listing 2.
     */
    private boolean outputDocumentToFile(Document myDocument, String path) {
        //setup this like outputDocument
        try {
        	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            //output to a file
            FileWriter writer = new FileWriter(path);
            outputter.output(myDocument, writer);
            writer.close();

        } catch(java.io.IOException e) {
        	JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
  //[end] XML outputter
	
	//[start] Static Methods
	public static File chooseSaveFile() {
		JFileChooser fileNamer = new JFileChooser();
		fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
		int returnVal = fileNamer.showSaveDialog(null);
		

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(".xml")) {

				return fileNamer.getSelectedFile();
			}
			return new File(fileNamer.getSelectedFile().getAbsolutePath()+".xml");
		}
		else if(returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	//[end] Static Methods
}
