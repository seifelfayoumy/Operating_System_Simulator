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
        this.processIdCounter = 1;
        this.userInput = new Mutex();
        this.userOutput = new Mutex();
        this.file = new Mutex();
        this.scheduler = new Scheduler(timeSlice, this.memory);
    }

    public void clock(String programAddress) {
        int nextInstruction = this.scheduler.getNextInstruction();
        if (nextInstruction != -1) {
            PCB pcb = this.memory.getPcb(this.scheduler.getRunningProcessID());
            String instruction = (String) this.memory.read(nextInstruction).data;
            this.scheduler.printRunningProcess();
            System.out.println("Executing Instruction: " + instruction);
            Interpreter.execute(instruction, new SystemCalls(pcb, this), pcb.processID);
        }

        if (programAddress != null && !programAddress.equals("")) {
            this.addProcess(Interpreter.readProgram(programAddress));
            this.scheduler.printQueues();
            this.scheduler.printRunningProcess();
        }
        this.memory.printMemory();
    }


    public void addProcess(List<String> programLines) {
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
        if(this.userInput.semSignal(processID)){
            int waitingProcess;
            try {
                waitingProcess = this.userInput.waiting.remove();
            } catch (Exception e) {
                waitingProcess = -1;
            }
            if (waitingProcess != -1) {
                this.scheduler.makeBlockedProcessReady(waitingProcess);
                this.memory.changePCBState(waitingProcess, "ready");

            }
        }

    }

    public void semSignalUserOutput(int processID) {
        if(this.userOutput.semSignal(processID)){
            int waitingProcess;
            try {
                waitingProcess = this.userOutput.waiting.remove();
            } catch (Exception e) {
                waitingProcess = -1;
            }
            if (waitingProcess != -1) {
                this.scheduler.makeBlockedProcessReady(waitingProcess);
                this.memory.changePCBState(waitingProcess, "ready");

            }
        }



    }

    public void semSignalFile(int processID) {
        if(this.file.semSignal(processID)){
            int waitingProcess;
            try {
                waitingProcess = this.file.waiting.remove();
            } catch (Exception e) {
                waitingProcess = -1;
            }
            if (waitingProcess != -1) {
                this.scheduler.makeBlockedProcessReady(waitingProcess);
                this.memory.changePCBState(waitingProcess, "ready");

            }
        }


    }


}
