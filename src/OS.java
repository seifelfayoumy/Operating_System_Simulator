import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class OS {
    Memory memory;
    int processIdCounter;
    Mutex userInput;
    Mutex userOutput;
    Mutex file;
    Scheduler scheduler;


    public OS(int timeSlice, int memorySize) {
        this.memory = new Memory(memorySize);
        this.processIdCounter = 0;
        this.userInput = new Mutex();
        this.userOutput = new Mutex();
        this.file = new Mutex();
        this.scheduler = new Scheduler(timeSlice, this.memory);
    }

    public void clock(String programAddress) {
        if (programAddress != null && !programAddress.equals("")) {
            this.addProcess(Interpreter.readProgram(programAddress));
        }
        int nextInstruction = this.scheduler.getNextInstruction();
        PCB pcb = this.memory.getPcb(this.scheduler.getRunningProcessID());
        if (nextInstruction != -1) {
            String instruction = (String) this.memory.read(nextInstruction).data;
            Interpreter.execute(instruction, new SystemCalls(pcb, this));
        }
    }


    public void addProcess(List<String> programLines) {
        if (!this.memory.spaceExists(programLines.size() + 3)) {
            MemoryDisk oldData = this.memory.getProcessData();
            MemoryWord.writeMemoryWordsToFile(oldData.data,oldData.processID);
        }
        PCB pcb = new PCB(this.processIdCounter, "ready", 3, new MemoryBoundary(0, programLines.size() + 2));
        MemoryBoundary memoryBoundary = this.memory.addProcess(pcb, programLines);
        this.memory.updateMemoryBoundary(pcb.processID, memoryBoundary);
        this.scheduler.addReadyProcess(this.processIdCounter);
        this.processIdCounter += 1;
    }


    public void semWaitUserInput(int processID) {
        if (!this.userInput.semWait(processID)) {
            this.scheduler.blockProcess(processID);
            this.memory.changePCBState(processID, "blocked");
        }
    }

    public void semWaitUserOutput(int processID) {
        if (!this.userOutput.semWait(processID)) {
            this.scheduler.blockProcess(processID);
            this.memory.changePCBState(processID, "blocked");
        }
    }

    public void semWaitFile(int processID) {
        if (!this.file.semWait(processID)) {
            this.scheduler.blockProcess(processID);
            this.memory.changePCBState(processID, "blocked");
        }
    }

    public void semSignalUserInput(int processID) {
        this.userInput.semSignal(processID);
        int waitingProcess = this.userInput.waiting.remove();
        this.scheduler.makeBlockedProcessReady(waitingProcess);
        this.memory.changePCBState(waitingProcess, "ready");
    }

    public void semSignalUserOutput(int processID) {
        this.userOutput.semSignal(processID);
        int waitingProcess = this.userOutput.waiting.remove();
        this.scheduler.makeBlockedProcessReady(waitingProcess);
        this.memory.changePCBState(waitingProcess, "ready");
    }

    public void semSignalFile(int processID) {
        this.file.semSignal(processID);
        int waitingProcess = this.file.waiting.remove();
        this.scheduler.makeBlockedProcessReady(waitingProcess);
        this.memory.changePCBState(waitingProcess, "ready");
    }


}
