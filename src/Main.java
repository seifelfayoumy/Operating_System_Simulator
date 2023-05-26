import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        OS os = new OS(2, 40);
        for (int i = 0; i < 100; i++) {
            String program = null;
            if (i == 0) {
                program = "programs/Program_1.txt";
            }
            if (i == 1) {
                program = "programs/Program_2.txt";
            }
            if (i == 4) {
                program = "programs/Program_3.txt";
            }
            System.out.println("Clock Cycle " + i);
            os.clock(program);
        }
    }
}