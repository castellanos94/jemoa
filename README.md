# JEMOA
Java Evolutionary Multi-Objective Algorithms, this version was developed with Java 11.
# Data type
   * Integer
   * Real (double)
   * Interval 
   * Fuzzy Number (Trapezoidal)
# Problems
  *  PSP
  *  Knapsack
  *  DTLZ
  *  ZDT
# Algorithms
*  For single-objective:
   * GA: a genetic algorithm. 
   * GWO: Grey Wolf Optimizer
*  For multi-objective optimization:
   * GA: A multi-objectve evolutionary algorithm (using dominance)
   * NSGA-II
   * NSGA-III
   * NSGA-III-P : NSGA-III with preferences incorporation, using a multi-criteria ordinal classifier
   * MOGWO : Multi-Objective Grey Wolf Optimizer
   * MOGWO/DE: Multi-Objective Grey Wolf Optimizer based on decomposition
   * MOGWO-V : Multi-Objective Grey Wolf Optimizer with SBX crossover and Polynomial mutation
   * MOGWO-P : Multi-Objective Grey Wolf Optimizer with preferences incorporation, using a multi-criteria ordinal classifier
   * MOGWO-O : Multi-Objective Grey Wolf Optimizer with preferences incorporation, using a net outranking score
   * iMOACO_R : Indicator-Based Multi-Objective Ant Colony Optimization Algorithm for Continuous Search Spaces
   * GWO-InClass: Multi-Objective Grey Wolf Optimizer with InterClass-nC 
   * ACO-InClass: Indicator-Based Multi-Objective Ant Colony Optimization Algorithm for Continuous Search Spaces with InterClass-nC
# Multi-criteria Decision Aid (MCDA)
   * Electre Tri
   * INTERCLASS-nC
   * INTERCLASS-nB
   * SatClassifier
# Execution DTLZ Test
To compile and run from command line the test suite it is necessary to execute the following maven goal:
```
$ mvn clean compile package 
```
For run
```
$ java -jar jemoa-1.0.0-jar-with-dependencies.jar -a NSGAIII -m 3   
```
It is required to have a directory -relative to the execution directory- with the instances in the following form:
```
DTLZ_INSTANCES/numberOfObjectives/DTLZ$N$_Instance.txt
```
Where N is the number of DTLZ problem. 
For more details use the --help
# Examples
There are examples of how to execute each algorithm in the EXAMPLE package, it is only necessary to execute the class of interest, this from an IDE.  
E.G. 
<a href="src/main/java/com/castellanos94/examples/DTLZNsga3.java">
Grey Wolf Optimizer example</a>.
# Publications
* Castellanos A, Cruz-Reyes L, Fernández E, Rivera G, Gomez-Santillan C, Rangel-Valdez N. Hybridisation of Swarm Intelligence Algorithms with Multi-Criteria Ordinal Classification: A Strategy to Address Many-Objective Optimisation. Mathematics. 2022; 10(3):322. <a href="https://doi.org/10.3390/math10030322" target="_blank">doi.org/10.3390/math10030322 </a>
* Castellanos-Alvarez A, Cruz-Reyes L, Fernandez E, Rangel-Valdez N, Gómez-Santillán C, Fraire H, Brambila-Hernández JA. A Method for Integration of Preferences to a Multi-Objective Evolutionary Algorithm Using Ordinal Multi-Criteria Classification Mathematical and Computational Applications. 2021; 26(2):27. <a href="https://doi.org/10.3390/mca26020027" target="_blank">doi.org/10.3390/mca26020027</a>