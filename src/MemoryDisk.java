import java.util.List;

public class MemoryDisk {
    List<MemoryWord> data;
    int processID;

    public MemoryDisk(List<MemoryWord> data, int processID){
        this.data = data;
        this.processID = processID;
    }
}
