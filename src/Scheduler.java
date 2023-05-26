import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Scheduler {
    private Queue<Integer> blockedQueue;
    private Queue<Process> readyQueue;
    private Process runningProcess;
    private int timeSlice;
    private Memory memory;
    private Map<Integer, Boolean> halfCycleRemaining = new HashMap<>();

    public Scheduler(int timeSlice, Memory memory) {
        this.timeSlice = timeSlice;
        this.memory = memory;
        this.readyQueue = new LinkedList<>();
        this.blockedQueue = new LinkedList<>();
    }

    public void printQueues() {
        System.out.println("Blocked Queue:");
        for (Integer value : blockedQueue) {
            System.out.println(value);
        }

        System.out.println("Ready Queue:");
        for (Process process : readyQueue) {
            System.out.println(process.processID);
        }
    }

    public void printRunningProcess() {
        if (this.runningProcess == null) {
            System.out.println("Running Process: none");
        } else {
            System.out.println("Running Process: " + this.runningProcess.processID);
        }

    }

    public int getNextInstruction() {
        if (runningProcess == null) {
            Process process;
            try {
                process = this.readyQueue.remove();
            } catch (Exception e) {
                process = null;
            }
            if (process == null) {
                return -1;
            } else {
                this.runningProcess = process;
            }
        } else if (runningProcess.remainingTime <= 0) {
            this.readyQueue.add(new Process(runningProcess.processID, this.timeSlice));
            this.memory.changePCBState(runningProcess.processID, "ready");
            try {
                runningProcess = this.readyQueue.remove();
            } catch (Exception e) {
                runningProcess = null;
            }
            this.printQueues();
        }

        int next = -1;
        PCB pcb = this.memory.getPcb(this.runningProcess.processID);
        Boolean isHalfCycleRemaining = halfCycleRemaining.getOrDefault(this.runningProcess.processID, false);
        if (pcb.programCounter + pcb.memoryBoundaries.start >= pcb.memoryBoundaries.end) {
            this.memory.changePCBState(pcb.processID, "finished");
            try {
                runningProcess = this.readyQueue.remove();
            } catch (Exception e) {
                runningProcess = null;
            }
            this.printQueues();
            if (runningProcess == null) {
                return -1;
            }
        }
        pcb = this.memory.getPcb(this.runningProcess.processID);
        pcb.processState = "running";
        this.memory.changePCBState(pcb.processID, "running");
        MemoryWord nextWord = this.memory.read(pcb.memoryBoundaries.start + pcb.programCounter);
        next = pcb.memoryBoundaries.start + pcb.programCounter;
        if (isHalfCycleRemaining) {
            runningProcess.remainingTime -= 1; // Deduct one more unit of time, as it's the second cycle.
            halfCycleRemaining.put(this.runningProcess.processID, false);
            pcb.programCounter += 1;
        } else if (isTwoCycleInstruction((String) nextWord.data)) {
            runningProcess.remainingTime -= 1;
            halfCycleRemaining.put(this.runningProcess.processID, true);
        } else {
            runningProcess.remainingTime -= 1;
            pcb.programCounter += 1;
        }


        this.memory.addPCB(pcb, -1);
        return next;


    }

    public int removeFromBlockedQueue() {
        return this.blockedQueue.remove();
    }

    public int getRunningProcessID() {
        return this.runningProcess.processID;
    }

    public void addReadyProcess(int processID) {
        this.readyQueue.add(new Process(processID, this.timeSlice));
    }

    public void blockProcess(int processID) {
        if (this.runningProcess.processID == processID) {
            this.runningProcess = null;
        }
        this.blockedQueue.add(processID);
        this.printQueues();

    }

    public void makeBlockedProcessReady(int processID) {
        this.blockedQueue.remove(processID);
        this.readyQueue.add(new Process(processID, this.timeSlice));
        this.printQueues();

    }

    private boolean isTwoCycleInstruction(String instruction) {
        String[] words = instruction.split(" ");
        return words[0].equals("assign") && ((words[2].equals("input") || words[2].startsWith("readFile")));
    }
}
