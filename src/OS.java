import java.util.ArrayList;
import java.util.Queue;

public class OS {
    Object[] memory;
    int memoryPointer = 0;
    int processIdCounter = 0;
    int roundRobinTime =0;
    PCB runningProcess;
    Queue<PCB> readyQueue;
    Queue<PCB> blockedQueue;


    public void addProcess(String[] programLines) {
        int processLength = programLines.length + 1 + 3;

        if(memoryPointer < (40 - processLength)){
            //store in disk later
        }else{
            int min = memoryPointer;
            int max = memoryPointer + processLength;
            PCB pcb = new PCB(processIdCounter,"ready",memoryPointer, new int[]{min, max});
            processIdCounter += 1;
            memory[memoryPointer] = pcb;
            memory[memoryPointer + 1] = "var_1";
            memory[memoryPointer + 2] = "var_2";
            memory[memoryPointer + 3] = "var_3";
            memoryPointer += 4;
            for(int i=0;i<programLines.length;i++){
                memory[memoryPointer] = programLines[i];
                memoryPointer += 1;
            }
            readyQueue.add(pcb);
        }

    }

    //USE MEMORY INSTEAD TO STORE QUEUES AND PCBS THEN USER INSTRUCTIONS AND VARIABLES
    public int getNextInstruction(){
        if(roundRobinTime > 1){
            runningProcess = this.readyQueue.remove();
            int next = runningProcess.programCounter;
            roundRobinTime = 0;
            runningProcess.programCounter+= 1;
            runningProcess.processState = "running";

        }else{

        }
        return 0;
    }



}
