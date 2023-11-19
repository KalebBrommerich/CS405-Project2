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
	private File file;
	boolean isResetRequested = false;
	private int MemoryMax = -1, ProcSizeMax = -1, NumProc = -1, MaxProcTime = -1, size = -1;
	private Object memAlgLock = new Object();
	private boolean algorithmChosen = false, compactChosen = false;
	private JTextArea output;
	private JScrollPane scroller;
	private ContigousMemoryAllocator allocator;
	private JButton play, step, reset, newFile, help;
	private ArrayList<Pair<Integer, Integer>> HighlightCoords = new ArrayList<Pair<Integer, Integer>>();
	private ArrayList<Color> HighlightColors = new ArrayList<Color>();
	public MemoryVisual mv; 
	boolean isPartListDefined = false;
	public Object ResetLock = new Object();
	private Object CompactLock = new Object();
	private boolean isHelpOpen = false;
	String currentButtons[] = new String[5];
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
        //scroller.setPreferredSize(new Dimension(300, 400));
		//output.setMaximumSize(new Dimension(300, 400));
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
				if(isHelpOpen) return;
				if(!algorithmChosen) {
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
				}
				else {
					allocator.Paused = !allocator.Paused;
					if(allocator.Paused) play.setText("Play");
					else play.setText("Pause");
					synchronized(allocator.lock) {
						allocator.lock.notify();
					}
				}
			}
			else if(source  == step) {
				if(isHelpOpen) return;
				if(!algorithmChosen) {
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
				}
				else {
					if(!allocator.Paused) return;
					allocator.steps++;
					synchronized(allocator.lock){
						allocator.lock.notify();
					}
				}
			}
			else if(source == reset) {
				if(isHelpOpen) return;
				System.out.println("Detected");
				if(!algorithmChosen) {
					allocator.memAlgo = 2;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) return;
				else {
					new Thread(() -> reset()).start();
				}
			}
			else if(source == newFile) {
				if(isHelpOpen) return;
				if(!algorithmChosen) {
					allocator.memAlgo = 3;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) return;
				else {
					new Thread(() -> newFile()).start();
				}
			}
			else {
				if(isHelpOpen) {
					isHelpOpen = false;
					play.setText(currentButtons[0].equals("Pause")?"Play":currentButtons[0]);
					step.setText(currentButtons[1]);
					reset.setText(currentButtons[2]);
					newFile.setText(currentButtons[3]);
					help.setText(currentButtons[4]);
					new Thread(() -> endHelp()).start();
				}
				else {
					isHelpOpen = true;
					currentButtons[0] = play.getText();
					currentButtons[1] = step.getText();
					currentButtons[2] = reset.getText();
					currentButtons[3] = newFile.getText();
					currentButtons[4] = help.getText();
					new Thread(() -> help()).start();
				}
			}
			
			if(setButtonTextCompact) {
				play.setText("yes");
				step.setText("no");
				reset.setText("-");
				newFile.setText("-");
				help.setText("Help");
			}
			else if(setButtonTextNormal) {
				play.setText("Play");
				step.setText("Step");
				reset.setText("Reset");
				newFile.setText("New File");
				help.setText("Help");
			}
		}
		
	}
	private void reset() {
		KillAllocatorThread();
		ResetAlgorithmAndSelection();
	}
	
	private void newFile() {
		KillAllocatorThread();
		setUpFile();
		ResetAlgorithmAndSelection();
	}
	
	private void KillAllocatorThread() {
		isResetRequested = true;
		allocator.Paused = true;
		allocator.steps = 0;
		while(!allocator.canStep) {
			synchronized(ResetLock) {
				try {
					ResetLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		allocator.currentProcesses.clear();
		allocator.proc.clear();
		synchronized(allocator.resetLock) {
			allocator.resetLock.notify();	
		}
		isResetRequested = false;
	}
	
	private void ResetAlgorithmAndSelection() {
		// TODO Auto-generated method stub
		play.setText("0");
		step.setText("1");
		reset.setText("2");
		newFile.setText("3");
		help.setText("Help");
		output.setText("");
		HighlightColors.clear();
		HighlightCoords.clear();
		
		algorithmChosen = false;
		compactChosen = false;
		isPartListDefined = false;
		Println("Choose a memory allocation algorithm\n(0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit)", Color.WHITE);
		
		allocator = new ContigousMemoryAllocator(size);
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
		
		Println("Would you like to compact the memory?\n(yes/no)", Color.WHITE);
		while(!compactChosen) {
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    isPartListDefined = true;
		Thread t = new Thread(() -> allocator.UserInterfaceStep());
		t.start();
	}
	
	private int convertToKB(String line[]) {
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

	private int convertToMS(String line[]) {
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
	public Object HelpLock = new Object();
	public boolean helpRequested = false;
	private Highlight[] hiarr;
	//private Highlighter tmphl;
	private void help() {
		allocator.Paused = true;
		allocator.steps = 0;
		helpRequested = true;
		while(!allocator.canStep) {
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
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		helpRequested = false;
		play.setText("-");
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
				+"New File:\nWill restart the algorithm selection process with a new file.\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
	}
	
	private void endHelp() {
		output.setText(currentText);
		output.setCaretPosition(output.getDocument().getLength());
		highlight();
	}
	
	private void setUpFile() {
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
						if (arr.length < 3 || !arr[1].equals("="))
							continue;
						String key = arr[0].toUpperCase();
						switch (key) {
						case "MEMORY_MAX":
							if (arr.length < 4)
								continue;
							MemoryMax = convertToKB(arr);
							size = MemoryMax;
							Println("Memory Max: " + MemoryMax, Color.cyan);
							break;
						case "PROC_SIZE_MAX":
							if (arr.length < 4)
								continue;
							ProcSizeMax = convertToKB(arr);
							Println("Proc_Size_Max: " + ProcSizeMax, Color.cyan);
							break;
						case "NUM_PROC":
							NumProc = Integer.parseInt(arr[2]);
							Println("Num Proc: " + NumProc, Color.cyan);
							break;
						case "MAX_PROC_TIME":
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
					if (MemoryMax == -1 || ProcSizeMax == -1 || NumProc == -1 || MaxProcTime == -1) {
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
		setUpFile();
		
		Println("Choose a memory allocation algorithm\n(0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit)", Color.WHITE);
		
		allocator = new ContigousMemoryAllocator(size);
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
		
		Println("Would you like to compact the memory?\n(yes/no)", Color.WHITE);
		while(!compactChosen) {
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
		
	    isPartListDefined = true;
		Thread t = new Thread(() -> allocator.UserInterfaceStep());
		t.start();
	}
	
	public void Println(String input, Color c) {
		cacheNewHighlight(c, output.getText(), input);
		output.setText(output.getText() + input + "\n");
		highlight();
		output.setCaretPosition(output.getDocument().getLength());
	}
	
	public void Print(String input) {
		output.setText(output.getText() + input);
	}
	
	public void cacheNewHighlight(Color c, String currentText, String newText) {
        int lastLineStart = currentText.length();
        int lastLineEnd = currentText.length()+newText.length();
		HighlightCoords.add(new Pair<Integer, Integer>(lastLineStart, lastLineEnd));
		HighlightColors.add(c);
	}
	
	private void highlight() {
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
	
	public void setText(String input) {
		output.setText(input);
	}
	
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
			//setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			//this.title.setBounds(5, 5, 100, 14);
			add(this.title);
		}
		
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
			setSize(this.x, this.y);
			if(!isPartListDefined) return;
			for(int i = 0; i < allocator.partList.size(); i++) {
				Partition part = allocator.partList.get(i);
				int getStart = part.getBase();
				int getEnd = part.getLength() + part.getBase();
				//System.out.println("Start: " + getStart);
				//System.out.println("End: " + getEnd);
				Color partitionColor = alternateColors();
				if(part.isbFree()) {
					partitionColor = Color.red;
					colorIdx--;
				}
				double startPercent = (double)getStart/allocator.size;
				double endPercent = (double)getEnd/allocator.size;
				int adjustedStart = (int)(startPercent * this.y);
				int adjustedEnd = (int)(endPercent * this.y);
				
				g.setColor(partitionColor);
				g.fillRect(0, adjustedStart, this.x, adjustedEnd-adjustedStart);
			}
		}
	}
	
	

}
