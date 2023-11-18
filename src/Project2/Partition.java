package Project2;

public class Partition {
	// the representation of each memory partition
	private int base; // base address
	private int length; // partition size
	private int remainingTime; // remaining time of process
	private boolean bFree; // status: free or allocated
	private String process; // assigned process if allocated

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}

	public boolean isbFree() {
		return bFree;
	}

	public void setbFree(boolean bFree) {
		this.bFree = bFree;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	// constructor method
	public Partition(int base, int length, int remainingTime) {
		this.base = base;
		this.length = length;
		this.remainingTime = remainingTime;
		this.bFree = true; // free by default when creating
		this.process = null; // unallocated to any process
	}

	@Override
	public String toString() {
		return "Partition [base=" + base + ", length=" + length + ", bFree=" + bFree + ", process=" + process + "]";

	}
}
