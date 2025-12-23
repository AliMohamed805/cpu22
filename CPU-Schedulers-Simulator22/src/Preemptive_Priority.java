import java.util.*;

public class Preemptive_Priority {
    private List<Process> processes;
    private int contextSwitch;
    private List<String> executionOrder;
    private int agingThreshold;

    public Preemptive_Priority(List<Process> processes, int contextSwitch, int agingThreshold) {
        this.processes = processes;
        this.contextSwitch = contextSwitch;
        this.executionOrder = new ArrayList<>();
        this.agingThreshold = agingThreshold;
    }

    public void startSimulation() {
        int currentTime = 0;
        int completedProcesses = 0;
        Process lastRunningProcess = null;
        int n = processes.size();

        while (completedProcesses < n) {
            Process current = findHighestPriority(currentTime);

            if (current == null) {
                currentTime++;
                continue;
            }

            if (lastRunningProcess != null && lastRunningProcess != current) {
                currentTime += contextSwitch;
            }

            if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals(current.name)) {
                executionOrder.add(current.name);
            }

            // Waiting time calculation
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0 && p != current) {
                    p.waitingTime++;
                }
            }

            current.remainingTime--;
            currentTime++;

            applyAging(currentTime, current);

            if (current.remainingTime == 0) {
                completedProcesses++;
                updateProcessStats(current, currentTime);
            }

            lastRunningProcess = current;
        }
    }

    private void applyAging(int currentTime, Process current) {
        for (Process p : processes) {
            if (p == current) continue;

            if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                p.tempWaitTime++;

                if (p.tempWaitTime >= agingThreshold && p.priority > 0) {
                    p.priority--;
                    p.tempWaitTime = 0;
                }
            }
        }
    }

    private Process findHighestPriority(int currentTime) {
        Process selectedProcess = null;
        int highestPriority = (int) 1e9;
        for (Process p : this.processes) {
            if(p.arrivalTime <= currentTime && p.remainingTime > 0)  {
                if (p.priority < highestPriority) {
                    selectedProcess = p;
                    highestPriority = p.priority;
                } else if (p.priority == highestPriority) {
                    if (selectedProcess != null && p.arrivalTime < selectedProcess.arrivalTime) {
                        selectedProcess = p;
                    }
                }
            }
        }
        return selectedProcess;
    }

    private void updateProcessStats(Process p, int completionTime) {
        p.completionTime = completionTime;
        p.turnaroundTime = p.completionTime - p.arrivalTime;
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }
}