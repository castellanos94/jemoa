# import the os module
import os
from os.path import join
from os import walk
from pathlib import Path
from shutil import copyfile

# detect the current working directory and print it
path = os.getcwd()
print("The current working directory is %s" % path)

# define the name of the directory to be created
dir_unzip = "/home/thinkpad/Documents/jemoa/experiments/DTLZ-10Obj-NRV"
workdir = "/home/thinkpad/Documents/jemoa/experiments/10/NRV/"
try:
    os.mkdir(workdir)
except OSError:
    print("Creation of the directory %s failed" % workdir)
else:
    print("Successfully created the directory %s " % workdir)

dirpath, dirnames, filenames = next(walk(dir_unzip))
print(dir_unzip, dirpath, dirnames, filenames)
algorithms = ["VAR-0", "VAR-97", "VAR-98",
              "VAR-100", "VAR-104", "VAR-112", "VAR-127"]
for name in algorithms:
    ndir = join(workdir, name)
    if not os.path.exists(ndir):
        try:
            os.mkdir(ndir)
        except OSError:
            print("Creation of the directory %s failed" % ndir)
        else:
            print("Successfully created the directory %s " % ndir)

for dirname in dirnames:
    current_dir = join(dirpath, dirname)
    _, dirnames, _ = next(walk(current_dir))
    print(dirname)
    result = list(Path(current_dir).rglob("*.[tT][xX][tT]"))
    print("Working at ", current_dir)

    for r in result:
        if "DM01" in r.name:
            problem_name = r.name.split("-")[0]
            algorithmName = ""
            for a in algorithms:
                if a in r.name:
                    algorithmName = a
                    break
            dst = join(workdir,algorithmName, problem_name)
            if not os.path.exists(dst):
                try:
                    os.mkdir(dst)
                except OSError:
                    print("Creation of the directory %s failed" % dst)
                else:
                    print("Successfully created the directory %s " % dst)
            copyfile(r,join(dst, r.name.replace(problem_name+"-","")))
