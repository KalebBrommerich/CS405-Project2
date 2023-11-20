package Project2;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContigousMemoryAllocator {
	public int size; // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap; // map process to partition
	public List<Partition> partList; // list of memory partitions
	public boolean isCompact = false; // is the allocator compacting partitions
	public ArrayList<Process> procClone; // duplicate of proc to maintain integrity during foreach loop, while proc is being modified
	public ArrayList<Process> currentProcesses; // Processes currently in the memory
	public ArrayList<Process> finishedProcesses; // Processes that have completed
	public ArrayList<Process> proc; // Processes
	public Object resetLock = new Object(); // Lock that manages the reset button on the interface
	private static UserInterface ui; // The user interface variable
	// constructor

	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size, 0)); // add the first hole, which is the whole memory at start up
	}

	// prints the allocation map (free + allocated) in ascending order of base
	// addresses
	public void print_status() {
		// TODO: add code below
		order_partitions();
		//System.out.printf("Paritions [Allocated=%d KB, Free=%d KB \n", allocated_memory(), free_memory());
		ui.Println("Partitions [Allocated="+allocated_memory()+", Free="+free_memory()+"]", Color.yellow);
		for (Partition part : partList) {
			ui.Println("Address " + "[" + part.getBase() + ":" + (part.getBase() + part.getLength()) + "] " + (part.isbFree() ? "Free" : part.getProcess())
					+ " ("+part.getLength() + ")" + " {" + part.getRemainingTime() + "}", Color.yellow);
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
		ui.Println("Holes: " + numHoles, Color.orange);
		if(numHoles == 0)  ui.Println("Average: 0 KB", Color.orange);
		else ui.Println("Average: " + totalHoleSize / numHoles + " KB", Color.orange);
		ui.Println("Total: " + totalHoleSize + " KB", Color.orange);
		ui.Println("Percent: " + df.format((double) totalHoleSize / (double) size * 100) + "%", Color.orange);

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
		if (allocMap.containsKey(process))
			return -1;// process allocated a partition already
		int index = 0, alloc = -1, partSize = -1, candidateIndex = -1;
		while (index < partList.size()) {
			Partition part = partList.get(index);
			if (part.isbFree() && part.getLength() >= size) { // Find worst fit
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
			Partition part = partList.get((pointer+index)%partList.size()); // (pointer + index)%list size will cover each idx starting at pointer.
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
		pointer += index; // Pointer increments by index. The number of partitions covered.
		return alloc;
	}

	// release the allocated memory of a process
	public int release(String process) {
		// TODO: add code below
		if (!allocMap.containsKey(process))
			return -1;// no partition allocated to process
		int size = -1;
		int idx = 0;
		for (Partition part : partList) {
			if (!part.isbFree() && process.equals(part.getProcess()) && (part.getRemainingTime() <= 0)) {
				part.setbFree(true);
				part.setProcess(null);
				part.setRemainingTime(0);
				size = part.getLength();
				idx++;
				break;
			}
		}
		if (size < 0)
			return size;
		if(isCompact) compact_holes();
		if(memAlgo == 2) { // If the algorithm is next fit, the pointer needs to stay on the partition it left off on.
			if(idx < pointer) {
				pointer--;
			}
		}
		return size;
	}

	private void adjustAddresses(int index, int adjustSize) { // offset all addresses after index by a value
		for (int i = index; i < partList.size(); i++) {
			partList.get(i).setBase(partList.get(i).getBase() - adjustSize);
		}

	}

	private void merge_adj_holes() {
		order_partitions(); // sort the partitions by base
		int i = 0;
		while(i < partList.size()-1) { // if two adjacent holes are free we will merge them into one hole
			Partition part = partList.get(i++);
			if(part.isbFree()) {
				Partition part1 = partList.get(i);
				if(part1.isbFree()) {
					part.setLength(part.getLength() + part1.getLength());
					//int adjustSize = part.getLength();
					partList.remove(part1);
					if(memAlgo == 2 && i+1 < pointer) { // for next fit we keep pointer on same element
						pointer--;
					}
					i--;
				}
			}
		}

	}
	// procedure to merge adjacent holes
	private void compact_holes() {
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
						if(memAlgo == 2 && i < pointer) { // for next fit we keep pointer on same element
							pointer--;
						}
						adjustAddresses(i, adjustSize);
					}
				}
			}
			i++;
		}
	}



	public ArrayList<Process> generateProcesses(int procSizeMax, int numProc, int maxProcTime) {
		ArrayList<Process> temp = new ArrayList<>();

		for (int i = 0; i < numProc; i++) { // round MS to Seconds
			temp.add(new Process("P" + i, (int) (Math.random() * procSizeMax)+1, // generate a bunch of random numbers for the processes
					(int) (Math.random() * maxProcTime)));
		}
		return temp;
	}

	public void decrementTime() {
		for (Partition part : partList) {
			if (!part.isbFree() && part.getRemainingTime() > 0) {
				part.setRemainingTime(part.getRemainingTime() - 1000); // subtract the time from each process by 1 second
			}
		}
	}

	public boolean Paused = true; // Is the Allocation algorithm paused
	public int steps = 0; // Has the allocation algorithm been commanded to take a step?
	public int memAlgo = 0; // what is the allocation algorithm we are using
	public Object lock = new Object(); // controls when the algorithm should run
	public static Object dataLock = new Object(); // controls read access to data in the algorithm for the interface
	public boolean canStep = true; // tracks when the algorithm is executing
	
	public void UserInterfaceStep() {
		//boolean isPlaying = false;
		synchronized(lock) {
			while(Paused && steps == 0) { // Am I not being commanded to execute by the UI
				try {
					ui.Println("Press play\nor Press step", Color.WHITE);
					lock.wait(); // wait for UI to notify me
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		canStep = false; // The algorithm is happening now
		while((!Paused || steps > 0) && (proc.size() > 0 || currentProcesses.size() > 0)) { // Did the interface tell me to stop or is the algorithm complete
			ui.Println("Proc size: " + proc.size() + ", current proc size: " + currentProcesses.size(), Color.pink);
			if(steps != 0) steps--; // decrement step because we are about to take a step
			for(int i = 0; i < currentProcesses.size(); i++) {
				Process p = currentProcesses.get(i);
				if (release(p.getProcName()) > 0) { // checks to see if the process is ready to be finished
					ui.Println("Succesfully deallocated " + p.getProcName(), Color.green); // prints finished process to ui
					finishedProcesses.add(p);
					currentProcesses.remove(i);
				}
			}
			
			// allocate partitions
			for (Process p : procClone) {
				String process = p.getProcName();
				int Size = p.getProcSize();
				int Time = p.getProcTime();
				switch (memAlgo) {
				case 0: // best fit
					if (best_fit(process, Size, Time) > 0) { // if > 0 could allocate, else could not allocate
						ui.Println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process, Color.green);
						proc.remove(p); // remove proc to signify that it has been entered into memory
						currentProcesses.add(p);
					} else {
						ui.Println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process, Color.RED);
					}
					break;
				case 1: // worst fit
					if (worst_fit(process, Size, Time) > 0) { // if > 0 could allocate, else could not allocate
						ui.Println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process, Color.green);
						proc.remove(p); // remove proc to signify that it has been entered into memory
						currentProcesses.add(p);
					} else {
						ui.Println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process, Color.RED);
					}
					break
					;
				case 2: // next fit
					if (next_fit(process, Size, Time) > 0) { // if > 0 could allocate, else could not allocate
						ui.Println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process, Color.green);
						proc.remove(p); // remove proc to signify that it has been entered into memory
						currentProcesses.add(p);
					} else {
						ui.Println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process, Color.RED);
					}
					break;
				case 3: // first fit
						if (first_fit(process, Size, Time) > 0) { // if > 0 could allocate, else could not allocate
							ui.Println("Succesfully allocated " + Size + " KB and " + Time + " ms to " + process, Color.green);
							proc.remove(p); // remove proc to signify that it has been entered into memory
							currentProcesses.add(p);
						} else {
							ui.Println("Couldn't allocate " + Size + " KB and " + Time + " ms to " + process, Color.RED);
						}
						break;
						default:
							ui.Println("Invalid input", Color.RED);
							System.exit(-1);
				}
			}
			if(!isCompact) merge_adj_holes(); // merge free partitions that are touching
			print_status(); // print status of allocator algorithm
			print_stats(); // print allocator algorithm statistics
			procClone = new ArrayList<>(proc); // copy proc to proc clone
			decrementTime(); // decrement time for all current processes
			ui.mv.repaint(); // repaint the memory paintcomponent on the interface
			try {
				Thread.sleep(1000); // delay for 1000 seconds to manage speed of output
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		canStep = true;
		if(ui.isResetRequested) { // If resetting is requested by the ui, we must free the thread that is waiting for this thread to be terminated
			synchronized(ui.ResetLock) {
				ui.ResetLock.notify();
			}
			synchronized(resetLock) {
				try {
					resetLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(ui.helpRequested) { // if help is requested by the ui, we must free the thread that is waiting for this thread to be terminated
			synchronized(ui.HelpLock) {
				ui.HelpLock.notify();
			}
		}
		if(proc.size() != 0 || currentProcesses.size() != 0) // If the allocation algorithm is not finished, create a new thread for the allocation algorithm step.
			ui.createNewUserInterfaceThread();
	}
	
	public static void main(String args[]) {
		ui = new UserInterface(); // open user interface
	}
}