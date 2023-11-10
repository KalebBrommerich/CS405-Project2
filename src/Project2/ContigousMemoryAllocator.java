package Project2;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ContigousMemoryAllocator {
	private int size;    // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap;   // map process to partition
	private List<Partition> partList;    // list of memory partitions
	// constructor
	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size, -1)); //add the first hole, which is the whole memory at start up
	}
      
	// prints the allocation map (free + allocated) in ascending order of base addresses
	public void print_status() {
		//TODO: add code below
		order_partitions();
		System.out.printf("Paritions [Allocated=%d KB, Free=%d KB \n",allocated_memory(),free_memory());
		for(Partition part: partList) {
			System.out.printf("Address [%d:%d] %s (%d KB) {%d ms}\n",
					part.getBase(),part.getBase()+ part.getLength()-1,
					part.isbFree() ? "Free" : part.getProcess(), part.getLength(),
					part.getRemainingTime());
		}
	}
      
	// get the size of total allocated memory
	private int allocated_memory() {
		//TODO: add code below
		int size = 0;
		for(Partition part : partList) {
			if(!part.isbFree())
				size+= part.getLength();
			}
		return size;
	}
      
	// get the size of total free memory
	private int free_memory() {
		//TODO: add code below
		int size = 0;
		for(Partition part : partList) {
			if(part.isbFree())
				size+= part.getLength();
			}
		return size;
	}
      
	// sort the list of partitions in ascending order of base addresses
	private void order_partitions() {
		//TODO: add code below
		Collections.sort(partList,(o1,o2)-> (o1.getBase() - o2.getBase()));
	}
      
	// implements the first fit memory allocation algorithm
	public int first_fit(String process, int size, int time) {
		//TODO: add code below
		System.out.println("Start First Fit Method: size=" + size);
		if(allocMap.containsKey(process)) return -1;//process allocated a partition already
		int index =0,alloc = -1;
		System.out.println("Start While Loop in FFM");
		while(index<partList.size()) {
			Partition part = partList.get(index);
			//part.getLength is the size of the partitions
			if(part.isbFree() && part.getLength() >= size) {//found a good partition
				Partition allocPart = new Partition(part.getBase(),size, time);
				allocPart.setbFree(false);
				allocPart.setProcess(process);
				allocPart.setRemainingTime(time);
				partList.add(index, allocPart);//insert this partition to list
				allocMap.put(process, allocPart);
				part.setBase(part.getBase()+size);
				part.setLength(part.getLength()-size);
				if (part.getLength()==0) partList.remove(part);
				alloc = size;
				break;
			}
			index++;
		}
		return alloc;
	}
      
	// release the allocated memory of a process
	public int release(String process) {
		//TODO: add code below
		if(!allocMap.containsKey(process))
			return -1;//no partition allocated to process
		int size = -1;
		for(Partition part : partList) {
			if(!part.isbFree() && process.equals(part.getProcess())) {
				part.setbFree(true);
				part.setProcess(null);
				size = part.getLength();
				break;
			}
		}
		if(size < 0 )
			return size;
		merge_holes();
		return size;
	}      
      
	// procedure to merge adjacent holes
	private void merge_holes() {
		//TODO: add code below
		order_partitions();
		int i = 0;
		while(i<partList.size()-1) {
			Partition part = partList.get(i);
			if(part.isbFree()) {
				int endAddr = part.getBase()+part.getLength()-1;
				int j = i+1;
				while(j < partList.size() && partList.get(j).isbFree()) {
					//merge j into i

					int startj = partList.get(j).getBase();
					if(startj== endAddr +1) {
						part.setLength((part.getLength() + partList.get(j).getLength()));
						partList.remove(partList.get(j));
						System.out.println("merge");
						endAddr = part.getBase()+part.getLength()-1;
						continue;
					}
					j++;
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
		ArrayList<Process> temp = new ArrayList<Process>();
		for(int i = 0;i<numProc;i++) {
			//round MS to Seconds
			//temp.add(new Process((int)(Math.random()*procSizeMax),(int)Math.round((Math.random()*maxProcTime) + 500) / 1000 * 1000));
		}
		temp.add(new Process(123, 6000));
		temp.add(new Process(13, 4000));
		temp.add(new Process(210, 8000));
		temp.add(new Process(23, 2000));
		temp.add(new Process(182, 5000));
		temp.add(new Process(93, 5000));
		temp.add(new Process(200,9000));
		temp.add(new Process(102,10000));
		temp.add(new Process(52, 1000));
		temp.add(new Process(34, 7000));
		return temp;
	}
	public static void main(String args[]) {

		/*System.out.println("Contiguos allocater thing");
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter max physical mem size (KB)");
		int size = sc.nextInt();
		if (size<0) {
			System.err.println("Invalid mem size");
			System.exit(-1);
		}
		ContigousMemoryAllocator allocator = new ContigousMemoryAllocator(size);*/
		int MemoryMax = -1, ProcSizeMax = -1, NumProc = -1, MaxProcTime = -1, size = -1;

		boolean fileNotChosen = true;
		while(fileNotChosen)
		{
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file", "txt");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(null);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				Scanner scr;
				try {
					scr = new Scanner(file);
					while(scr.hasNextLine()) {
						String line = scr.nextLine();
						String arr[] = line.split(" ");
						if(arr.length < 3) continue;
						String key = arr[0].toUpperCase();
						switch(key) {
							case "MEMORY_MAX":
								//System.out.println("5");
								if(arr.length < 4) continue;
								MemoryMax = convertToKB(arr);
								size = MemoryMax;
								System.out.println("Memory Max: " + MemoryMax);
								break;
							case "PROC_SIZE_MAX":
								if(arr.length < 4) continue;
								ProcSizeMax = convertToKB(arr);
								System.out.println("Proc_Size_Max: " + ProcSizeMax);
								break;
							case "NUM_PROC":
								NumProc = Integer.parseInt(arr[2]);
								System.out.println("Num Proc: " + NumProc);
								break;
							case "MAX_PROC_TIME":
								if(arr.length < 4) continue;
								MaxProcTime = convertToMS(arr);
								System.out.println("Max Proc Time: " + MaxProcTime);
								break;
							default:
								System.out.println("The key {"+arr[0]+"} in the config file is not supported.");
						}
					}
					if(MemoryMax == -1 || ProcSizeMax == -1 || NumProc == -1 || MaxProcTime == -1) {
						System.out.println("The input file is missing an important parameter.");	
					}
					else {
						fileNotChosen = false;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		ArrayList<Process> proc = generateProcesses(ProcSizeMax, NumProc, MaxProcTime);
		for(Process p: proc) {
			System.out.println(p.toString());
		}
		
  	ContigousMemoryAllocator allocator = new ContigousMemoryAllocator(size);
		Scanner sc = new Scanner(System.in);
		while(true) {
			
			System.out.print("mmu>");
			String command = sc.nextLine();
			String arr[] = command.split(" ");
			
			if(arr[0].toLowerCase().equals("h")) {
				//allocator.print_help_message();
			}
			else if(arr[0].toLowerCase().equals("stat")) {
				allocator.print_status();
			}
			else if(arr[0].toLowerCase().equals("exit")) {
				break;
			}
			else if(arr[0].toLowerCase().equals("rq")) {
				String process = arr[1];
				int rqSize = Integer.parseInt(arr[2]);
				int rqTime = Integer.parseInt(arr[3]);
				if(allocator.first_fit(process, rqSize, rqTime)>0) {
					System.out.println("Succesfully allocated " + rqSize + " KB and " + rqTime + " ms to " + process);
				}
				else {
					System.err.println("Couldn't allocate " + rqSize + " KB and " + rqTime + " ms to " + process);
				}
			}
			else if(arr[0].toLowerCase().equals("rl")){
				String process = arr[1];
				if(allocator.release(process)>0) {
					System.out.println("Succesfully deallocated "+ process);
				}
				else {
					System.err.println("Couldn't deallocate "+ process);
				}
			}
		}
	}


}