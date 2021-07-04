package com.castellanos94.cli;

import java.util.HashMap;
import java.util.concurrent.Callable;

import com.castellanos94.algorithms.multi.MOEAD;
import com.castellanos94.solutions.DoubleSolution;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
@Command(name ="moead")
public class MOEADSubCommand implements Runnable{
    @ParentCommand
    private CmdLine mainCmd;
    
    @Override
    public void run() {
       int numberOfObjectives = mainCmd.getNumberOfObjectives();
        HashMap<String, Object> setup = mainCmd.setup(numberOfObjectives); 

       //MOEAD<DoubleSolution> moead = new MOEAD<>(problem, MAX_ITERATIONS, N, weightVectors, T, crossoverOperator, mutationOperator, repairOperator, dominanceComparator, apporachUsed);       
    }

   
}
