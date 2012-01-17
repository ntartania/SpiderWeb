/*
 * File:         PlaybackPanel.java
 * Project:		 Spiderweb Network Graph Visualizer
 * Created:      11/08/2011
 * Last Changed: Date: 12/08/2011 
 * Author:       Matthew Smith
 * 
 * This code was produced at Carleton University 2011
 */
package spiderweb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import spiderweb.graph.savingandloading.LoadingListener;
import spiderweb.visualizer.eventplayer.EventPlayer;
import spiderweb.visualizer.eventplayer.PlayState;

/**
 * PlaybackPanel is a component which contains the buttons and sliders that affect 
 * the playing of the events in the event player. 
 * It also has a loading bar for when a new item is being loaded and progress needs to be shown.
 * 
 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
 * @version Date: 12/08/2011 
 */
public class PlaybackPanel extends JPanel implements ActionListener, ChangeListener, LoadingListener {

	/**eclipse generated serial UID*/
	private static final long serialVersionUID = -4379461440497103002L;
	
	//Buttons that represent the direction and speed of the event playback
	protected JButton fastForwardButton;
	protected JButton forwardButton;
	protected JButton pauseButton;
	protected JButton reverseButton;
	protected JButton fastReverseButton;
	
	//sliders to represent the speed and where through the playback the current event is.
	protected JSlider fastSpeedSlider;
	protected JSlider playbackSlider;
	
	//The loading bar
	protected JProgressBar progressBar;
	
	//the main panel which includes everything except the loading bar
	//(easier to add the loading bar to this without disturbing the rest of the components)
	protected JPanel mainPanel;
	
	/**A playback listener would be cleaner (event player listening to this class)*/
	protected EventPlayer player;
	
	/**
	 * PlaybackPanel is a component which contains the buttons and sliders that affect 
	 * the playing of the events in the event player. 
	 * As well the panel is a loading listener and will display a progress bar when an item is loading.
	 */
	public PlaybackPanel() {
		fastSpeedSlider = new JSlider(JSlider.HORIZONTAL,0,100,25);
		fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum()-fastSpeedSlider.getMinimum())/4);
		fastSpeedSlider.setFont(new Font("Arial",Font.PLAIN,8));
		fastSpeedSlider.setPaintTicks(false);
		fastSpeedSlider.setPaintLabels(true);
		fastSpeedSlider.setForeground(Color.BLACK);
		fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Quick Playback Speed"));
		fastSpeedSlider.setEnabled(false);
		
		fastReverseButton = new JButton("<|<|");
		fastReverseButton.setName("Fast Reverse Button");
		fastReverseButton.setEnabled(false);
		
		reverseButton = new JButton("<|");
		reverseButton.setName("Reverse Button");
		reverseButton.setEnabled(false);
		
		pauseButton = new JButton("||");
		pauseButton.setName("Pause Button");
		pauseButton.setEnabled(false);
		
		forwardButton = new JButton("|>");
		forwardButton.setName("Forward Button");
		forwardButton.setEnabled(false);
		
		fastForwardButton = new JButton("|>|>");
		fastForwardButton.setName("Fast Forward Button");
		fastForwardButton.setEnabled(false);
		
		playbackSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
		playbackSlider.setEnabled(false);
		
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
		
		GridBagLayout southLayout = new GridBagLayout();
		GridBagConstraints southConstraints = new GridBagConstraints();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(southLayout);
		
		buttonPanel.add(fastReverseButton);
		buttonPanel.add(reverseButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(forwardButton);
		buttonPanel.add(fastForwardButton);
		southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
		southLayout.setConstraints(fastSpeedSlider, southConstraints);
		buttonPanel.add(fastSpeedSlider);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2,1));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Playback Options"));
		mainPanel.add(buttonPanel);
		mainPanel.add(playbackSlider);
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
	}
	
	/**
	 * adds listeners to all the components and sets the sliders to be enabled
	 * @param player The EventPlayer which will play the log events.
	 */
	public void startPlayback(EventPlayer player) {
		this.player = player; 
		
		fastSpeedSlider.setEnabled(true);
		playbackSlider.setEnabled(true);
		
		fastSpeedSlider.addChangeListener(this);
		fastReverseButton.addActionListener(this); 
		reverseButton.addActionListener(this); 
		pauseButton.addActionListener(this); 
		forwardButton.addActionListener(this); 
		fastForwardButton.addActionListener(this);
		
		SliderListener s = new SliderListener();
		playbackSlider.addChangeListener(s);
		playbackSlider.addMouseListener(s);
	}
	
	/**
	 * removes any listeners and sets the buttons to be disabled
	 */
	public void stopPlayback() {

		//Remove listeners so that if somehow their listeners are triggered an exception 
		// will not be thrown because of a null event player
		fastSpeedSlider.removeChangeListener(this);
		fastReverseButton.removeActionListener(this); 
		reverseButton.removeActionListener(this); 
		pauseButton.removeActionListener(this); 
		forwardButton.removeActionListener(this); 
		fastForwardButton.removeActionListener(this); 
		
		//Disable all buttons
		fastReverseButton.setEnabled(false);
		reverseButton.setEnabled(false);
		pauseButton.setEnabled(false);
		forwardButton.setEnabled(false);
		fastForwardButton.setEnabled(false);
		fastSpeedSlider.setEnabled(false);
		playbackSlider.setEnabled(false);
		playbackSlider.setValue(0);
		
		//The old event player is done with
		player = null;
	}
	
	/**
	 * Returns the playback slider for modification and referencing
	 * @return The JSlider displayed as a playback slider on the panel.
	 */
	public JSlider getPlaybackSlider() {
		return playbackSlider;
	}
	
	/**
	 * Updates which buttons are enabled depending on the passed PlayState
	 * @param state The new PlayState of the event Player
	 */
	public void updateButtons(PlayState state) {
		//Likely a more efficient method exists but enables all 
		//buttons then depending on the state the method disables appropriately
		
		fastReverseButton.setEnabled(true);
		reverseButton.setEnabled(true);
		pauseButton.setEnabled(true);
		forwardButton.setEnabled(true);
		fastForwardButton.setEnabled(true);
		
		switch(state) {
		case FASTREVERSE:
			fastReverseButton.setEnabled(false);
			break;
			
		case REVERSE:
			reverseButton.setEnabled(false);
			break;
			
		case FASTFORWARD:
			fastForwardButton.setEnabled(false);
			break;
			
		case FORWARD:
			forwardButton.setEnabled(false);
			break;
			
		case PAUSE:
			if(player.atFront()) { //if at the front(first event) then set the reverse buttons to disabled
				fastReverseButton.setEnabled(false);
				reverseButton.setEnabled(false);
			}
			pauseButton.setEnabled(false);
			if(player.atBack()) { //if at the back(last event) then set the reverse buttons to disabled
				forwardButton.setEnabled(false);
				fastForwardButton.setEnabled(false);
			}
			break;
		}
	}

	/**
	 * Handler for all of the playback buttons
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		JButton source = (JButton) ae.getSource();
		String buttonName = source.getName();
		if(buttonName.equals("Fast Reverse Button")) {
			player.fastReverse();
		} else if(buttonName.equals("Reverse Button")) {
			player.reverse();
		} else if(buttonName.equals("Pause Button")) {
			player.pause();
		} else if(buttonName.equals("Forward Button")) {
			player.forward();
		} else if(buttonName.equals("Fast Forward Button")) {
			player.fastForward();
		}
	}

	/**
	 * Handler for the play speed slider
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {
		player.setSpeedMultiplier(((JSlider)ce.getSource()).getValue());
	}

	/**
	 * sets the progress bar tobzero, the maximum to the loading amount
	 * sets the text to the new item loading and adds the loading bar to the panel
	 */
	@Override
	public void loadingStarted(int loadingAmount, String whatIsLoading) {
		progressBar.setMinimum(0);
		progressBar.setMaximum(loadingAmount);
		progressBar.setName(whatIsLoading);
		progressBar.setValue(0);
		progressBar.setString(whatIsLoading+": 0%");
		progressBar.setStringPainted(true);
		
		add(progressBar, BorderLayout.NORTH);
		validate();
	}

	/**
	 * changes the value and the displayed text of the progress bar
	 */
	@Override
	public void loadingProgress(int progress) {
		progressBar.setValue(progress);
		progressBar.setString((String.format(progressBar.getName()+": %.3g%n", progressBar.getPercentComplete()*100))+"%");
	}

	/**
	 * removes the progress bar from the screen
	 */
	@Override
	public void loadingComplete() {
		remove(progressBar);
	}


	/**
	 * Listener for handling any event which takes place in the playbck panel.
	 * 
	 * @author <A HREF="mailto:smith_matthew@live.com">Matthew Smith</A>
	 */
	class SliderListener extends MouseAdapter implements ChangeListener {

		PlayState prevState = PlayState.PAUSE;
		
		@Override
		public void stateChanged(ChangeEvent ce) {
			if(player !=null) {
				JSlider source = (JSlider)ce.getSource();
				player.goToTime(source.getValue());
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if(((JSlider)(e.getSource())).isEnabled()){
				prevState = player.getPlayState();
				player.pause();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(((JSlider)(e.getSource())).isEnabled()){
				if(prevState == PlayState.FASTREVERSE) {
					player.fastReverse();
				}
				else if (prevState == PlayState.REVERSE) {
					player.reverse();
				}
				else if (prevState == PlayState.FORWARD) {
					player.forward();
				}
				else if (prevState == PlayState.FASTFORWARD) {
					player.fastForward();
				}
				else if (prevState == PlayState.PAUSE) {
					player.pause();
				}
			}
		}
	}

}
