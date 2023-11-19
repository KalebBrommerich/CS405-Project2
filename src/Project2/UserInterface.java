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
	private int MemoryMax = -1, ProcSizeMax = -1, NumProc = -1, MaxProcTime = -1, size = -1;
	private Object memAlgLock = new Object();
	private boolean algorithmChosen = false, compactChosen = false;
	private JTextArea output;
	private int lineindex = 0;
	private JScrollPane scroller;
	private ContigousMemoryAllocator allocator;
	private JButton play, step, reset, tmp1, tmp2;
	private ArrayList<Pair<Integer, Integer>> HighlightCoords = new ArrayList<Pair<Integer, Integer>>();
	private ArrayList<Color> HighlightColors = new ArrayList<Color>();
	public MemoryVisual mv; 
	boolean isPartListDefined = false;
	private Object CompactLock = new Object();
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
        mv = new MemoryVisual(Color.RED, 138, 360, "Memory");
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
        
        tmp1 = new JButton("3");
        gbc.gridx = 6;
        mainPanel.add(tmp1, gbc);
        
        tmp2 = new JButton("-");
        gbc.gridx = 8;
        mainPanel.add(tmp2, gbc);
        
        add(mainPanel);
        play.addActionListener(new SpecialActionListener());
        step.addActionListener(new SpecialActionListener());
        reset.addActionListener(new SpecialActionListener());
        tmp1.addActionListener(new SpecialActionListener());
        tmp2.addActionListener(new SpecialActionListener());
        setVisible(true);
        // Set the frame to be visible
        setUpAllocator();
    }
	
	private void ResetAlgorithmAndSelection() {
		// TODO Auto-generated method stub
		System.out.println("Here");
		play.setText("0");
		step.setText("1");
		reset.setText("2");
		tmp1.setText("3");
		tmp2.setText("-");
		output.setText("");
		HighlightColors.clear();
		HighlightCoords.clear();
		allocator.Paused = true;
		allocator.steps = 0;
		algorithmChosen = false;
		compactChosen = false;
		isPartListDefined = false;
		System.out.println("Here3");
		repaint();
		Println("Choose a memory allocation algorithm\n(0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit)", Color.WHITE);

		System.out.println("Here4");
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
	
	public class SpecialActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JButton source = (JButton)e.getSource();
			boolean setButtonTextCompact = false, setButtonTextNormal = false;
			if(source == play) {
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
				else ResetAlgorithmAndSelection();
			}
			else if(source == tmp1) {
				if(!algorithmChosen) {
					allocator.memAlgo = 3;
					algorithmChosen = true;
					synchronized(memAlgLock) {
						memAlgLock.notify();
					}
					setButtonTextCompact = true;
				}
				else if(!compactChosen) return;
			}
			
			if(setButtonTextCompact) {
				play.setText("yes");
				step.setText("no");
				reset.setText("-");
				tmp1.setText("-");
				tmp2.setText("-");
			}
			else if(setButtonTextNormal) {
				play.setText("Play");
				step.setText("Step");
				reset.setText("reset");
				tmp1.setText("tmp1");
				tmp2.setText("tmp2");
			}
		}
		
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
	public void setUpAllocator() {
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
		lineindex++;
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
			System.out.println("Hello");
			setSize(this.x, this.y);
			if(!isPartListDefined) return;

			System.out.println(allocator.partList.size());
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
