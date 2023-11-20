package Project2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.JScrollPane;

public class UserInterface extends JFrame {
	private class Pair<K, V> {
	    private K key;
	    private V value;

	    public Pair(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }

	    public K getKey() {
	        return key;
	    }

	    public V getValue() {
	        return value;
	    }
	}
	private File file; // the input file the user is choosing to use
	boolean isResetRequested = false;
	private int MemoryMax = -1, ProcSizeMax = -1, NumProc = -1, MaxProcTime = -1, size = -1; // constants declared in the input file to initialize the allocator
	private Object memAlgLock = new Object(); // locked until memory algorithm is selected by user
	private boolean algorithmChosen = false, compactChosen = false; // tracks when the algorithm/compact is selected by user and when it is not
	private JTextArea output;// the text area when output goes
	private JScrollPane scroller;
	private ContigousMemoryAllocator allocator; // allocator algorithm
	private JButton play, step, reset, newFile, help;
	private ArrayList<Pair<Integer, Integer>> HighlightCoords = new ArrayList<Pair<Integer, Integer>>(); // list that stores where highlights are located in the JTextArea
	private ArrayList<Color> HighlightColors = new ArrayList<Color>(); // list of colors for highlights in the JTextArea
	public MemoryVisual mv; //mv is the memory paint component
	boolean isPartListDefined = false; // Is false until the partList in contigousMemoryAllocator is defined
	public Object ResetLock = new Object(); // locks until algorithm is terminated
	private Object CompactLock = new Object(); // locks until compact is selected
	private boolean isHelpOpen = false; // tracks when help menu is open or closed
	String currentButtons[] = new String[5]; // stores the buttons' content before the help menu appears.
	public UserInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(722, 434); // Set initial size
        setResizable(false);
		Font ariel12 = new Font("Arial", Font.PLAIN, 12);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        output = new JTextArea();
        output.setEditable(false);
        scroller = new JScrollPane(output);
        
        output.setFont(ariel12);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 8;
        gbc.gridheight = 8;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scroller, gbc);
        mv = new MemoryVisual(Color.RED, 200, 360, "Memory");
        gbc.gridwidth = 3;
        gbc.gridx = 8;
        mainPanel.add(mv, gbc);
        play = new JButton("0");
        gbc.weightx = 1;
    	gbc.weighty = 0.02;
    	gbc.gridx = 0;
    	gbc.gridy = 9;
    	gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 1, 1, 1);
        mainPanel.add(play, gbc);
        
        step = new JButton("1");
        gbc.gridx = 2;
        mainPanel.add(step, gbc);
        
        reset = new JButton("2");
        gbc.gridx = 4;
        mainPanel.add(reset, gbc);
        
        newFile = new JButton("3");
        gbc.gridx = 6;
        mainPanel.add(newFile, gbc);
        
        help = new JButton("Help");
        gbc.gridx = 8;
        mainPanel.add(help, gbc);
        
        add(mainPanel);
        play.addActionListener(new SpecialActionListener());
        step.addActionListener(new SpecialActionListener());
        reset.addActionListener(new SpecialActionListener());
        newFile.addActionListener(new SpecialActionListener());
        help.addActionListener(new SpecialActionListener());
        setVisible(true);
        // Set the frame to be visible
        setUpAllocator();
    }
	
	public class SpecialActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JButton source = (JButton)e.getSource();
			boolean setButtonTextCompact = false, setButtonTextNormal = false;
			if(source == play) {
				if(isHelpOpen) return;// does nothing when help is open
				if(!algorithmChosen) { // if algorithm has not been chosen this button will choose best fit
					allocator.memAlgo = 0;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) {
					allocator.isCompact = true;
					compactChosen = true;
					synchronized(CompactLock) {
						CompactLock.notify();
					}
					setButtonTextNormal = true;
				}// choose compact Yes
				else {
					allocator.Paused = !allocator.Paused;
					if(allocator.Paused) play.setText("Play");
					else play.setText("Pause");
					synchronized(allocator.lock) {
						allocator.lock.notify();
					}// play/pause button
				}
			}
			else if(source  == step) {
				if(isHelpOpen) return;// does nothing when help is open
				if(!algorithmChosen) { // if algorithm has not been chosen this button will choose worst fit
					allocator.memAlgo = 1;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) {
					allocator.isCompact = false;
					compactChosen = true;
					synchronized(CompactLock) {
						CompactLock.notify();
					}
					setButtonTextNormal = true;
				} // choose compact No
				else {
					if(!allocator.Paused) return;
					allocator.steps++;
					synchronized(allocator.lock){
						allocator.lock.notify();
					} // steps in allocation algorithm
				}
			}
			else if(source == reset) {
				if(isHelpOpen) return;// does nothing when help is open
				if(!algorithmChosen) { // if algorithm has not been chosen this button will choose next fit
					allocator.memAlgo = 2;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) return; // does nothing if algorithm is chosen and compact is not
				else {
					new Thread(() -> reset()).start();
				} // resets the program with same file
			}
			else if(source == newFile) {
				if(isHelpOpen) return;// does nothing when help is open
				if(!algorithmChosen) { // if algorithm has not been chosen this button will choose first fit
					allocator.memAlgo = 3;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) return;// does nothing if algorithm is chosen and compact is not
				else {
					new Thread(() -> newFile()).start();
				} // resets the program and chooses new file
			}
			else {
				if(isHelpOpen) { // close help menu
					isHelpOpen = false;
					play.setText(currentButtons[0].equals("Pause")?"Play":currentButtons[0]); // If play button equaled pause set it back to play
					step.setText(currentButtons[1]);
					reset.setText(currentButtons[2]);
					newFile.setText(currentButtons[3]);
					help.setText(currentButtons[4]);
					new Thread(() -> endHelp()).start(); // close help menu
				}
				else {
					isHelpOpen = true;
					currentButtons[0] = play.getText(); // store the value of the buttons before entering help menu
					currentButtons[1] = step.getText();
					currentButtons[2] = reset.getText();
					currentButtons[3] = newFile.getText();
					currentButtons[4] = help.getText();
					new Thread(() -> help()).start(); //open help menu
				}
			}
			
			if(setButtonTextCompact) { // sets up buttons for compact selection
				play.setText("yes");
				step.setText("no");
				reset.setText("-");
				newFile.setText("-");
				help.setText("Help");
			}
			else if(setButtonTextNormal) { // sets buttons to normal value
				play.setText("Play");
				step.setText("Step");
				reset.setText("Reset");
				newFile.setText("New File");
				help.setText("Help");
			}
		}
		
	}
	private void reset() { // resets allocator algorithm by killing the thread and creating a new algorithm instance
		KillAllocatorThread();
		ResetAlgorithmAndSelection();
	}
	
	private void newFile() { // resets allocator algorithm with new file by killing the thread, prompting user to choose new file, and creating a new algorithm instance
		KillAllocatorThread();
		setUpFile();
		ResetAlgorithmAndSelection();
	}
	
	private void KillAllocatorThread() { // kills allocator algorithm thread and waits for it to die
		isResetRequested = true; // shares that it would like to kill the thread
		allocator.Paused = true; // sets variables to end the loop in the thread
		allocator.steps = 0;
		while(!allocator.canStep) { // waits for the thread to tell this thread that it has quit processing some data
			synchronized(ResetLock) {
				try {
					ResetLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		allocator.currentProcesses.clear(); // clear that data
		allocator.proc.clear(); // clear that data
		synchronized(allocator.resetLock) { // the allocator thread was waiting for me to clear that data. Now I'm done. this kills the thread
			allocator.resetLock.notify();	
		}
		isResetRequested = false; // I no longer would like to kill future threads
	}
	
	private void ResetAlgorithmAndSelection() { // resets the UI and the allocator by creating a new allocator
		// TODO Auto-generated method stub
		play.setText("0"); // set buttons back to beginning value
		step.setText("1");
		reset.setText("2");
		newFile.setText("3");
		help.setText("Help");
		output.setText("");
		HighlightColors.clear(); //clear the highlighting data
		HighlightCoords.clear();
		
		algorithmChosen = false; // set variables to track user interface steps back to default value
		compactChosen = false;
		isPartListDefined = false;
		Println("Choose a memory allocation algorithm\n(0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit)", Color.WHITE); // query the user for algorithm type
		
		allocator = new ContigousMemoryAllocator(size); // wait for the user to declare an algorithm
		while(!algorithmChosen) {
			synchronized(memAlgLock) {
				try {
					memAlgLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		Println("Would you like to compact the memory?\n(yes/no)", Color.WHITE); // query the user for compact
		while(!compactChosen) { // wait for the user to declare value
			synchronized(CompactLock){
				try {
					CompactLock.wait();
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		allocator.proc = allocator.generateProcesses(ProcSizeMax, NumProc, MaxProcTime); // reset allocator data
		allocator.procClone = new ArrayList<>(allocator.proc);
		allocator.currentProcesses = new ArrayList<>();
		allocator.finishedProcesses = new ArrayList<>();
		for (Process p : allocator.proc) {
			// print the randomly generated processes and their attributes
			Println(p.toString(), Color.cyan);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    isPartListDefined = true;
		Thread t = new Thread(() -> allocator.UserInterfaceStep()); // start the allocation algorithm again
		t.start();
	}
	
	private int convertToKB(String line[]) { // convert different byte units to KB
		String unit = line[3].toLowerCase();
		int value = Integer.parseInt(line[2]);
		switch (unit) {
		case "bytes":
		case "byte":
		case "b":
			return value / 1024;
		case "kilobytes":
		case "kilobyte":
		case "kb":
			return value;
		case "megabytes":
		case "megabyte":
		case "mb":
			return value * 1024;
		case "gigabytes":
		case "gigabyte":
		case "gb":
			return value * 1024 * 1024;
		default:
			System.err.println("Unsupported unit: " + unit);
			return -1;
		}
	}

	private int convertToMS(String line[]) { // convert different second units to ms
		String unit = line[3].toLowerCase();
		int value = Integer.parseInt(line[2]);
		switch (unit) {
		case "milliseconds":
		case "millisecond":
		case "ms":
			return value;
		case "seconds":
		case "second":
		case "s":
			return value * 1000;
		case "minutes":
		case "minute":
		case "min":
			return value * 60 * 1000;
		case "hours":
		case "hour":
		case "h":
			return value * 60 * 60 * 1000;
		default:
			System.err.println("Unsupported unit: " + unit);
			return -1;
		}
	}
	private String currentText = "";
	public Object HelpLock = new Object(); // waits for allocator thread to finish up
	public boolean helpRequested = false;
	private void help() {
		allocator.Paused = true; // set up help menu
		allocator.steps = 0;
		helpRequested = true;
		while(!allocator.canStep) { // once allocator thread is done it will open the help menu
			synchronized(HelpLock) {
				try {
					HelpLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			Thread.sleep(1000); // sleep to give the ui time to print data to the output JTextArea. So the highlighting does not mess up with multithreading
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		helpRequested = false;
		play.setText("-"); // change buttons to help menu
		step.setText("-");
		reset.setText("-");
		newFile.setText("-");
		help.setText("Back");
		currentText =  output.getText();
		output.setText("The memory bar on the right:\nRed portions are free memory. All other colors are partitions that are not available.\n\n"
				+"Compact:\nIf compact is turned on, the free partitions will be compacted at the end of memory. If not free partitions will not be moved.\n\n"
				+"Step:\nClick x times to take x steps in the memory allocation algorithm.\n\n"
				+"Play/Pause:\nPlay will automatically run through the algorithm. And pause will stop the algorithm.\n\n"
				+"Reset:\nReset will restart the algorithm selection process with the same file that was initially input.\n\n"
				+"New File:\nWill restart the algorithm selection process with a new file.\n\n\n\n\n\n\n\n\n\n");
		//display help text
	}
	
	private void endHelp() { // close the help menu and set the highlights back
		output.setText(currentText);
		output.setCaretPosition(output.getDocument().getLength());
		highlight();
	}
	
	private void setUpFile() { // function allows user to input a configuration file and processes the configuration.
		boolean fileNotChosen = true;
		while (fileNotChosen) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file", "txt");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				Scanner scr;
				try {
					scr = new Scanner(file);
					while (scr.hasNextLine()) {
						String line = scr.nextLine();
						String arr[] = line.split(" ");
						if (arr.length < 3 || !arr[1].equals("=")) // if line is misconfigured will skip the line
							continue;
						String key = arr[0].toUpperCase();
						switch (key) {
						case "MEMORY_MAX": // line specifies memory max
							if (arr.length < 4)
								continue;
							MemoryMax = convertToKB(arr);
							size = MemoryMax;
							Println("Memory Max: " + MemoryMax, Color.cyan);
							break;
						case "PROC_SIZE_MAX": // line specifies proc size max
							if (arr.length < 4)
								continue;
							ProcSizeMax = convertToKB(arr);
							Println("Proc_Size_Max: " + ProcSizeMax, Color.cyan);
							break;
						case "NUM_PROC": // line specifies number of processes
							NumProc = Integer.parseInt(arr[2]);
							Println("Num Proc: " + NumProc, Color.cyan);
							break;
						case "MAX_PROC_TIME": // line specifies number of processes
							if (arr.length < 4)
								continue;
							MaxProcTime = convertToMS(arr);
							Println("Max Proc Time: " + MaxProcTime, Color.cyan);
							break;
						default:
							Println("The key {" + arr[0] + "} in the config file is not supported.", Color.RED);
						}
					}
					scr.close();
					if (MemoryMax == -1 || ProcSizeMax == -1 || NumProc == -1 || MaxProcTime == -1) { // if any of the necessary data is missing we will print an error and open the JFileChooser window again
						Println("The input file is missing an important parameter.", Color.RED);
					} else {
						fileNotChosen = false;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void setUpAllocator() {
		setUpFile();// choose file
		
		Println("Choose a memory allocation algorithm\n(0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit)", Color.WHITE); // user prompted to choose algorithm
		
		allocator = new ContigousMemoryAllocator(size);
		while(!algorithmChosen) { // ui must wait for user to choose algorithm
			synchronized(memAlgLock) {
				try {
					memAlgLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		 // user prompted to choose compact yes/no
		Println("Would you like to compact the memory?\n(yes/no)", Color.WHITE);
		while(!compactChosen) { // waits for user to choose
			synchronized(CompactLock){
				try {
					CompactLock.wait();
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		allocator.proc = allocator.generateProcesses(ProcSizeMax, NumProc, MaxProcTime);
		allocator.procClone = new ArrayList<>(allocator.proc);
		allocator.currentProcesses = new ArrayList<>();
		allocator.finishedProcesses = new ArrayList<>();
		for (Process p : allocator.proc) {
			// print the randomly generated processes and their attributes
			Println(p.toString(), Color.cyan);
		}
		
	    isPartListDefined = true; // part list is now defined
		Thread t = new Thread(() -> allocator.UserInterfaceStep());
		t.start(); // can now start the allocator algorithm step
	}
	
	public void Println(String input, Color c) { // print line to output + \n
		cacheNewHighlight(c, output.getText(), input);
		output.setText(output.getText() + input + "\n");
		highlight();
		output.setCaretPosition(output.getDocument().getLength());
	}
	
	//store a new highlight coordinates and color in the list of highlights in the JTextArea
	public void cacheNewHighlight(Color c, String currentText, String newText) {
        int lastLineStart = currentText.length();
        int lastLineEnd = currentText.length()+newText.length();
		HighlightCoords.add(new Pair<Integer, Integer>(lastLineStart, lastLineEnd));
		HighlightColors.add(c);
	}
	
	private void highlight() { // highlights all of the lines to their respective color. Called each time the text is reset
		Highlighter highlighter = output.getHighlighter();
		for(int i = 0; i < HighlightCoords.size(); i++) {
			int lineStart = HighlightCoords.get(i).key;
			int lineEnd = HighlightCoords.get(i).value;
			if(lineStart == -1) continue;
			try {
				highlighter.addHighlight(lineStart, lineEnd, new DefaultHighlighter.DefaultHighlightPainter(HighlightColors.get(i)));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Creates a new thread for the allocator step algorithm
	public void createNewUserInterfaceThread() {
		Thread t = new Thread(() ->allocator.UserInterfaceStep());
		t.start();
	}
	
	public class MemoryVisual extends JPanel {
		private int x, y;
		private int colorIdx = 0;
		JLabel title;
		public MemoryVisual(Color c, int x, int y, String title) {
			this.title = new JLabel(title);
			setBackground(c);
			this.x = x;
			this.y = y;
			
			Border border = BorderFactory.createLineBorder(Color.BLACK);
			setBorder(border);
			setLayout(null);
			add(this.title);
		}
		
		//Cycles through colors for the partitions that are not free
		private Color alternateColors() {
			switch(colorIdx++) {
			case 0:
				return Color.blue;
			case 1:
				return Color.green;
			case 2:
				return Color.orange;
			case 3:
				colorIdx = 0;
				return Color.gray;
			}
			return Color.blue;
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			setSize(this.x, this.y); // set size of paint component to x and y
			if(!isPartListDefined) return; // if part list is not defined we will not paint on the paint componenet
			
			for(int i = 0; i < allocator.partList.size(); i++) { // paint each partition in the partlist onto the paint component
				Partition part = allocator.partList.get(i);
				int getStart = part.getBase();
				int getEnd = part.getLength() + part.getBase();
				Color partitionColor = alternateColors();
				if(part.isbFree()) {
					partitionColor = Color.red;
					colorIdx--;
				}
				double startPercent = (double)getStart/allocator.size; // take the percentage that the partition takes up relative to the size of the memory container
				double endPercent = (double)getEnd/allocator.size;
				int adjustedStart = (int)(startPercent * this.y); // and use this percentage to get the relative size in the paint component in pixels.
				int adjustedEnd = (int)(endPercent * this.y);
				
				g.setColor(partitionColor);
				g.fillRect(0, adjustedStart, this.x, adjustedEnd-adjustedStart);
			}
		}
	}
	
	

}
