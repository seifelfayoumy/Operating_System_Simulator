import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    public Object getVariableData(String name) {
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
                if (((MemoryBoundary) this.memory[i + 3].data).onDisk) {
                    MemoryDisk oldData = this.getProcessData();
                    MemoryWord.writeMemoryWordsToFile(oldData.data, oldData.processID);
                    List<MemoryWord> data = MemoryWord.readMemoryWordsFromFile((Integer) this.memory[i].data);
                    ((MemoryBoundary) this.memory[i + 3].data).onDisk = false;
                    int k = 0;
                    for (int j = ((MemoryBoundary) this.memory[i + 3].data).start; j < ((MemoryBoundary) this.memory[j + 3].data).end; j++) {
                        this.memory[j] = data.get(k);
                        k++;
                    }
                }
                return new PCB(processID, (String) this.memory[i + 1].data, (int) this.memory[i + 2].data, (MemoryBoundary) this.memory[i + 3].data);
            }
        }
        return null;
    }

    public boolean spaceExists(int programLength) {
        boolean pcbSpace = false;

        for (int i = 0; i < 12; i += 4) {
            if (spaceEmpty(i)) {
                pcbSpace = true;
                break;
            }
        }

        if (!pcbSpace) {
            return false;
        } else {
            int consecutiveAvailable = 0;
            for (int i = 12; i < this.memory.length; i++) {
                if (spaceEmpty(i)) {
                    consecutiveAvailable++;
                    if (consecutiveAvailable == programLength) {
                        return true;
                    }
                } else {
                    consecutiveAvailable = 0;
                }
            }
            return false;
        }
    }

    public MemoryBoundary addProcess(PCB pcb, List<String> programLines) {
        MemoryBoundary memoryBoundary = null;
        boolean addedPcb = false;
        for (int i = 0; i < 12; i += 4) {
            if (spaceEmpty(i)) {
                this.addPCB(pcb, i);
                addedPcb = true;
                break;
            }
        }

        if (!addedPcb) {
            return null;
        }

        int consecutiveAvailable = 0;
        int firstIndex = -1;
        for (int i = 12; i < this.memory.length; i++) {
            if (spaceEmpty(i)) {
                if (consecutiveAvailable == 0) {
                    firstIndex = i;
                }
                consecutiveAvailable++;
                if (consecutiveAvailable == programLines.size() + 3) {
                    break;
                }
            } else {
                consecutiveAvailable = 0;
            }
        }
        memoryBoundary = new MemoryBoundary(firstIndex, programLines.size() + 2);

        if (consecutiveAvailable == programLines.size() + 3) {
            int j = 0;
            for (int i = firstIndex; i < firstIndex + programLines.size() + 3; i++) {
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
        return memoryBoundary;
    }

    public boolean spaceEmpty(int index) {
        if (memory[index] == null) {
            return true;
        }

        for (int i = 0; i < 12; i += 4) {
            if (memory[i] != null && memory[i].variable.equals("processID") &&
                    memory[i + 3].variable.equals("memoryBoundaries") && memory[i + 3].data instanceof MemoryBoundary) {
                MemoryBoundary boundary = (MemoryBoundary) memory[i + 3].data;
                if (index >= boundary.start && index <= boundary.end) {
                    // Consider memory as empty if the process is finished or the memory is on disk
                    if (memory[i + 1].variable.equals("processState") && memory[i + 1].data.equals("finished") ||
                            boundary.onDisk) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public void changePCBState(int processID, String state) {
        for (int i = 0; i < 9; i += 4) {
            if (this.read(i).data.equals(processID)) {
                this.memory[i + 1].data = state;
            }
        }
    }

    public void updateMemoryBoundary(int processID, MemoryBoundary memoryBoundary) {
        for (int i = 0; i < 9; i += 4) {
            if (this.read(i).data.equals(processID)) {
                this.memory[i + 3].data = memoryBoundary;
            }
        }
    }

    public MemoryDisk getProcessData() {
        List<MemoryWord> result = null;
        MemoryBoundary memoryBoundary = null;
        MemoryBoundary maxMemoryBoundary = null;
        int maxLength = 0;
        int processID = -1;
        for (int i = 0; i < 9; i += 4) {
            memoryBoundary = (MemoryBoundary) this.memory[i + 3].data;
            if (memoryBoundary.getLength() > maxLength && (this.read(i + 1).data.equals("blocked") || this.read(i + 1).data.equals("ready"))) {
                maxLength = memoryBoundary.getLength();
                maxMemoryBoundary = memoryBoundary;
                processID = (int) this.memory[i].data;
            }
        }
        for (int i = 0; i < 9; i += 4) {
            if (this.read(i).data.equals(processID)) {
                MemoryBoundary newBoundary = (MemoryBoundary) this.memory[i + 3].data;
                newBoundary.onDisk = true;
                this.memory[i + 3].data = newBoundary;
            }
        }
        for (int i = maxMemoryBoundary.start; i < maxMemoryBoundary.end; i++) {
            result.add(this.memory[i]);
        }
        return new MemoryDisk(result, processID);
    }

}
