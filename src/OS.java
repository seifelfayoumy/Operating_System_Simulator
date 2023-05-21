import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class OS {
    Memory memory;
    int kernelMemoryPointer;
    int userMemoryPointer;
    int processIdCounter;
    Mutex userInput;
    Mutex userOutput;
    Mutex file;

    Queue<Process> blockedQueue;
    Queue<Process> readyQueue;
    Process runningProcess;
    int timeSlice;


    public OS(int timeSlice, int memorySize) {
        this.memory = new Memory(memorySize);
        this.processIdCounter = 0;
        this.timeSlice = timeSlice;
        this.kernelMemoryPointer = 0;
        this.userMemoryPointer = 12;
        this.userInput = new Mutex();
        this.userOutput = new Mutex();
        this.file = new Mutex();
    }

    public void clock(String programAddress) {
        if (programAddress != null && !programAddress.equals("")) {
            this.addProcess(Interpreter.readProgram(programAddress));
        }
        PCB pcb = this.memory.getPcb(this.runningProcess.processID);
        int nextInstruction = this.getNextInstruction();
        if (nextInstruction != -1) {
            String instruction = (String) this.memory.read(nextInstruction).data;
            Interpreter.execute(instruction, new SystemCalls(pcb, this));
        }
    }


    public void addProcess(List<String> programLines) {
        int processLength = programLines.size() + 3;
        PCB pcb = new PCB(this.processIdCounter, "ready", 3, new MemoryBoundary(0, processLength - 1));
        this.memory.addPCB(pcb, this.kernelMemoryPointer);
        this.kernelMemoryPointer += 4;
        this.memory.allocate(new MemoryWord("var_1", null), this.userMemoryPointer);
        this.userMemoryPointer += 1;
        this.memory.allocate(new MemoryWord("var_2", null), this.userMemoryPointer);
        this.userMemoryPointer += 1;
        this.memory.allocate(new MemoryWord("var_3", null), this.userMemoryPointer);
        this.userMemoryPointer += 1;
        for (int i = this.userMemoryPointer; i < programLines.size() + this.userMemoryPointer; i++) {
            this.memory.allocate(new MemoryWord("line_" + i, programLines.get(i)), i);
            this.userMemoryPointer += 1;
        }
        this.readyQueue.add(new Process(this.processIdCounter, this.timeSlice));
        this.processIdCounter += 1;
    }

    public int getNextInstruction() {
        if (runningProcess.remainingTime <= 0) {
            this.readyQueue.add(new Process(runningProcess.processID, this.timeSlice));
            this.runningProcess = this.readyQueue.remove();
        } else {

        }
        if (this.runningProcess != null) {
            PCB pcb = this.memory.getPcb(this.runningProcess.processID);
            int next = pcb.memoryBoundaries.start + pcb.programCounter;
            pcb.programCounter += 1;
            pcb.processState = "running";
            this.memory.addPCB(pcb, -1);
            runningProcess.remainingTime -= 1;
            return next;
        }
        return -1;

    }

    public void semWaitUserInput(int processID){
        if(this.userInput.semWait(processID) == false){
            this.blockedQueue.add(new Process(processID, this.timeSlice));
        }
    }
    public void semWaitUserOutput(int processID){
        if(this.userOutput.semWait(processID) == false){
            this.blockedQueue.add(new Process(processID, this.timeSlice));
        }
    }
    public void semWaitFile(int processID){
        if(this.file.semWait(processID) == false){
            this.blockedQueue.add(new Process(processID, this.timeSlice));
        }
    }
    public void semSignalUserInput(int processID){
       this.userInput.semSignal(processID);
       int waitingProcess = this.userInput.waiting.remove();
       this.readyQueue.add(new Process(waitingProcess,this.timeSlice));
    }
    public void semSignalUserOutput(int processID){
        this.userOutput.semSignal(processID);
        int waitingProcess = this.userOutput.waiting.remove();
        this.readyQueue.add(new Process(waitingProcess,this.timeSlice));
    }
    public void semSignalFile(int processID){
        this.file.semSignal(processID);
        int waitingProcess = this.file.waiting.remove();
        this.readyQueue.add(new Process(waitingProcess,this.timeSlice));
    }


}
