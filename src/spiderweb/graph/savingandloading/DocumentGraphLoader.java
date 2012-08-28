/*
 * File:         DocumentGraphLoader
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      15/08/2011
 * Last Changed: Date: 15/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb.graph.savingandloading;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import spiderweb.graph.DocumentVertex;
import spiderweb.graph.LogEvent;
import spiderweb.graph.P2PConnection;
import spiderweb.graph.P2PNetworkGraph;
import spiderweb.graph.ReferencedNetworkGraph;

/**
 * The Document Graph Loader gives an easy way to select a file on the user's drive and parses
 * the file into 
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 15/08/2011 
 */
public class DocumentGraphLoader extends ProgressAdapter{

	private P2PNetworkGraph documentGraph;

	public DocumentGraphLoader() {
		super();
		documentGraph = new P2PNetworkGraph();
	}
	
	public ReferencedNetworkGraph getGraph() {
		return new ReferencedNetworkGraph(documentGraph, documentGraph);
	}
	
	public List<LogEvent> getLogList() {
		List<LogEvent> logs = new ArrayList<LogEvent>();
		logs.add(LogEvent.getStartEvent());
		logs.add(LogEvent.getEndEvent(logs.get(0)));
		return logs;
	}

	/**
	 * @return <code>true</code> if file loaded successfully
	 */
	public boolean doLoad() {
		String[] acceptedExtensions = { "doclink" };
		File file = chooseLoadFile(".doclink Files", acceptedExtensions);
		if(file != null) {
			try {
				final int totalLines = countLines(file); //the total number of lines so the loading bar can size itself properly
				graphBuilder(new BufferedReader(new FileReader(file)), totalLines);

				taskComplete();
				return true;
			} catch(Exception e) {
				taskComplete();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}

	private void graphBuilder(BufferedReader linksDoc, final int totalLines) {
		try {
			String str; //will contain each log event as it is read.

			int lineCount = 0;

			int edgeCount = 0;
			
			//notify the listeners that the log events have begun loading
			taskStarted(totalLines, "Link Document");

			//logEvents.add(LogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing graph
			while ((str = linksDoc.readLine()) != null) //reading lines log file
			{
				String [] docs = str.split("\\s+");
				String firstDoc = docs[0].substring(0, docs[0].length()-1); //remove the last character(it should always be a ':')
				int documentToLink = Integer.parseInt(firstDoc);
				DocumentVertex docOne = new DocumentVertex(documentToLink);
				if(!documentGraph.containsVertex(docOne)){
					documentGraph.addVertex(docOne);
				}
				
				for(int i=1;i<docs.length;i++) {
					int otherDocument = Integer.parseInt(docs[i]);
					DocumentVertex docTwo = new DocumentVertex(otherDocument);
					if(!documentGraph.containsVertex(docTwo)) {
						documentGraph.addVertex(docTwo);
					}
					if(!documentGraph.areDocumentsConnected(documentToLink, otherDocument)) {
						documentGraph.addEdge(new P2PConnection(P2PConnection.DOC2DOC, edgeCount++), docOne, docTwo);
					}
				}
				
				//Increment the line number and notify the loading listeners
				lineCount++;
				progress(lineCount);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method counts the number of lines a passed File has and returns the value.
	 * 
	 * Created by user martinus from stack overflow
	 * @param fileToCount The File to count the number of lines in.
	 * @return The number of lines a file contains.
	 * @throws IOException 	IOException - If the first byte cannot be read for any reason other than the end of the file, 
	 * 						if the input stream has been closed, or if some other I/O error occurs. 
	 * 						FileNotFoundException - if the file does not exist, is a directory rather than a regular file, 
	 * 						or for some other reason cannot be opened for reading. 
	 */
	public static int countLines(File fileToCount) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(fileToCount));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
			return count;
		} finally {
			is.close();
		}
	}


	/**
	 * Brings up a JFileChooser for the choice of selecting a file to open for loading.
	 * 
	 * Filters the files visible by the passed extensions and brings up error dialogs if
	 * an incorrect file type is selected, or an error occurs.
	 * @param filterDescription The description of what file extensions are being filtered
	 * @param acceptedExtensions The file extensions to filter
	 * @return a <code>File</code> containing the information to load to the graph.
	 */
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
}
