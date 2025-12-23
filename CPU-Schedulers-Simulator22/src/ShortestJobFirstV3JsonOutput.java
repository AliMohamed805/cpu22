import java.util.*;
import java.io.*;

public class ShortestJobFirstV3JsonOutput {


    static class SchedulerInput {
        List<Process> processes;
        int contextSwitch;
    }

    static class Result {
        List<String> executionOrder = new ArrayList<>();
        List<Process> processes;
        double avgWT, avgTAT;
    }


    static Result simulateSJF(List<Process> processes, int cs) {

        Result res = new Result();
        res.processes = processes;

        int time = 0;
        int finished = 0;
        int n = processes.size();
        Process current = null;

        while (finished < n) {

            List<Process> ready = new ArrayList<>();
            for (Process p : processes) {
                // Fixed: using p.remainingTime instead of p.remaining
                if (p.arrivalTime <= time && p.remainingTime > 0)
                    ready.add(p);
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            //Fix by using p.remainingTime
            ready.sort(Comparator.comparingInt(p -> p.remainingTime));
            Process next = ready.get(0);

            if (current != next) {
                if (current != null)
                    time += cs; 
                current = next;
                res.executionOrder.add(current.name);
            }

            current.remainingTime--;
            time++;

            if (current.remainingTime == 0) {
                current.completionTime = time;
                finished++;

                if (finished < n) {
                    time += cs; 
                }

                current = null;
            }
        }

        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            int tat = p.completionTime - p.arrivalTime;
            //Fix by using p.burstTime
            p.waitingTime = tat - p.burstTime;
            totalWT += p.waitingTime;
            totalTAT += tat;
        }

        res.avgWT = totalWT / n;
        res.avgTAT = totalTAT / n;
        return res;
    }
}