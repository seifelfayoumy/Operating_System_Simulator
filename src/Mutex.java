class Mutex {
    private boolean isLocked = false;
    private int lockingProcessId = -1;

    public boolean semWait(int processId) {
        if (isLocked) {
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
