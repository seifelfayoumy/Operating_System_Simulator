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
        if (index < 0 || index >= size) {
            return false;
        }
        memory[index] = word;
        return true;
    }

    public void printMemory() {
        System.out.println("\n======= Kernel Memory =======");
        for (int i = 0; i < 12; i++) {
            if (this.memory[i] == null) {
                System.out.println("Index " + i + ": EMPTY");
            } else {
                System.out.println("Index " + i + ": Variable: " + this.memory[i].variable + ", Data: " + this.memory[i].data);
            }
        }

        System.out.println("\n======= User Memory =======");
        for (int i = 12; i < memory.length; i++) {
            if (this.memory[i] == null) {
                System.out.println("Index " + i + ": EMPTY");
            } else {
                System.out.println("Index " + i + ": Variable: " + this.memory[i].variable + ", Data: " + this.memory[i].data);
            }
        }
    }

    public boolean isEmpty(int index) {
        if (index < 0 || index >= size || memory[index] != null || memory[index].data != null) {
            return false;
        }
        return true;
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

    public MemoryBoundary addProcess(PCB pcb, List<String> programLines) {
        MemoryBoundary memoryBoundary = null;
        boolean addedPcb = false;
        for (int i = 0; i < 9; i += 4) {
            if (spaceEmpty(i)) {
                this.addPCB(pcb, i);
                addedPcb = true;
                break;
            }
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
        memoryBoundary = new MemoryBoundary(firstIndex, firstIndex + programLines.size() + 3);

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
        } else {
            while (!spaceExists(programLines.size() + 3)) {
                int processIDToSwap = selectProcessToSwapOut();
                swapToDisk(processIDToSwap);
            }
            return addProcess(pcb, programLines);
        }
        return memoryBoundary;
    }

    private PCB getPcbFromMemory(int processID) {
        for (int i = 0; i < 12; i += 4) {
            if (this.memory[i] != null && this.memory[i].variable.equals("processID") && this.memory[i].data.equals(processID)) {
                return new PCB(processID, (String) this.memory[i + 1].data, (int) this.memory[i + 2].data, (MemoryBoundary) this.memory[i + 3].data);
            }
        }
        return null;
    }


    private void swapToDisk(int processID) {
        PCB pcb = getPcbFromMemory(processID);
        if (pcb != null) {
            if (!pcb.memoryBoundaries.onDisk) {
                List<MemoryWord> memoryWords = new ArrayList<>();
                for (int i = pcb.memoryBoundaries.start; i < pcb.memoryBoundaries.end; i++) {
                    memoryWords.add(this.read(i));
                }
                MemoryWord.writeMemoryWordsToFile(memoryWords, processID);
                pcb.memoryBoundaries.onDisk = true;
                this.addPCB(pcb, -1);
                System.out.println("Swapped to Disk Process: " + processID);
            }
        }
    }

    public PCB getPcb(int processID) {
        PCB pcb = getPcbFromMemory(processID);
        if (pcb != null && pcb.memoryBoundaries.onDisk) {
            if (spaceExists(pcb.memoryBoundaries.getLength())) {
                swapFromDisk(processID);
                return getPcbFromMemory(processID);
            } else {
                int processIDToSwap = selectProcessToSwapOut();
                swapToDisk(processIDToSwap);
                swapFromDisk(processID);
                return getPcbFromMemory(processID);
            }
        }
        return pcb;
    }

    private void swapFromDisk(int processID) {
        List<MemoryWord> memoryWords = MemoryWord.readMemoryWordsFromFile(processID);


        int consecutiveAvailable = 0;
        int firstIndex = -1;
        for (int i = 12; i < this.memory.length; i++) {
            if (spaceEmpty(i)) {
                if (consecutiveAvailable == 0) {
                    firstIndex = i;
                }
                consecutiveAvailable++;
                if (consecutiveAvailable == memoryWords.size()) {
                    break;
                }
            } else {
                consecutiveAvailable = 0;
            }
        }
        if(consecutiveAvailable == memoryWords.size()){
            int j = 0;
            for (int i = firstIndex; i < firstIndex + memoryWords.size(); i++) {
                this.allocate(memoryWords.get(j), i);
                j++;
            }
        }

        PCB pcb = getPcbFromMemory(processID);
        pcb.memoryBoundaries.start = firstIndex;
        pcb.memoryBoundaries.end = firstIndex + memoryWords.size();
        pcb.memoryBoundaries.onDisk = false;
        this.addPCB(pcb, -1);
        System.out.println("Swapped From Disk Process: " + processID);
    }

    public void addPCB(PCB pcb, int index) {
        boolean exists = false;
        for (int i = 0; i < 9; i += 4) {
            if (this.memory[i] != null && this.memory[i].variable.equals("processID") && this.memory[i].data.equals(pcb.processID)) {
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


    public boolean spaceExists(int programLength) {

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


    public boolean spaceEmpty(int index) {
        if (memory[index] == null) {
            return true;
        }

        for (int i = 0; i < 12; i += 4) {
            if (memory[i] != null && memory[i].variable.equals("processID") &&
                    memory[i + 3].variable.equals("memoryBoundaries") && memory[i + 3].data instanceof MemoryBoundary) {
                MemoryBoundary boundary = (MemoryBoundary) memory[i + 3].data;
                if (index >= boundary.start && index < boundary.end) {
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
            if (this.read(i) != null && this.read(i).data.equals(processID)) {
                this.memory[i + 1].data = state;
            }
        }
    }

    public void updateMemoryBoundary(int processID, MemoryBoundary memoryBoundary) {
        for (int i = 0; i < 9; i += 4) {
            if (this.read(i) != null && this.read(i).data.equals(processID)) {
                this.memory[i + 3].data = memoryBoundary;
            }
        }
    }

    public int selectProcessToSwapOut() {
        List<PCB> pcbs = getAllPCBs();
        PCB largestBlocked = null;
        PCB largestReady = null;
        for (PCB pcb : pcbs) {
            String processState = pcb.processState;
            MemoryBoundary boundary = pcb.memoryBoundaries;
            int memoryLength = boundary.getLength();

            if ("blocked".equals(processState)) {
                if (largestBlocked == null || memoryLength > largestBlocked.memoryBoundaries.end - largestBlocked.memoryBoundaries.start) {
                    largestBlocked = pcb;
                }
            } else if ("ready".equals(processState)) {
                if (largestReady == null || memoryLength > largestReady.memoryBoundaries.end - largestReady.memoryBoundaries.start) {
                    largestReady = pcb;
                }
            }
        }

        if (largestBlocked != null) {
            return largestBlocked.processID;
        } else if (largestReady != null) {
            return largestReady.processID;
        }

        throw new RuntimeException("No processes are available to swap out");
    }

    public List<PCB> getAllPCBs() {
        List<PCB> pcbs = new ArrayList<>();
        for (int i = 0; i < 9; i += 4) {
            if (memory[i] != null && memory[i].variable.equals("processID")) {
                int processID = (int) memory[i].data;
                String processState = (String) memory[i + 1].data;
                int programCounter = (int) memory[i + 2].data;
                MemoryBoundary memoryBoundaries = (MemoryBoundary) memory[i + 3].data;
                pcbs.add(new PCB(processID, processState, programCounter, memoryBoundaries));
            }
        }
        return pcbs;
    }


}
