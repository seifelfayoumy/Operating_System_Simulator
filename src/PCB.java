public class PCB {
    int processId;
    String processState;
    int programCounter;
    int[] memoryBoundaries;

    public PCB(int processId, String processState, int programCounter, int[] memoryBoundaries){
        this.processId = processId;
        this.processState = processState;
        this.programCounter = programCounter;
        this.memoryBoundaries = memoryBoundaries;
    }
}
