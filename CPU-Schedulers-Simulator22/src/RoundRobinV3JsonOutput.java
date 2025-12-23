import java.util.*;
import java.io.*;

public class RoundRobinV3JsonOutput {


    static class SchedulerInput {
        List<Process> processes;
        int quantum;
        int contextSwitch;
    }

    static class Result {
        List<String> executionOrder = new ArrayList<>();
        List<Process> processes;
        double avgWT;
        double avgTAT;
    }

    static Result simulateRR(List<Process> processes, int quantum, int contextSwitch) {

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> ready = new LinkedList<>();
        Result res = new Result();
        res.processes = processes;

        int time = 0, index = 0, finished = 0;
        int n = processes.size();

        while (finished < n) {

            while (index < n && processes.get(index).arrivalTime <= time) {
                ready.add(processes.get(index++));
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            Process current = ready.poll();
            res.executionOrder.add(current.name);

            //Fix by using current.remainingTime
            int slice = Math.min(quantum, current.remainingTime);

            for (int t = 0; t < slice; t++) {
                current.remainingTime--;
                time++;

                // Update waiting time for others
                for (Process p : ready)
                    p.waitingTime++;

                while (index < n && processes.get(index).arrivalTime <= time) {
                    ready.add(processes.get(index++));
                }

                if (current.remainingTime == 0)
                    break;
            }

            if (current.remainingTime == 0) {
                current.completionTime = time;
                finished++;
            } else {
                ready.add(current);
            }

            if (!ready.isEmpty()) {
                for (int cs = 0; cs < contextSwitch; cs++) {
                    time++;
                    for (Process p : ready)
                        p.waitingTime++;
                }
            }
        }

        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            int tat = p.completionTime - p.arrivalTime;
            totalWT += p.waitingTime;
            totalTAT += tat;
        }

        res.avgWT = totalWT / n;
        res.avgTAT = totalTAT / n;
        return res;
    }
}