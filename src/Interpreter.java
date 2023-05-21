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

    public static void execute(String instruction, SystemCalls systemCalls) {
        String[] words = instruction.split(" ");
        switch (words[0]) {
            case "print":
                systemCalls.printData(systemCalls.readFromMemory(words[1]));
                break;
            case "assign":
                Object value = null;
                boolean externalData = false;
                String str = "";
                if (words[2].equals("input")) {
                    externalData = true;
                    str = systemCalls.getTextInput();
                } else if (words[2].equals("readFile")) {
                    externalData = true;
                    str = systemCalls.readFile(words[1]);
                }
                if (externalData) {
                    try {
                        value = Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        value = str;
                    }
                }

                if (externalData) {
                    systemCalls.writeToMemory(words[1], value);
                } else {
                    systemCalls.writeToMemory(words[1], words[2]);
                }


                break;
            case "writeFile":
                systemCalls.writeFile(words[1], words[2]);
            case "printFromTo":
                int x = Integer.parseInt(words[1]);
                int y = Integer.parseInt(words[2]);
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
