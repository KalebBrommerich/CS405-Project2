package Project2;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.Pointer;

public class ContigousMemoryAllocator {
	private int size; // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap; // map process to partition
	private List<Partition> partList; // list of memory partitions

	private static ContigousMemoryAllocator allocator;
	private static ArrayList<Process> procClone;
	private static ArrayList<Process> currentProcesses;
	private static ArrayList<Process> finishedProcesses;
	private static ArrayList<Process> proc;
	// constructor

	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size, -1)); // add the first hole, which is the whole memory at start up
	}

	// prints the allocation map (free + allocated) in ascending order of base
	// addresses
	public void print_status() {
		// TODO: add code below
		order_partitions();
		System.out.printf("Paritions [Allocated=%d KB, Free=%d KB \n", allocated_memory(), free_memory());
		for (Partition part : partList) {
			System.out.printf("Address [%d:%d] %s (%d KB) {%d ms}\n", part.getBase(),
					part.getBase() + part.getLength() - 1, part.isbFree() ? "Free" : part.getProcess(),
					part.getLength(), part.getRemainingTime());
		}
	}

	// print the number of holes, the average size of the holes,
	// total size of all of the holes, and the percentage of total free memory
	// partition over the total memory
	public void print_stats() {
		DecimalFormat df = new DecimalFormat("#.00");
		int numHoles = 0;
		int holeSize = 0;
		int totalHoleSize = 0;
		for (Partition part : partList) {
			if (part.isbFree()) {
				numHoles++;
				holeSize = part.getLength();
				totalHoleSize += holeSize;
			}
		}
		System.out.println("Holes: " + numHoles);
		if(numHoles == 0)  System.out.println("Average: 0 KB"); // NOT SURE IF THIS FIX DIVISION BY 0
		else System.out.println("Average: " + totalHoleSize / numHoles + " KB");
		System.out.println("Total: " + totalHoleSize + " KB");
		System.out.println("Percent: " + df.format((double) totalHoleSize / (double) size * 100) + "%");

	}

	// get the size of total allocated memory
	private int allocated_memory() {
		// TODO: add code below
		int size = 0;
		for (Partition part : partList) {
			if (!part.isbFree())
				size += part.getLength();
		}
		return size;
	}

	// get the size of total free memory
	private int free_memory() {
		// TODO: add code below
		int size = 0;
		for (Partition part : partList) {
			if (part.isbFree())
				size += part.getLength();
		}
		return size;
	}

	// sort the list of partitions in ascending order of base addresses
	private void order_partitions() {
		// TODO: add code below
		Collections.sort(partList, (o1, o2) -> (o1.getBase() - o2.getBase()));
	}

	// implements the first fit memory allocation algorithm
	public int first_fit(String process, int size, int time) {
		// TODO: add code below
		if (allocMap.containsKey(process))
			return -1;// process allocated a partition already
		int index = 0, alloc = -1;
		while (index < partList.size()) {
			Partition part = partList.get(index);
			// part.getLength is the size of the partitions
			if (part.isbFree() && part.getLength() >= size) {// found a good partition
				Partition allocPart = new Partition(part.getBase(), size, time);
				allocPart.setbFree(false);
				allocPart.setProcess(process);
				allocPart.setRemainingTime(time);
				partList.add(index, allocPart);// insert this partition to list
				allocMap.put(process, allocPart);
				part.setBase(part.getBase() + size);
				part.setLength(part.getLength() - size);
				if (part.getLength() == 0)
					partList.remove(part);
				alloc = size;
				break;
			}
			index++;
		}
		return alloc;
	}

	public int best_fit(String process, int size, int time) {
		// TODO: add code below
		// System.out.println("Start First Fit Method: size=" + size);
		if (allocMap.containsKey(process))
			return -1;// process allocated a partition already
		int index = 0, alloc = -1, partSize = Integer.MAX_VALUE, candidateIndex = -1;
		// System.out.println("Start While Loop in FFM");
		while (index < partList.size()) {
			Partition part = partList.get(index);
			if (part.isbFree() && part.getLength() >= size) {
				if (partSize > part.getLength() - size) {
					partSize = part.getLength() - size;
					candidateIndex = index;
				}
			}
			index++;
		}
		if (candidateIndex >= 0) {// found a good partition
			Partition part = partList.get(candidateIndex);
			Partition allocPart = new Partition(part.getBase(), size, time);
			allocPart.setbFree(false);
			allocPart.setProcess(process);
			allocPart.setRemainingTime(time);
			partList.add(index, allocPart);// insert this partition to list
			allocMap.put(process, allocPart);
			part.setBase(part.getBase() + size);
			part.setLength(part.getLength() - size);
			if (part.getLength() == 0)
				partList.remove(part);
			alloc = size;
		}
		return alloc;
	}

	public int worst_fit(String process, int size, int time) {
		// TODO: add code below
		// System.out.println("Start First Fit Method: size=" + size);
		if (allocMap.containsKey(process))
			return -1;// process allocated a partition already
		int index = 0, alloc = -1, partSize = -1, candidateIndex = -1;
		// System.out.println("Start While Loop in FFM");
		while (index < partList.size()) {
			Partition part = partList.get(index);
			if (part.isbFree() && part.getLength() >= size) {
				if (partSize < part.getLength() - size) {
					partSize = part.getLength() - size;
					candidateIndex = index;
				}
			}
			index++;
		}
		if (candidateIndex >= 0) {// found a good partition
			Partition part = partList.get(candidateIndex);
			Partition allocPart = new Partition(part.getBase(), size, time);
			allocPart.setbFree(false);
			allocPart.setProcess(process);
			allocPart.setRemainingTime(time);
			partList.add(index, allocPart);// insert this partition to list
			allocMap.put(process, allocPart);
			part.setBase(part.getBase() + size);
			part.setLength(part.getLength() - size);
			if (part.getLength() == 0)
				partList.remove(part);
			alloc = size;
		}
		return alloc;
	}

	int pointer = 0;
	public int next_fit(String process, int size, int time) {
		//the -1 is temporary so no errors show up
		if(allocMap.containsKey(process))
			return -1; // process allocated a partition already
		int index = 0, alloc = -1;
		while (index < partList.size()) {
			Partition part = partList.get((pointer+index)%partList.size());
			// part.getLength is the size of the partitions
			if (part.isbFree() && part.getLength() >= size) {// found a good partition
				Partition allocPart = new Partition(part.getBase(), size, time);
				allocPart.setbFree(false);
				allocPart.setProcess(process);
				allocPart.setRemainingTime(time);
				partList.add(index, allocPart);// insert this partition to list
				allocMap.put(process, allocPart);
				part.setBase(part.getBase() + size);
				part.setLength(part.getLength() - size);
				if (part.getLength() == 0)
					partList.remove(part);
				alloc = size;
				break;
			}
			index++;
		}
		pointer += index+1;
		return alloc;
	}

	// release the allocated memory of a process
	public int release(String process) {
		// TODO: add code below
		if (!allocMap.containsKey(process))
			return -1;// no partition allocated to process
		int size = -1;
		for (Partition part : partList) {
			if (!part.isbFree() && process.equals(part.getProcess()) && (part.getRemainingTime() <= 0)) {
				part.setbFree(true);
				part.setProcess(null);
				part.setRemainingTime(0);
				size = part.getLength();
				break;
			}
		}
		if (size < 0)
			return size;
		//merge_holes();
		//merge_adj_holes();
		return size;
	}

	private void adjustAddresses(int index, int adjustSize) {
		for (int i = index; i < partList.size(); i++) {
			partList.get(i).setBase(partList.get(i).getBase() - adjustSize);
		}

	}

	private void merge_adj_holes() {
		order_partitions();
		int i = 0;
		while(i < partList.size()-1) {
			Partition part = partList.get(i++);
			if(part.isbFree()) {
				Partition part1 = partList.get(i);
				if(part1.isbFree()) {
					part.setLength(part.getLength() + part1.getLength());
					//int adjustSize = part.getLength();
					partList.remove(part1);
					System.out.println("merge");
					i--;
				}
			}
		}

	}
	// procedure to merge adjacent holes
	private void merge_holes() {
		order_partitions();
		int i = 0;
		while (i < partList.size() - 1) {
			Partition part = partList.get(i);
			if (part.isbFree()) {// at i
				for (int j = i + 1; j < partList.size(); j++) {
					if (partList.get(j).isbFree()) {
						partList.get(j).setLength((part.getLength() + partList.get(j).getLength()));
						int adjustSize = part.getLength();
						partList.remove(part);
						System.out.println("merge");
						adjustAddresses(i, adjustSize);
					}
				}
			}
			i++;
		}
	}

	public static int convertToKB(String line[]) {
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

	public static int convertToMS(String line[]) {
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

	private static ArrayList<Process> generateProcesses(int procSizeMax, int numProc, int maxProcTime) {
		ArrayList<Process> temp = new ArrayList<>();

		for (int i = 0; i < numProc; i++) { // round MS to Seconds
			temp.add(new Process("P" + i, (int) (Math.random() * procSizeMax)+1,
					(int) (Math.random() * maxProcTime)));
		}
		return temp;
	}

	public void decrementTime() {
		for (Partition part : partList) {
			if (!part.isbFree() && part.getRemainingTime() > 0) {
				part.setRemainingTime(part.getRemainingTime() - 1000);
			}
		}
	}

	private static boolean Paused = true;
	private static boolean isFinished = false;
	private static int steps = 0;
	private static int memAlgo = 0;
	private static Object lock = new Object();
	private static class KeyboardProc implements WinUser.LowLevelKeyboardProc{
		@Override
		public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
			// TODO Auto-generated method stub
			if(nCode >= 0) {
				int vkCode = lParam.vkCode;
				int eventType = wParam.intValue();
				System.out.println("nCode: " + nCode);
				//If the press was space key
				if(vkCode == 32) {
					if(eventType == WinUser.WM_KEYUP) {
						Paused = !Paused;
						System.out.println("pressed");
						synchronized(lock) {
							lock.notify();
						}
					}
				}
				
				if(vkCode == 'S' || vkCode == 's') {
					if(eventType == WinUser.WM_KEYUP) {
						steps++;
						synchronized(lock) {
							lock.notify();
						}
					}
				}
			}
			return User32.INSTANCE.CallNextHookEx(null, nCode, wParam, new WinDef.LPARAM(Pointer.nativeValue(lParam.getPointer())));
		}
	}
	
	public void UserInterfaceStep() {
		boolean isPlaying = false;
		synchronized(lock) {
			while(Paused && steps == 0) {
				try {
					System.out.println("[Space]:Play/Pause\n[s]:Step");
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		while((!Paused || steps > 0 || isPlaying) && (proc.size() > 0 || currentProcesses.size() > 0)) {
			System.out.println("Proc size: " + proc.size() + ", current proc size: " + currentProcesses.size());
			if(steps != 0) steps--;
			if (currentProcesses.size() > 0) {
				for(int i = 0; i < currentProcesses.size(); i++) {
					Process p = currentProcesses.get(i);
					if (allocator.release(p.getProcName()) > 0) {
						System.out.println("Succesfully deallocated " + p.getProcName());
						finishedProcesses.add(p);
						currentProcesses.remove(i);
					}
				}
			}
			// allocate partitions
			for (Process p : procClone) {
				String process = p.getProcName();
				int Size = p.getProcSize();
				int Time = p.getProcTime();
				switch (memAlgo) {
				case 0:
					if (allocator.best_fit(process, Size, Time) > 0) {
						System.out.println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process);
						proc.remove(p);
						currentProcesses.add(p);
					} else {
						System.err.println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process);
					}
					break;
				case 1:
					if (allocator.worst_fit(process, Size, Time) > 0) {
						System.out.println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process);
						proc.remove(p);
						currentProcesses.add(p);
					} else {
						System.err.println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process);
					}
					break
					;
				case 2:
					if (allocator.next_fit(process, Size, Time) > 0) {
						System.out.println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process);
						proc.remove(p);
						currentProcesses.add(p);
					} else {
						System.err.println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process);
					}
					break;
				case 3:
						if (allocator.first_fit(process, Size, Time) > 0) {
							System.out.println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process);
							proc.remove(p);
							currentProcesses.add(p);
						} else {
							System.err.println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process);
						}
						break;
						default:
							System.err.println("Invalid input");
							System.exit(-1);
				}
			}
			allocator.merge_adj_holes();
			allocator.print_status();
			allocator.print_stats();
			procClone = new ArrayList<>(proc);
			allocator.decrementTime();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(proc.size() == 0 && currentProcesses.size() == 0)
			isFinished = true;
	}

	public synchronized void UserInput() {

	}

	public static void main(String args[]) {
		int MemoryMax = -1, ProcSizeMax = -1, NumProc = -1, MaxProcTime = -1, size = -1;
		boolean fileNotChosen = true;
		while (fileNotChosen) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file", "txt");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
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
							System.out.println("Memory Max: " + MemoryMax);
							break;
						case "PROC_SIZE_MAX":
							if (arr.length < 4)
								continue;
							ProcSizeMax = convertToKB(arr);
							System.out.println("Proc_Size_Max: " + ProcSizeMax);
							break;
						case "NUM_PROC":
							NumProc = Integer.parseInt(arr[2]);
							System.out.println("Num Proc: " + NumProc);
							break;
						case "MAX_PROC_TIME":
							if (arr.length < 4)
								continue;
							MaxProcTime = convertToMS(arr);
							System.out.println("Max Proc Time: " + MaxProcTime);
							break;
						default:
							System.out.println("The key {" + arr[0] + "} in the config file is not supported.");
						}
					}
					scr.close();
					if (MemoryMax == -1 || ProcSizeMax == -1 || NumProc == -1 || MaxProcTime == -1) {
						System.out.println("The input file is missing an important parameter.");
					} else {
						fileNotChosen = false;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Scanner sc = new Scanner(System.in);
		System.out.print("Choose a memory allocation algorithm (0 - Best Fit, 1 - Worst Fit, 2 - Next Fit, 3 - First Fit):");
		memAlgo = sc.nextInt();
		sc.close();
		proc = generateProcesses(ProcSizeMax, NumProc, MaxProcTime);
		for (Process p : proc) {
			// print the randomly generated processes and their attributes
			System.out.println(p.toString());
		}

		allocator = new ContigousMemoryAllocator(size);
		procClone = new ArrayList<>(proc);
		currentProcesses = new ArrayList<>();
		finishedProcesses = new ArrayList<>();
		
		User32 user32 = User32.INSTANCE;
		HHOOK hhk = user32.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, new KeyboardProc(), null, 0);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> user32.UnhookWindowsHookEx(hhk)));
		System.out.println("Waiting after thread made");
		Paused = false;
		while(!isFinished) {
			allocator.UserInterfaceStep();
		}
	}
}