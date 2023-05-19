import java.util.ArrayList;
import java.util.Queue;

public class OS {
    Memory memory;
    int memoryPointer;
    int processIdCounter;
    int roundRobinTime;
    PCB runningProcess;
    static Mutex userInput = new Mutex();
    static Mutex userOutput = new Mutex();
    static Mutex file = new Mutex();


    public OS() {
        this.memory = new Memory(40);
        this.processIdCounter = 0;
    }


    public void addProcess(String[] programLines) {
        int processLength = programLines.length + 3;
        PCB pcb = new PCB(this.processIdCounter, "ready", 0, new MemoryBoundary(0, processLength - 1));
        this.memory.addPCB(pcb, 0);
        this.memory.allocate(new MemoryWord("var_1", null), 12);
        this.memory.allocate(new MemoryWord("var_2", null), 13);
        this.memory.allocate(new MemoryWord("var_3", null), 14);
        for (int i = 15; i < programLines.length + 15; i++) {
            this.memory.allocate(new MemoryWord("line_" + i, programLines[i]), i);
        }

    }

    //USE MEMORY INSTEAD TO STORE QUEUES AND PCBS THEN USER INSTRUCTIONS AND VARIABLES
    public int getNextInstruction() {
        return -1;
    }


}
