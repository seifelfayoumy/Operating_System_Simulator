public class PCB {
    int processID;
    String processState;
    int programCounter;
    MemoryBoundary memoryBoundaries;


    public PCB(int processID, String processState, int programCounter, MemoryBoundary memoryBoundaries) {
        this.processID = processID;
        this.processState = processState;
        this.programCounter = programCounter;
        this.memoryBoundaries = memoryBoundaries;

    }
}
