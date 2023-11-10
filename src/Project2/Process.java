package Project2;

public class Process {
	
	//this is only used for processes not allocated yet
	private int procSize, procTime;
	public Process(int procSize, int procTime) {
		this.procSize = procSize;
		this.procTime = procTime;
	}
	@Override
	public String toString() {
		return "Process [procSize=" + procSize + " , procTime=" + procTime + "]";
	}

}
