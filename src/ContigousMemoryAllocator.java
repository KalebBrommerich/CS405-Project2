package Lab6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ContigousMemoryAllocator {
	private int size;    // maximum memory size in bytes (B)
	private Map<String, Partition> allocMap;   // map process to partition
	private List<Partition> partList;    // list of memory partitions
	// constructor
	public ContigousMemoryAllocator(int size) {
		this.size = size;
		this.allocMap = new HashMap<>();
		this.partList = new ArrayList<>();
		this.partList.add(new Partition(0, size)); //add the first hole, which is the whole memory at start up
	}
      
	// prints the list of available commands
	public void print_help_message() {
		//TODO: add code below
		System.out.println("RQ <process> <size> request a memory partition w/ <size> to <process>");
		System.out.println("RL <process> release memory partition to <process>");
		System.out.println("STAT show all memory partitions");
		System.out.println("EXIT exit simulator");
		System.out.println("H show help (available commands)");
	}
      
	// prints the allocation map (free + allocated) in ascending order of base addresses
	public void print_status() {
		//TODO: add code below
		order_partitions();
		System.out.printf("Paritions [Allocated=%d KB, Free=%d KB",allocated_memory(),free_memory());
		for(Partition part: partList) {
			System.out.printf("Address [%d:%d] %s (%d KB) \n",
					part.getBase(),part.getBase()+ part.getLength()-1,
					part.isbFree() ? "Free" : part.getProcess(), part.getLength());
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
	public int first_fit(String process, int size) {
		//TODO: add code below
		if(allocMap.containsKey(process)) return -1;//process allocated a partition already
		int index =0,alloc = -1;
		while(index<partList.size()) {
			Partition part = partList.get(index);
			if(part.isbFree() && part.getLength() >= size) {//found a good partition
				Partition allocPart = new Partition(part.getBase(),size);
				allocPart.setbFree(false);
				allocPart.setProcess(process);
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
					}
					j++;
				}
			}
			i++;
		}
	}
	public static void main(String args[]) {
		System.out.println("Contiguos allocater thing");
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter max physical mem size (KB)");
		int size = sc.nextInt();
		if (size<0) {
			System.err.println("Invalid mem size");
			System.exit(-1);
		}
		ContigousMemoryAllocator allocator = new ContigousMemoryAllocator(size);
		while(true) {
			System.out.print("mmu>");
			String command = sc.nextLine();
			String arr[] = command.split(" ");
			if(arr[0].toLowerCase().equals("h")) {
				allocator.print_help_message();
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
				if(allocator.first_fit(process, rqSize)>0) {
					System.out.println("Succesfully allocated " + rqSize + " to " + process);
				}
				else {
					System.err.println("Couldn't allocated " + rqSize + " to " + process);
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