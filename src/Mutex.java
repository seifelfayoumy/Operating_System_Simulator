import java.util.LinkedList;
import java.util.Queue;

class Mutex {
    private boolean isLocked;
    private int lockingProcessId;
    Queue<Integer> waiting;

    public Mutex(){
        this.isLocked = false;
        this.lockingProcessId = -1;
        this.waiting = new LinkedList<>();
    }

    public boolean semWait(int processId) {
        if (isLocked) {
            waiting.add(processId);
            return false;
        }
        isLocked = true;
        lockingProcessId = processId;
        return true;
    }

    public boolean semSignal(int processId) {
        if (!isLocked) {
            return true;
        }

        if (processId != lockingProcessId) {
            return false;
        }

        isLocked = false;
        lockingProcessId = -1;
        return true;
    }
}
