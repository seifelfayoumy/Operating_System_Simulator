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

    Queue<Integer> blockedQueue;
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
        if (this.memory.spaceExists(programLines.size() + 3)) {
            PCB pcb = new PCB(this.processIdCounter, "ready", 3, new MemoryBoundary(0, programLines.size() + 2));
            this.memory.addProcess(pcb, programLines);
            this.readyQueue.add(new Process(this.processIdCounter, this.timeSlice));
            this.processIdCounter += 1;
        } else {
            //swap to disk
        }
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

    public void semWaitUserInput(int processID) {
        if (!this.userInput.semWait(processID)) {
            this.blockedQueue.add(processID);
        }
    }

    public void semWaitUserOutput(int processID) {
        if (!this.userOutput.semWait(processID)) {
            this.blockedQueue.add(processID);
        }
    }

    public void semWaitFile(int processID) {
        if (!this.file.semWait(processID)) {
            this.blockedQueue.add(processID);
        }
    }

    public void semSignalUserInput(int processID) {
        this.userInput.semSignal(processID);
        int waitingProcess = this.userInput.waiting.remove();
        this.blockedQueue.remove(waitingProcess);
        this.readyQueue.add(new Process(waitingProcess, this.timeSlice));
    }

    public void semSignalUserOutput(int processID) {
        this.userOutput.semSignal(processID);
        int waitingProcess = this.userOutput.waiting.remove();
        this.blockedQueue.remove(waitingProcess);
        this.readyQueue.add(new Process(waitingProcess, this.timeSlice));
    }

    public void semSignalFile(int processID) {
        this.file.semSignal(processID);
        int waitingProcess = this.file.waiting.remove();
        this.blockedQueue.remove(waitingProcess);
        this.readyQueue.add(new Process(waitingProcess, this.timeSlice));
    }


}
