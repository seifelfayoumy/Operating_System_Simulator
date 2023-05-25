public class MemoryBoundary {
    public int start;
    public int end;
    boolean onDisk;

    public MemoryBoundary(int start, int end) {
        this.start = start;
        this.end = end;
        this.onDisk = false;
    }

    public int getLength(){
        return this.end = this.start;
    }


}
