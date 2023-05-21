import java.io.*;
import java.util.Scanner;

public class SystemCalls {
    PCB pcb;
    Memory memory;
    OS os;

    public SystemCalls(PCB pcb, OS os) {
        this.pcb = pcb;
        this.memory = os.memory;
        this.os = os;
    }

    public String readFile(String address) {
        try {
            FileReader fileReader = new FileReader(address);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                return line;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeFile(String address, String data) {
        try {
            FileWriter fileWriter = new FileWriter(address);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printData(Object data) {
        System.out.println(data);
    }

    public String getTextInput() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter a value");
        return sc.nextLine();
    }

    public void writeToMemory(String variableName, Object value) {
        for (int i = pcb.memoryBoundaries.start; i < pcb.memoryBoundaries.start + 3; i++) {
            if (memory.isEmpty(i)) {
                if (memory.read(i).variable.equals("var_1") || memory.read(i).variable.equals("var_2") || memory.read(i).variable.equals("var_3"))
                    memory.allocate(new MemoryWord(variableName, value), i);
            }
        }
    }

    public Object readFromMemory(String variableName) {
        for (int i = pcb.memoryBoundaries.start; i < pcb.memoryBoundaries.start + 3; i++) {
            if (memory.read(i).variable.equals(variableName)) {
                return memory.read(i).data;
            }
        }
        return null;
    }

    public void semWaitUserInput(){
        os.semWaitUserInput(this.pcb.processID);
    }
    public void semWaitUserOutput(){
        os.semWaitUserOutput(this.pcb.processID);
    }
    public void semWaitFile(){
        os.semWaitFile(this.pcb.processID);
    }
    public void semSignalUserInput(){
        os.semSignalUserInput(this.pcb.processID);
    }
    public void semSignalUserOutput(){
        os.semSignalUserOutput(this.pcb.processID);
    }
    public void semSignalFile(){
        os.semSignalFile(this.pcb.processID);
    }


}
