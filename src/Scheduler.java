import java.util.Queue;

public class Scheduler {
    private Queue<Integer> blockedQueue;
    private Queue<Process> readyQueue;
    private Process runningProcess;
    private int timeSlice;
    private Memory memory;

    public Scheduler(int timeSlice, Memory memory) {
        this.timeSlice = timeSlice;
        this.memory = memory;
    }

    public int getNextInstruction() {
        if (runningProcess == null) {
            Process process = this.readyQueue.remove();
            if (process == null) {
                return -1;
            } else {
                this.runningProcess = process;
            }
        } else if (runningProcess.remainingTime <= 0) {
            this.readyQueue.add(new Process(runningProcess.processID, this.timeSlice));
            this.memory.changePCBState(runningProcess.processID, "ready");
            this.runningProcess = this.readyQueue.remove();
        }

        PCB pcb = this.memory.getPcb(this.runningProcess.processID);

        if (pcb.programCounter > pcb.memoryBoundaries.end) {
            this.memory.changePCBState(pcb.processID, "finished");
            runningProcess = this.readyQueue.remove();
            if (runningProcess == null) {
                return -1;
            }
        } else {
            pcb.processState = "running";
            runningProcess.remainingTime -= 1;
        }

        int next = pcb.memoryBoundaries.start + pcb.programCounter;
        pcb.programCounter += 1;

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
        this.blockedQueue.add(processID);
    }

    public void makeBlockedProcessReady(int processID) {
        this.blockedQueue.remove(processID);
        this.readyQueue.add(new Process(processID, this.timeSlice));
    }
}
