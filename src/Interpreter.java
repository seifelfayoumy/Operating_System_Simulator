import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;


public class Interpreter {

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

    public static void execute(String instruction, Memory memory, PCB pcb) {
        String[] words = instruction.split(" ");
        switch (words[0]) {
            case "print":
                System.out.println(memory.getVariable(words[1]));
                break;
            case "assign":
                Object value = null;
                boolean externalData = false;
                String str = "";
                if (words[2].equals("input")) {
                    externalData = true;
                    Scanner sc = new Scanner(System.in);
                    System.out.print("Please enter a value");
                    str = sc.nextLine();
                } else if (words[2].equals("readFile")) {
                    externalData = true;
                    try {
                        FileReader fileReader = new FileReader(words[1]);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            str = line;
                        }
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (externalData) {
                    try {
                        value = Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        value = str;
                    }
                }
                for (int i = pcb.memoryBoundaries.start; i < pcb.memoryBoundaries.end + 3; i++) {
                    if (memory.isEmpty(i)) {
                        if (externalData) {
                            memory.allocate(new MemoryWord(words[1], value), i);
                        } else {
                            memory.allocate(new MemoryWord(words[1], words[2]), i);
                        }
                        break;
                    }
                }
                break;
            case "writeFile":
                try {
                    FileWriter fileWriter = new FileWriter(words[1]);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(words[2]);
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case "printFromTo":
                int x = Integer.parseInt(words[1]);
                int y = Integer.parseInt(words[2]);
                for (int i = x; i < y + 1; i++) {
                    System.out.println(i);
                }
            case "semWait":
                if (words[1].equals("userInput")) {
                    OS.userInput.semWait(pcb.processID);
                } else if (words[1].equals("userOutput")) {
                    OS.userOutput.semWait(pcb.processID);
                } else if (words[1].equals("file")) {
                    OS.file.semWait(pcb.processID);
                }
                break;
            case "semSignal":
                if (words[1].equals("userInput")) {
                    OS.userInput.semSignal(pcb.processID);
                } else if (words[1].equals("userOutput")) {
                    OS.userOutput.semSignal(pcb.processID);
                } else if (words[1].equals("file")) {
                    OS.file.semSignal(pcb.processID);
                }
                break;

        }
    }


}
