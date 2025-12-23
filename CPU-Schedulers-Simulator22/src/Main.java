
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   CPU Schedulers Simulator");
        System.out.println("========================================");
        System.out.println();

        System.out.println("Running All Unit Tests...\n");

        try {
            System.out.println("=== AG Scheduler Tests ===");
            AGUnitTests.main(new String[]{});
        } catch (Exception e) {
            System.err.println("ERROR running AG Tests: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("\n=== Other Schedulers Tests ===");
            OtherSchedulersUnitTests.main(new String[]{});
        } catch (Exception e) {
            System.err.println("ERROR running Other Schedulers Tests: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("   All Tests Completed");
        System.out.println("========================================");
    }
}