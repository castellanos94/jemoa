package com.castellanos94.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
@Command(name ="moead")
public class MOEADSubCommand implements Runnable{
    @ParentCommand
    private Main mainCmd;
    @Override
    public void run() {
       
    }

   
}
