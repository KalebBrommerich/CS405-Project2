package Project2;

public class Process {
	
	//this is only used for processes not allocated yet
	private int procSize, procTime;
	private String procName;
	public Process(String procName, int procSize, int procTime) {
		this.procName = procName;
		this.procSize = procSize;
		this.procTime = procTime;
	}
	public int getProcSize() {
		return procSize;
	}
	public void setProcSize(int procSize) {
		this.procSize = procSize;
	}
	public int getProcTime() {
		return procTime;
	}
	public void setProcTime(int procTime) {
		this.procTime = procTime;
	}
	public String getProcName() {
		return procName;
	}
	public void setProcName(String procName) {
		this.procName = procName;
	}
	@Override
	public String toString() {
		return "Process " + procName + " [procSize=" + procSize + " , procTime=" + procTime + "]";
	}

}
