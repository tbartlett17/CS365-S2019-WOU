//tyler bartlett

#include <stdio.h>
#include <iostream>
#include <time.h>
#include <stdlib.h>
#include <string>
#define PROC_COUNT 8 // num of procs to generate


struct process{
    std::string name;
    int arrivalTime;
    int burstTime;
    int priority;
    int waitTime, turnAroundTime;
    int flag;
};

process generateProcess(int pID)
{
    process newProc;

    newProc.name = "p" + std::to_string(pID);
    newProc.arrivalTime = rand() % 11; // arrival time is 0-10
    newProc.burstTime = 2 + rand() % 41; //burst time is 2-42
    newProc.priority = rand() % 4 + 1; //setting a random priorty from 1 to 4. 1>2>3>4
    return newProc;
}

void sortArrivalTimes(process procArr[])
{
    process tempProc;

    for (int i = 1; i < PROC_COUNT; i++)
    {
        for (int j = 0; j < PROC_COUNT - i; j++)
        {
            if(procArr[j].arrivalTime > procArr[j+1].arrivalTime)
            {
				tempProc = procArr[j];
				procArr[j] = procArr[j+1];
				procArr[j+1] = tempProc;
            }
        }
    }

}

//first come first serve algo
void fcfs(process allProcs[])
{
    process temp[PROC_COUNT];
    int totalWait = 0, totalTurnAround = 0;
    float avgWait = 0.0, avgTurnAround = 0.0;

    //copy proc arr to temp arr
    for (int i = 0; i < PROC_COUNT; i++)
    {
        temp[i] = allProcs[i];
    }

    sortArrivalTimes(temp);

    totalWait = temp[0].waitTime = 0;
    totalTurnAround = temp[0].turnAroundTime = temp[0].burstTime - temp[0].arrivalTime;

    for (int i = 1; i < PROC_COUNT; i++)
    {
        temp[i].waitTime = (temp[i - 1].burstTime + temp[i - 1].arrivalTime + temp[i - 1].waitTime) - temp[i].arrivalTime;
        temp[i].turnAroundTime = (temp[i].waitTime + temp[i].burstTime);

        totalWait += temp[i].waitTime;
        totalTurnAround += temp[i].turnAroundTime;
    }

    avgWait = (float) totalWait / PROC_COUNT;
    avgTurnAround = (float) totalTurnAround / PROC_COUNT;

    std::cout << "pID, ArrivalTime, BurstTime, WaitTime, TurnAroundTime\n";

    for (int i = 0; i < PROC_COUNT; i++)
    {
        std::cout << temp[i].name << "\t" << temp[i].arrivalTime << "\t" << temp[i].burstTime << "\t" << temp[i].waitTime << "\t" << temp[i].turnAroundTime << "\n";
    }

}

//round robin algo
void rr(process allProcs[])
{
    int pflag = 0, time = 0, currentTime = 0, temp;
    int totalWait = 0, totalTurnAround = 0;
    float avgWait = 0.0, avgTurnAround = 0.0;

    process temp1[PROC_COUNT], temp2[PROC_COUNT]; 

    for (int i = 0; i < PROC_COUNT; i++)
    {
        temp1[i] = allProcs[i];
    }

    sortArrivalTimes(temp1);

    for(int i=0;i<PROC_COUNT;i++)
    {
        temp2[i]=temp1[i];
    }

    std::cout << "using time quantum of 10\n";
    int quantum = 10;

    for(int k=0;;k++)
    {
		if (k > PROC_COUNT - 1)
        {
            k = 0;
        }
        if (temp1[k].burstTime > 0)
        {
            //printf("  %d  %s", currentTime, temp1[k].name);
            std::cout << currentTime << " " << temp1[k].name << "\n";
        }

        time = 0;
        while (time < quantum && temp1[k].burstTime > 0)
        {
            time++;
            currentTime++;
            temp1[k].burstTime--;
        }

        if (temp1[k].burstTime <= 0 && temp1[k].flag != 1)
        {
            temp1[k].waitTime = currentTime - temp2[k].burstTime - temp1[k].arrivalTime;
            temp1[k].turnAroundTime = currentTime - temp1[k].arrivalTime;

            pflag++;
            temp1[k].flag = 1;

            totalWait += temp1[k].waitTime;
            totalTurnAround += temp1[k].turnAroundTime;
        }

        if (pflag == PROC_COUNT)
        {
            break;
        }
	}
	//printf("  %d",currentTime);
	std::cout << currentTime << "\n";
    avgWait = (float)totalWait/PROC_COUNT;
	avgTurnAround = (float)totalTurnAround/PROC_COUNT;
    //printf("\n\n Average waiting time = %0.2f\n Average turn-around = %0.2f.",avgwt,avgta);
    std::cout << "avgWaitTime: " << avgWait << " avgTurnAround: " << avgTurnAround << "\n";
}


int main()
{
    srand(time(NULL));

    
    process allProcs[PROC_COUNT]; // array to hold all process
    
    
    std::cout << "generating " << PROC_COUNT << " processes...\n\n";
    for (int i = 0; i < PROC_COUNT; i++)
    {
        allProcs[i] = generateProcess(i);
        //std::cout << "name: " << allProcs[i].name << "\tarrival time: " << allProcs[i].arrivalTime << '\n';
    }
    std::cout << "done!\n";

    std::cout << "performing fcfs algo...\n";
    fcfs(allProcs);
    std::cout << "done!\n";

    std::cout << "performing rr algo...\n";
    rr(allProcs);
    std::cout << "done!\n";
    
    return 0; //donezo muy bien
}