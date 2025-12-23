import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AGUnitTests {

    public static void main(String[] args) {

        String[] testFiles = {
            "AG_test1.json",
            "AG_test2.json", 
            "AG_test3.json", 
            "AG_test4.json",
            "AG_test5.json", 
            "AG_test6.json"

        };
        
        System.out.println("=== Starting AG Scheduler Verification ===");
        
        for (String file : testFiles) {
            runTest(file);
        }
    }

    public static void runTest(String fileName) {
        try {
            System.out.print("Running " + fileName + "... ");
            
            // Read and Parse JSON Input
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject json = new JSONObject(content);
            JSONObject input = json.getJSONObject("input");
            JSONArray procArr = input.getJSONArray("processes");

            // Build Process List
            List<Process> processList = new ArrayList<>();
            for (int i = 0; i < procArr.length(); i++) {
                JSONObject p = procArr.getJSONObject(i);
                processList.add(new Process(
                    p.getString("name"),
                    p.getInt("arrival"),
                    p.getInt("burst"),
                    p.getInt("priority"),
                    p.getInt("quantum")
                ));
            }
            AGScheduler scheduler = new AGScheduler(processList);
            Map<String, Object> results = scheduler.run();

            JSONObject expected = json.getJSONObject("expectedOutput");
            boolean passed = true;

            List<String> actualOrder = compressOrder((List<String>) results.get("executionOrder"));
            JSONArray expectedOrder = expected.getJSONArray("executionOrder");
            
            if (!matchList(actualOrder, expectedOrder)) {
                System.out.println("\n  [X] Order FAILED");
                System.out.println("      Expected: " + expectedOrder);
                System.out.println("      Got:      " + actualOrder);
                passed = false;
            }

            JSONArray expectedProcs = expected.getJSONArray("processResults");
            List<Process> finalProcs = (List<Process>) results.get("processes");
            Map<String, List<Integer>> history = (Map<String, List<Integer>>) results.get("quantumHistory");

            for (int i = 0; i < expectedProcs.length(); i++) {
                JSONObject exp = expectedProcs.getJSONObject(i);
                String name = exp.getString("name");
                Process act = finalProcs.stream().filter(p -> p.name.equals(name)).findFirst().orElse(null);

                if (act == null) {
                    System.out.println("\n  [X] Process " + name + " missing in output");
                    passed = false;
                    continue;
                }

                // Verify Waiting Time
                if (act.waitingTime != exp.getInt("waitingTime")) {
                    System.out.println("\n  [X] " + name + " Waiting Time Failed (Exp: " + exp.getInt("waitingTime") + ", Got: " + act.waitingTime + ")");
                    passed = false;
                }
                
                // Verify Quantum History (The unique requirement for AG)
                JSONArray expHist = exp.getJSONArray("quantumHistory");
                List<Integer> actHist = history.get(name);
                
                if (!matchHistory(actHist, expHist)) {
                    System.out.println("\n  [X] " + name + " History Failed (Exp: " + expHist + ", Got: " + actHist + ")");
                    passed = false;
                }
            }

            if (passed) System.out.println("PASSED");
            else System.out.println("FAILED");

        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }
    }

    // Verification Helpers
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

    // Compares List<String> with JSONArray
    private static boolean matchList(List<String> act, JSONArray exp) {
        if (act.size() != exp.length()) return false;
        for (int i = 0; i < act.size(); i++) {
            if (!act.get(i).equals(exp.getString(i))) return false;
        }
        return true;
    }
    
    // Compares List<Integer> with JSONArray
    private static boolean matchHistory(List<Integer> act, JSONArray exp) {
        if (act.size() != exp.length()) return false;
        for (int i = 0; i < act.size(); i++) {
            if (act.get(i) != exp.getInt(i)) return false;
        }
        return true;
    }
}