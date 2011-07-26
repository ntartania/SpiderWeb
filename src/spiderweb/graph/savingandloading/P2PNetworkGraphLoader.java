/*
 * File:         P2PNetworkGraphLoader.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      01/06/2011
 * Last Changed: Date: 26/07/2011 
 * Author:       <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import spiderweb.graph.*;
import spiderweb.networking.HTTPClient;
import spiderweb.graph.LogEvent;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * The P2PNetworkGraphLoader is a class that will load multiple types 
 * of files and create graphs and log events from them.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 21/07/2011 
 */
public class P2PNetworkGraphLoader extends ProgressAdapter{
	
	private LinkedList<LogEvent> logList;
	private P2PNetworkGraph fullGraph;
	private P2PNetworkGraph dynamicGraph;
	

	//[start] Constructor
	public P2PNetworkGraphLoader() {
		super();
		logList = new LinkedList<LogEvent>();
		fullGraph = new P2PNetworkGraph();
		dynamicGraph = new P2PNetworkGraph();
		
	}
	//[end] Constructor

	//[start] Loading Method
	/**
	 * @return <code>true</code> if file loaded successfully
	 */
	public boolean doLoad() {
		String[] acceptedExtensions = { "xml","txt" };
		File file = chooseLoadFile(".xml and .txt Files", acceptedExtensions);
		if(file != null) {
			if(file.getAbsolutePath().endsWith(".txt")) {
				try {
					taskStarted(1,"Log Files");
					LogEventListBuilder logBuilder = new LogEventListBuilder();
					for(LoadingListener l : progressListeners) {
						logBuilder.addProgressListener(l);
					}
					logList = logBuilder.createList(new BufferedReader(new FileReader(file)));

					fullGraph = logBuilder.getFullGraph(); //load hidden graph but keep visible graph empty
					taskComplete();
					return true;
				} catch(Exception e) {
					taskComplete();
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(file.getAbsolutePath().endsWith(".xml")) {
				try {
					SAXBuilder builder = new SAXBuilder();
					final Document networkDoc = builder.build(file);
					graphBuilder(networkDoc);
					logList.addFirst(LogEvent.getStartEvent());
					logList.addLast(LogEvent.getEndEvent(logList.getLast()));
					return true;
				} catch(Exception e) {
					taskComplete();
					JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return false;
	}
	//[end] Loading Method

	//[start] Graph Builder
	private void buildGraphVertices(Element nodeMap, P2PNetworkGraph startGraph) {
		int counter = 0;
		//Tell the listeners what is going to be loaded and how much
		taskChanged(nodeMap.getChildren().size(), "Vertices"); 

		for(Object o : nodeMap.getChildren()) {
			Element elem = (Element)o;
			String type = elem.getAttribute("type").getValue();

			if(type.equals("PeerVertex")) {
				int key = Integer.parseInt(elem.getChild("key").getText());
				fullGraph.addVertex(new PeerVertex(key));
				startGraph.addVertex(new PeerVertex(key));
			}
			else if(type.equals("DocumentVertex")) {
				int key = Integer.parseInt(elem.getChild("key").getText());
				fullGraph.addVertex(new DocumentVertex(key));
				startGraph.addVertex(new DocumentVertex(key));
			}
			progress(++counter);
		}
	}
	
	private int buildGraphEdges(Element edgeMap, P2PNetworkGraph startGraph) {
		int counter = 0;
		int edgeCounter=0;
		//Tell the listeners what is going to be loaded and how much
		taskChanged(edgeMap.getChildren().size(), "Edges");
		
		for(Object o : edgeMap.getChildren()) {
			Element elem = (Element)o;
			String type = elem.getAttribute("type").getValue();

			if(type.equals("PeerToPeer")) { //Peer to Peer
				int v1Key = Integer.parseInt(elem.getChild("v1").getText());
				int v2Key = Integer.parseInt(elem.getChild("v2").getText());
				P2PVertex peer1 = fullGraph.getVertexInGraph(new PeerVertex(v1Key));
				P2PVertex peer2 = fullGraph.getVertexInGraph(new PeerVertex(v2Key));
				startGraph.addEdge(new P2PConnection(P2PConnection.P2P,edgeCounter), peer1, peer2);
				fullGraph.addEdge(new P2PConnection(P2PConnection.P2P,edgeCounter), peer1, peer2);
				edgeCounter++;
			}
			else if(type.equals("PeerToDocument")) { //Peer to Document
				int v1Key = Integer.parseInt(elem.getChild("v1").getText());
				int v2Key = Integer.parseInt(elem.getChild("v2").getText())%1000;
				P2PVertex document = fullGraph.getVertexInGraph(new DocumentVertex(v2Key));
				P2PVertex peer = fullGraph.getVertexInGraph(new PeerVertex(v1Key));
				Pair<P2PVertex> pair = new Pair<P2PVertex>(peer, document);
				startGraph.addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter),pair);
				fullGraph.addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter),pair);
				edgeCounter++;

				PeerDocumentVertex pdv = new PeerDocumentVertex(v1Key, v2Key);
				fullGraph.addVertex(pdv);
				startGraph.addVertex(pdv);
				startGraph.addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter),peer,pdv);
				fullGraph.addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter),peer,pdv);
				edgeCounter++;

				startGraph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter),pdv,document);
				fullGraph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter),pdv,document);
				edgeCounter++;
			}
			progress(++counter);
		}
		return edgeCounter;
	}
	
	private void buildLogEvents(Element logElem, P2PNetworkGraph startGraph, int edgeCounter) {
		if(logElem != null) {
			taskChanged(logElem.getChildren().size(), "Events");
			int counter=0;
			for(Object o : logElem.getChildren()) {

				Element event = (Element)o;
				String type = event.getAttribute("type").getValue();
				if(type.equals("start") || type.equals("end")) {
					continue;
				}
				long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
				int paramOne = Integer.parseInt(event.getChildText("param1"));
				int paramTwo = 0;
				int paramThree = 0;
				if(LogEvent.typeHasParamTwo(type)) {
					paramTwo = Integer.parseInt(event.getChildText("param2"));
				}
				if(LogEvent.typeHasParamThree(type)) {
					paramThree = Integer.parseInt(event.getChildText("param3"));
				}
				LogEvent evt = new LogEvent(timeDifference, type, paramOne, paramTwo, paramThree);
				if(evt.isConstructing()) {
					if (evt.getType().equals("online")){
						fullGraph.addVertex(new PeerVertex(evt.getParam(1)));
					} else if(evt.getType().equals("connect")){
						P2PVertex from = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(1)));
						P2PVertex to = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(2)));
						if(fullGraph.findEdge(to, from) == null) {
							P2PConnection edgeOne = new P2PConnection(P2PConnection.P2P,edgeCounter);
							edgeCounter++;
							P2PConnection edgeTwo = new P2PConnection(P2PConnection.P2P,edgeCounter);
							edgeCounter++;
							fullGraph.addEdge(edgeOne, from, to);
							fullGraph.addEdge(edgeTwo, to, from);
						}
						// else the edge already exists
					} else if(evt.getType().equals("publish")){
						P2PVertex document = new DocumentVertex(evt.getParam(2));
						P2PVertex peerDocument = new PeerDocumentVertex(evt.getParam(1), evt.getParam(2));
						P2PVertex peer = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(1)));

						if(!fullGraph.containsVertex(document)) {
							fullGraph.addVertex(document);
						}
						else {
							document = fullGraph.getVertexInGraph(document);
						}
						fullGraph.addVertex(peerDocument);

						if(fullGraph.findEdge(peer, document) == null) {
							fullGraph.addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter), peer, document);
							edgeCounter++;
						}
						if(fullGraph.findEdge(peer, peerDocument) == null) {
							fullGraph.addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter), peer, peerDocument);
							edgeCounter++;
						}
						if(fullGraph.findEdge(peerDocument, document) == null) {
							fullGraph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter), peerDocument, document);
							edgeCounter++;
						}
					}
					else if(evt.getType().equals("linkdocument")){
						P2PVertex documentOne = fullGraph.getVertexInGraph(new DocumentVertex(evt.getParam(1)));
						P2PVertex documentTwo = fullGraph.getVertexInGraph(new DocumentVertex(evt.getParam(2)));
						if(fullGraph.findEdge(documentOne, documentTwo) == null) {
							fullGraph.addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeCounter), documentOne, documentTwo);
							edgeCounter++;
						}
					}
				}
				logList.add(evt);
				progress(++counter);
			}
		}
	}
	
	private void graphBuilder(Document networkDoc) {
		if(networkDoc.getRootElement().getName().equals("network")) {
			int edgeCounter=0; //to keep track of indexing the edges
			P2PNetworkGraph startGraph = new P2PNetworkGraph();
			Element networkElem = networkDoc.getRootElement();
			
			Element graphElem = networkElem.getChild("graph");
			if(graphElem != null) {//Create Graph
				taskStarted(1,"Start Building Graph");
				//Add Vertices to graph
				buildGraphVertices(graphElem.getChild("nodemap"), startGraph);

				//Add Edges to graph
				edgeCounter = buildGraphEdges(graphElem.getChild("edgemap"), startGraph);
			}

			//Create Logs
			buildLogEvents(networkElem.getChild("logevents"), startGraph, edgeCounter);
			
			dynamicGraph = startGraph;
			taskComplete();
		}
	}


	private void addEventsToGraph(Document networkDoc) {
		if(networkDoc.getRootElement().getName().equals("network")) {
			int edgeCounter=fullGraph.getEdgeCount();
			Element networkElem = networkDoc.getRootElement();
			Element logElem = networkElem.getChild("logevents");
			if(logElem != null) {
				taskChanged(logElem.getChildren().size(), "Events");
				for(Object o : logElem.getChildren()) {

					Element event = (Element)o;
					String type = event.getAttribute("type").getValue();
					if(type.equals("start") || type.equals("end")) {
						continue;
					}
					long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
					int paramOne = Integer.parseInt(event.getChildText("param1"));
					int paramTwo = 0;
					int paramThree = 0;
					if(LogEvent.typeHasParamTwo(type)) {
						paramTwo = Integer.parseInt(event.getChildText("param2"));
					}
					if(LogEvent.typeHasParamThree(type)) {
						paramThree = Integer.parseInt(event.getChildText("param3"));
					}
					LogEvent evt = new LogEvent(timeDifference, type, paramOne, paramTwo, paramThree);
					if(evt.isConstructing()) {
						if (evt.getType().equals("online")){
							fullGraph.addVertex(new PeerVertex(evt.getParam(1)));
						} else if(evt.getType().equals("connect")){
							P2PVertex from = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(1)));
							P2PVertex to = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(2)));
							if(fullGraph.findEdge(to, from) == null) {
								P2PConnection edgeOne = new P2PConnection(P2PConnection.P2P,edgeCounter);
								edgeCounter++;
								P2PConnection edgeTwo = new P2PConnection(P2PConnection.P2P,edgeCounter);
								edgeCounter++;
								fullGraph.addEdge(edgeOne, from, to);
								fullGraph.addEdge(edgeTwo, to, from);
							}
							// else the edge already exists
						} else if(evt.getType().equals("publish")){
							P2PVertex document = new DocumentVertex(evt.getParam(2));
							P2PVertex peerDocument = new PeerDocumentVertex(evt.getParam(1), evt.getParam(2));
							P2PVertex peer = fullGraph.getVertexInGraph(new PeerVertex(evt.getParam(1)));

							if(!fullGraph.containsVertex(document)) {
								fullGraph.addVertex(document);
							}
							else {
								document = fullGraph.getVertexInGraph(document);
							}
							fullGraph.addVertex(peerDocument);

							if(fullGraph.findEdge(peer, document) == null) {
								fullGraph.addEdge(new P2PConnection(P2PConnection.P2DOC,edgeCounter), peer, document);
								edgeCounter++;
							}
							if(fullGraph.findEdge(peer, peerDocument) == null) {
								fullGraph.addEdge(new P2PConnection(P2PConnection.P2PDOC,edgeCounter), peer, peerDocument);
								edgeCounter++;
							}
							if(fullGraph.findEdge(peerDocument, document) == null) {
								fullGraph.addEdge(new P2PConnection(P2PConnection.DOC2PDOC,edgeCounter), peerDocument, document);
								edgeCounter++;
							}
						}
						else if(evt.getType().equals("linkdocument")){
							P2PVertex documentOne = fullGraph.getVertexInGraph(new DocumentVertex(evt.getParam(1)));
							P2PVertex documentTwo = fullGraph.getVertexInGraph(new DocumentVertex(evt.getParam(2)));
							if(fullGraph.findEdge(documentOne, documentTwo) == null) {
								fullGraph.addEdge(new P2PConnection(P2PConnection.DOC2DOC,edgeCounter), documentOne, documentTwo);
								edgeCounter++;
							}
						}
					}
					logList.add(evt);
				}
			}
		}
	}
	//[end] Graph Builder

	//[start] Getters

	public LinkedList<LogEvent> getLogList() {
		return logList;
	}
	public P2PNetworkGraph getFullP2PNetworkGraph() {
		return fullGraph;
	}
	public P2PNetworkGraph getDynamicP2PNetworkGraph() {
		return dynamicGraph;
	}
	//[end] Getters

	//[start] Static Methods
	public static File chooseLoadFile(String filterDescription, String[] acceptedExtensions) {
		JFileChooser fileNamer = new JFileChooser();
		fileNamer.setFileFilter(new ExtensionFileFilter(filterDescription, acceptedExtensions));
		int returnVal = fileNamer.showOpenDialog(null);


		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for(String extension : acceptedExtensions) {
				if(fileNamer.getSelectedFile().getAbsolutePath().endsWith(extension)) {

					return fileNamer.getSelectedFile();
				}
			}
			JOptionPane.showMessageDialog(null, "Error: Incorrect extension.", "Error", JOptionPane.ERROR_MESSAGE);

			return null;
		}
		else if (returnVal == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(null, "Error: Could not load file.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}


	public static P2PNetworkGraphLoader buildGraph(InputStream inStream, HTTPClient client) throws JDOMException, IOException {

		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(inStream);
		
		client.setLatestTime(doc.getRootElement().getAttribute("time").getLongValue());

		P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
		loader.logList.addFirst(LogEvent.getStartEvent());
		loader.logList.addLast(LogEvent.getEndEvent(loader.logList.getLast()));
		loader.graphBuilder(doc);

		return loader;
	}


	public static LinkedList<LogEvent> buildLogs(InputStream inStream, HTTPClient client, P2PNetworkGraph fullGraph) throws JDOMException, IOException {

		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(inStream);
		
		client.setLatestTime(doc.getRootElement().getAttribute("time").getLongValue());
		
		P2PNetworkGraphLoader loader = new P2PNetworkGraphLoader();
		
		loader.fullGraph = fullGraph;
		loader.addEventsToGraph(doc);
		return loader.logList;
	}
	//[end] Static Methods

}

