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
        this.allocate(new MemoryWord("processID", pcb.processID), index);
        this.allocate(new MemoryWord("processState", pcb.processState), index + 1);
        this.allocate(new MemoryWord("programCounter", pcb.programCounter), index + 2);
        this.allocate(new MemoryWord("memoryBoundaries", pcb.memoryBoundaries), index + 3);
    }


//    public boolean isFull() {
//        return !memory.co (null);
//    }
}
