import java.util.*;

public class AGScheduler {
    private List<Process> processes;
    private List<String> executionOrder;
    private Map<String, List<Integer>> quantumHistory;
    
    public AGScheduler(List<Process> inputProcesses) {
        this.processes = new ArrayList<>();
        this.executionOrder = new ArrayList<>();
        this.quantumHistory = new HashMap<>();
        
        // Deep copy processes
        for (Process p : inputProcesses) {
            Process newP = new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum);
            this.processes.add(newP);
            
            List<Integer> history = new ArrayList<>();
            history.add(newP.quantum);
            quantumHistory.put(newP.name, history);
        }
    }

    public Map<String, Object> run() {
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        Queue<Process> readyQueue = new LinkedList<>();
        Process currentProcess = null;
        int timeSpentInCurrentQuantum = 0;

        // Initial check for time 0
        checkArrivals(readyQueue, currentTime);

        while (completed < n) {
            
            // If CPU is idle, pick from HEAD of queue
            if (currentProcess == null) {
                if (!readyQueue.isEmpty()) {
                    currentProcess = readyQueue.poll();
                    timeSpentInCurrentQuantum = 0;
                } else {
                    currentTime++;
                    checkArrivals(readyQueue, currentTime);
                    continue;
                }
            }

            executionOrder.add(currentProcess.name);

            // Execute for 1 unit
            currentProcess.remainingTime--;
            timeSpentInCurrentQuantum++;
            currentTime++;

            // Check Arrivals currently 
            checkArrivals(readyQueue, currentTime);

            // Check Completion
            if (currentProcess.remainingTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                
                updateQuantum(currentProcess, 0); 
                
                completed++;
                currentProcess = null;
                continue;
            }

            // AG Scheduling
            int q = currentProcess.quantum;
            int limit25 = (int) Math.ceil(q * 0.25);
            int limit50 = limit25 * 2; // Next 25%

            if (timeSpentInCurrentQuantum == limit25) {
                Process bestPriority = getBestPriority(readyQueue);
                // Lower Priority Number = Higher Importance
                if (bestPriority != null && bestPriority.priority < currentProcess.priority) {
                    
                    int remainingQ = q - timeSpentInCurrentQuantum;
                    int addedQ = (int) Math.ceil(remainingQ / 2.0);
                    updateQuantum(currentProcess, q + addedQ);
                    
                    readyQueue.add(currentProcess);
                    readyQueue.remove(bestPriority);
                    currentProcess = bestPriority;
                    timeSpentInCurrentQuantum = 0;
                    continue; 
                }
            }
            
            else if (timeSpentInCurrentQuantum >= limit50) {
                Process bestSJF = getShortestJob(readyQueue);
                if (bestSJF != null && bestSJF.remainingTime < currentProcess.remainingTime) {
                    
                    int remainingQ = q - timeSpentInCurrentQuantum;
                    updateQuantum(currentProcess, q + remainingQ);
                    
                    readyQueue.add(currentProcess);
                    readyQueue.remove(bestSJF);
                    currentProcess = bestSJF;
                    timeSpentInCurrentQuantum = 0;
                    continue; 
                }
            }

            if (timeSpentInCurrentQuantum == q) {
                updateQuantum(currentProcess, q + 2);
                
                readyQueue.add(currentProcess);
                currentProcess = null;
            }
        }

        Map<String, Object> results = new HashMap<>();
        results.put("executionOrder", executionOrder);
        results.put("quantumHistory", quantumHistory);
        results.put("processes", processes);
        return results;
    }

    private void checkArrivals(Queue<Process> readyQueue, int time) {
        for (Process p : processes) {
            if (p.arrivalTime == time) {
                readyQueue.add(p);
            }
        }
    }

    private void updateQuantum(Process p, int newQ) {
        p.quantum = newQ;
        quantumHistory.get(p.name).add(newQ);
    }

    private Process getBestPriority(Queue<Process> queue) {
        Process best = null;
        for (Process p : queue) {
            if (best == null || p.priority < best.priority) best = p;
        }
        return best;
    }

    private Process getShortestJob(Queue<Process> queue) {
        Process best = null;
        for (Process p : queue) {
            if (best == null || p.remainingTime < best.remainingTime) best = p;
        }
        return best;
    }
}