public class Process {
    public String name;
    public int arrivalTime;
    public int burstTime;
    public int priority;

    public int quantum; 

    public int remainingTime; 
    public int waitingTime;
    public int turnaroundTime;
    public int completionTime;

    public int tempWaitTime; 

    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
        
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.tempWaitTime = 0;
    }
}