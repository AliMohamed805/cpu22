import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class OtherSchedulersUnitTests {

    public static void main(String[] args) {
        String baseDir = "d:\\CPU-Schedulers-Simulator\\":
        String[] testFiles = {
            baseDir + "test_cases_v3\\Other_Schedulers\\test_1.json",
            baseDir + "test_cases_v3\\Other_Schedulers\\test_2.json", 
            baseDir + "test_cases_v3\\Other_Schedulers\\test_3.json", 
            baseDir + "test_cases_v3\\Other_Schedulers\\test_4.json",
            baseDir + "test_cases_v3\\Other_Schedulers\\test_5.json", 
            baseDir + "test_cases_v3\\Other_Schedulers\\test_6.json"
        };
        
        System.out.println("=== Starting Schedulers Verification ===\n");
        
        int totalTests = 0;
        int passedTests = 0;
        
        for (String file : testFiles) {
            if (!Files.exists(Paths.get(file))) {
                System.out.println("WARNING: Test file not found: " + file);
                continue;
            }
            int[] results = runTest(file);
            totalTests += results[0];
            passedTests += results[1];
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("Total Tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + (totalTests - passedTests));
    }

    public static int[] runTest(String fileName) {
        int totalTests = 0;
        int passedTests = 0;
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject json = new JSONObject(content);
            String testName = json.getString("name");
            
            System.out.println("Running: " + testName);
            
            // Parse input
            JSONObject input = json.getJSONObject("input");
            int contextSwitch = input.getInt("contextSwitch");
            int rrQuantum = input.getInt("rrQuantum");
            int agingInterval = input.getInt("agingInterval");
            JSONArray procArr = input.getJSONArray("processes");
            
            JSONObject expectedOutput = json.getJSONObject("expectedOutput");
            
            // Test SJF
            totalTests++;
            if (testSJF(procArr, contextSwitch, expectedOutput.getJSONObject("SJF"))) {
                System.out.println("  ✓ SJF PASSED");
                passedTests++;
            } else {
                System.out.println("  ✗ SJF FAILED");
            }
            
            // Test RR
            totalTests++;
            if (testRR(procArr, rrQuantum, contextSwitch, expectedOutput.getJSONObject("RR"))) {
                System.out.println("  ✓ RR PASSED");
                passedTests++;
            } else {
                System.out.println("  ✗ RR FAILED");
            }
            
            // Test Priority
            totalTests++;
            if (testPriority(procArr, contextSwitch, agingInterval, expectedOutput.getJSONObject("Priority"))) {
                System.out.println("  ✓ Priority PASSED");
                passedTests++;
            } else {
                System.out.println("  ✗ Priority FAILED");
            }
            
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new int[]{totalTests, passedTests};
    }

    private static boolean testSJF(JSONArray procArr, int contextSwitch, JSONObject expected) {
        try {
            List<Process> processes = buildProcessList(procArr, 0);
            
            RoundRobinV3JsonOutput.Result result = ShortestJobFirstV3JsonOutput.simulateSJF(processes, contextSwitch);
            
            // Compress execution order
            List<String> actualOrder = compressOrder(result.executionOrder);
            JSONArray expectedOrder = expected.getJSONArray("executionOrder");
            
            if (!matchList(actualOrder, expectedOrder)) {
                System.out.println("    [X] SJF Order Failed");
                System.out.println("        Expected: " + expectedOrder);
                System.out.println("        Got:      " + actualOrder);
                return false;
            }
            
            // Verify process results
            JSONArray expectedProcs = expected.getJSONArray("processResults");
            for (int i = 0; i < expectedProcs.length(); i++) {
                JSONObject exp = expectedProcs.getJSONObject(i);
                String name = exp.getString("name");
                Process act = processes.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
                
                if (act == null) {
                    System.out.println("    [X] SJF Process " + name + " missing");
                    return false;
                }
                
                if (act.waitingTime != exp.getInt("waitingTime")) {
                    System.out.println("    [X] SJF " + name + " WT Failed (Exp: " + exp.getInt("waitingTime") + ", Got: " + act.waitingTime + ")");
                    return false;
                }
                
                int expectedTAT = exp.getInt("turnaroundTime");
                int actualTAT = act.completionTime - act.arrivalTime;
                if (actualTAT != expectedTAT) {
                    System.out.println("    [X] SJF " + name + " TAT Failed (Exp: " + expectedTAT + ", Got: " + actualTAT + ")");
                    return false;
                }
            }
            
            // Verify averages
            double expectedAvgWT = expected.getDouble("averageWaitingTime");
            if (Math.abs(result.avgWT - expectedAvgWT) > 0.01) {
                System.out.println("    [X] SJF Avg WT Failed (Exp: " + expectedAvgWT + ", Got: " + result.avgWT + ")");
                return false;
            }
            
            double expectedAvgTAT = expected.getDouble("averageTurnaroundTime");
            if (Math.abs(result.avgTAT - expectedAvgTAT) > 0.01) {
                System.out.println("    [X] SJF Avg TAT Failed (Exp: " + expectedAvgTAT + ", Got: " + result.avgTAT + ")");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("    [X] SJF Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testRR(JSONArray procArr, int quantum, int contextSwitch, JSONObject expected) {
        try {
            List<Process> processes = buildProcessList(procArr, quantum);
            
            RoundRobinV3JsonOutput.Result result = RoundRobinV3JsonOutput.simulateRR(processes, quantum, contextSwitch);
            
            // Compress execution order
            List<String> actualOrder = compressOrder(result.executionOrder);
            JSONArray expectedOrder = expected.getJSONArray("executionOrder");
            
            if (!matchList(actualOrder, expectedOrder)) {
                System.out.println("    [X] RR Order Failed");
                System.out.println("        Expected: " + expectedOrder);
                System.out.println("        Got:      " + actualOrder);
                return false;
            }
            
            // Verify process results
            JSONArray expectedProcs = expected.getJSONArray("processResults");
            for (int i = 0; i < expectedProcs.length(); i++) {
                JSONObject exp = expectedProcs.getJSONObject(i);
                String name = exp.getString("name");
                Process act = processes.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
                
                if (act == null) {
                    System.out.println("    [X] RR Process " + name + " missing");
                    return false;
                }
                
                if (act.waitingTime != exp.getInt("waitingTime")) {
                    System.out.println("    [X] RR " + name + " WT Failed (Exp: " + exp.getInt("waitingTime") + ", Got: " + act.waitingTime + ")");
                    return false;
                }
                
                int expectedTAT = exp.getInt("turnaroundTime");
                int actualTAT = act.completionTime - act.arrivalTime;
                if (actualTAT != expectedTAT) {
                    System.out.println("    [X] RR " + name + " TAT Failed (Exp: " + expectedTAT + ", Got: " + actualTAT + ")");
                    return false;
                }
            }
            
            // Verify averages
            double expectedAvgWT = expected.getDouble("averageWaitingTime");
            if (Math.abs(result.avgWT - expectedAvgWT) > 0.01) {
                System.out.println("    [X] RR Avg WT Failed (Exp: " + expectedAvgWT + ", Got: " + result.avgWT + ")");
                return false;
            }
            
            double expectedAvgTAT = expected.getDouble("averageTurnaroundTime");
            if (Math.abs(result.avgTAT - expectedAvgTAT) > 0.01) {
                System.out.println("    [X] RR Avg TAT Failed (Exp: " + expectedAvgTAT + ", Got: " + result.avgTAT + ")");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("    [X] RR Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testPriority(JSONArray procArr, int contextSwitch, int agingInterval, JSONObject expected) {
        try {
            List<Process> processes = buildProcessList(procArr, 0);
            
            Preemptive_Priority scheduler = new Preemptive_Priority(processes, contextSwitch, agingInterval);
            scheduler.startSimulation();
            
            // Compress execution order
            List<String> actualOrder = compressOrder(scheduler.getExecutionOrder());
            JSONArray expectedOrder = expected.getJSONArray("executionOrder");
            
            if (!matchList(actualOrder, expectedOrder)) {
                System.out.println("    [X] Priority Order Failed");
                System.out.println("        Expected: " + expectedOrder);
                System.out.println("        Got:      " + actualOrder);
                return false;
            }
            
            // Verify process results
            JSONArray expectedProcs = expected.getJSONArray("processResults");
            for (int i = 0; i < expectedProcs.length(); i++) {
                JSONObject exp = expectedProcs.getJSONObject(i);
                String name = exp.getString("name");
                Process act = processes.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);
                
                if (act == null) {
                    System.out.println("    [X] Priority Process " + name + " missing");
                    return false;
                }
                
                if (act.waitingTime != exp.getInt("waitingTime")) {
                    System.out.println("    [X] Priority " + name + " WT Failed (Exp: " + exp.getInt("waitingTime") + ", Got: " + act.waitingTime + ")");
                    return false;
                }
                
                int expectedTAT = exp.getInt("turnaroundTime");
                int actualTAT = act.completionTime - act.arrivalTime;
                if (actualTAT != expectedTAT) {
                    System.out.println("    [X] Priority " + name + " TAT Failed (Exp: " + expectedTAT + ", Got: " + actualTAT + ")");
                    return false;
                }
            }
            
            // Calculate and verify averages
            double totalWT = 0, totalTAT = 0;
            for (Process p : processes) {
                totalWT += p.waitingTime;
                totalTAT += (p.completionTime - p.arrivalTime);
            }
            double avgWT = totalWT / processes.size();
            double avgTAT = totalTAT / processes.size();
            
            double expectedAvgWT = expected.getDouble("averageWaitingTime");
            if (Math.abs(avgWT - expectedAvgWT) > 0.01) {
                System.out.println("    [X] Priority Avg WT Failed (Exp: " + expectedAvgWT + ", Got: " + avgWT + ")");
                return false;
            }
            
            double expectedAvgTAT = expected.getDouble("averageTurnaroundTime");
            if (Math.abs(avgTAT - expectedAvgTAT) > 0.01) {
                System.out.println("    [X] Priority Avg TAT Failed (Exp: " + expectedAvgTAT + ", Got: " + avgTAT + ")");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("    [X] Priority Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static List<Process> buildProcessList(JSONArray procArr, int quantum) {
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < procArr.length(); i++) {
            JSONObject p = procArr.getJSONObject(i);
            list.add(new Process(
                p.getString("name"),
                p.getInt("arrival"),
                p.getInt("burst"),
                p.getInt("priority"),
                quantum
            ));
        }
        return list;
    }

    private static List<String> compressOrder(List<String> raw) {
        List<String> res = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return res;
        
        res.add(raw.get(0));
        for (int i = 1; i < raw.size(); i++) {
            if (!raw.get(i).equals(raw.get(i - 1))) {
                res.add(raw.get(i));
            }
        }
        return res;
    }

    private static boolean matchList(List<String> act, JSONArray exp) {
        if (act.size() != exp.length()) return false;
        for (int i = 0; i < act.size(); i++) {
            if (!act.get(i).equals(exp.getString(i))) return false;
        }
        return true;
    }
}
