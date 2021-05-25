@echo off
echo Running al problem in parallel
Title Running IMOACOR Experiment
set "javaPath=jdk11\bin\java.exe"
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 1 --endProblem 1 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 2 --endProblem 2 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 3 --endProblem 3 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 4 --endProblem 4 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 5 --endProblem 5 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 6 --endProblem 6 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 7 --endProblem 7 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 8 --endProblem 8 --seed 1
start "" %javaPath% -jar -a IMOACORP -m 5 --initialProblem 9 --endProblem 9 --seed 1
echo all done!
pause