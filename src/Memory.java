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

    public boolean spaceExists(int programLength) {
        boolean pcbSpace = false;
        boolean programSpace = false;
        if (this.read(0) == null || this.read(4) == null || this.read(8) == null) {
            pcbSpace = true;
        } else if (this.read(1).data.equals("finished") || this.read(5).data.equals("finished") || this.read(9).data.equals("finished")) {
            pcbSpace = true;
        }

        if (!pcbSpace) {
            return false;
        } else {
            return this.hasConsecutiveNulls(programLength) != -1;
        }
    }

    public void addProcess(PCB pcb, List<String> programLines) {
        boolean addedPcb = false;
        for (int i = 0; i < 9; i += 4) {
            if (this.read(i) == null || this.read(i + 1).data.equals("finished")) {
                this.addPCB(pcb, i);
                addedPcb = true;
                break;
            }
        }

        int firstIndex = this.hasConsecutiveNulls(programLines.size() + 3);
        if (firstIndex != -1) {
            int j = 0;
            for (int i = firstIndex; i < programLines.size() + 3; i++) {
                if (i == firstIndex) {
                    this.allocate(new MemoryWord("var_1", null), i);
                } else if (i == firstIndex + 1) {
                    this.allocate(new MemoryWord("var_2", null), i);
                } else if (i == firstIndex + 2) {
                    this.allocate(new MemoryWord("var_3", null), i);
                } else {
                    this.allocate(new MemoryWord("line_" + j, programLines.get(j)), i);
                    j += 1;
                }

            }
        }
    }

    private int hasConsecutiveNulls(int n) {
        int consecutiveCount = 0;
        for (int i = 0; i < this.memory.length; i++) {
            if (this.memory[i] == null) {
                consecutiveCount++;
                if (consecutiveCount == n) {
                    return i - n + 1;  // Return the starting index of the n consecutive nulls
                }
            } else {
                consecutiveCount = 0;
            }
        }
        return -1;
    }


//    public boolean isFull() {
//        return !memory.co (null);
//    }
}
