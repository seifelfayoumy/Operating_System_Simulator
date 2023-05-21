import java.util.*;

public class Memory {
    private MemoryWord[] memory;
    private int size;

    public Memory(int size) {
        this.size = size;
        this.memory = new MemoryWord[size];
    }

    public boolean allocate(MemoryWord word, int index) {
        if (index < 0 || index >= size || memory[index] != null) {
            return false;
        }
        memory[index] = word;
        return true;
    }

    public boolean isEmpty(int index) {
        if (index < 0 || index >= size || memory[index] != null || memory[index].data != null) {
            return false;
        }
        return true;
    }

    public MemoryWord deallocate(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        MemoryWord word = memory[index];
        memory[index] = null;
        return word;
    }

    public MemoryWord read(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return memory[index];
    }

    public Object getVariable(String name) {
        for (int i = 0; i < this.size; i++) {
            if (this.memory[i] != null && this.memory[i].variable.equals(name)) {
                return this.memory[i].data;
            }
        }
        return null;
    }

    public void addPCB(PCB pcb, int index) {
        boolean exists = false;
        for (int i = 0; i < this.memory.length; i++) {
            if (this.memory[i].variable.equals("processID") && this.memory[i].data.equals(pcb.processID)) {
                this.allocate(new MemoryWord("processID", pcb.processID), i);
                this.allocate(new MemoryWord("processState", pcb.processState), i + 1);
                this.allocate(new MemoryWord("programCounter", pcb.programCounter), i + 2);
                this.allocate(new MemoryWord("memoryBoundaries", pcb.memoryBoundaries), i + 3);
                exists = true;
            }
        }
        if (!exists) {
            this.allocate(new MemoryWord("processID", pcb.processID), index);
            this.allocate(new MemoryWord("processState", pcb.processState), index + 1);
            this.allocate(new MemoryWord("programCounter", pcb.programCounter), index + 2);
            this.allocate(new MemoryWord("memoryBoundaries", pcb.memoryBoundaries), index + 3);
        }
    }

    public PCB getPcb(int processID) {
        for (int i = 0; i < this.memory.length; i++) {
            if (this.memory[i].variable.equals("processID") && this.memory[i].data.equals(processID)) {
                return new PCB(processID, (String) this.memory[i + 1].data, (int) this.memory[i + 2].data, (MemoryBoundary) this.memory[i + 3].data);
            }
        }
        return null;
    }


//    public boolean isFull() {
//        return !memory.co (null);
//    }
}
