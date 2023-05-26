import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;


public class Interpreter {
    private static Map<Integer, Boolean> halfCycleCompleted = new HashMap<>();
    private static Map<Integer, Object> value = new HashMap<>();

    public static List<String> readProgram(String address) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(address));
        } catch (IOException e) {
            System.out.println("Error reading file at " + address);
            e.printStackTrace();
            return new ArrayList<>();
        }

        // Convert the List<String> to String[]
        return lines;
    }

    public static void execute(String instruction, SystemCalls systemCalls, int processId) {
        String[] words = instruction.split(" ");
        switch (words[0]) {
            case "print":
                systemCalls.printData(systemCalls.readFromMemory(words[1]));
                break;
            case "assign":
                if (words[2].equals("input")) {
                    if (halfCycleCompleted.getOrDefault(processId, false)) {
                        systemCalls.writeToMemory(words[1], value.remove(processId));
                        halfCycleCompleted.put(processId, false);

                    } else {
                        value.put(processId, systemCalls.getTextInput());
                        halfCycleCompleted.put(processId, true);

                    }
                } else if (words[2].equals("readFile")) {
                    if (halfCycleCompleted.getOrDefault(processId, false)) {
                        systemCalls.writeToMemory(words[1], value.remove(processId));
                        halfCycleCompleted.put(processId, false);

                    } else {
                        value.put(processId, systemCalls.readFile((String) systemCalls.readFromMemory(words[3])));
                        halfCycleCompleted.put(processId, true);

                    }
                } else {
                    systemCalls.writeToMemory(words[1], words[2]);
                }
                break;
            case "writeFile":
                systemCalls.writeFile((String) systemCalls.readFromMemory(words[1]), (String) systemCalls.readFromMemory(words[2]));
            case "printFromTo":
                int x = Integer.parseInt((String) systemCalls.readFromMemory(words[1]));
                int y = Integer.parseInt((String) systemCalls.readFromMemory(words[2]));
                for (int i = x; i < y + 1; i++) {
                    systemCalls.printData(i);
                }
            case "semWait":
                if (words[1].equals("userInput")) {
                    systemCalls.semWaitUserInput();
                } else if (words[1].equals("userOutput")) {
                    systemCalls.semWaitUserOutput();
                } else if (words[1].equals("file")) {
                    systemCalls.semWaitFile();
                }
                break;
            case "semSignal":
                if (words[1].equals("userInput")) {
                    systemCalls.semSignalUserInput();
                } else if (words[1].equals("userOutput")) {
                    systemCalls.semSignalUserOutput();
                } else if (words[1].equals("file")) {
                    systemCalls.semSignalFile();
                }
                break;
        }
    }


}
