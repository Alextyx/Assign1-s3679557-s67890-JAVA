import os
import subprocess
import sys

javacomplie = 'javac NearestNeighFileBasedForTiming.java'
javaclass = "NearestNeighFileBasedForTiming"


def run_dynamic_eval(cwd):
    cwd = os.path.abspath(cwd)
    subprocess.run(javacomplie, cwd=cwd)
    for impl in ["naive", "kdtree"]:
        for n in [1000, 10000, 100000]:
            for k in [5]:
                for m in [10, 100, 1000]:
                    inputfilename = "test\dynamic_test_{}_k{}_M{}.in".format(n, k, m)
                    outputfilename = "test\dynamic_test_{}_k{}_M{}.out".format(n, k, m)
                    datafile = "generation\sample_data_{}".format(n)
                    cmd = "java {} {} {} {} {}".format(javaclass, impl, datafile, inputfilename, outputfilename)
                    subprocess.run(cmd, cwd=cwd)
                    subprocess.run(cmd, cwd=cwd)
                    subprocess.run(cmd, cwd=cwd)


def run_fixed_eval(cwd):
    cwd = os.path.abspath(cwd)
    subprocess.run(javacomplie, cwd=cwd)
    for impl in ["naive", "kdtree"]:
        for n in [1000, 10000, 100000]:
            for k in [5,20,100]:
                for m in [10, 100, 1000]:
                    inputfilename = "test\static_test_{}_k{}_M{}.in".format(n, k, m)
                    outputfilename = "test\static_test_{}_k{}_M{}.out".format(n, k, m)
                    datafile = "generation\sample_data_{}".format(n)
                    cmd = "java {} {} {} {} {}".format(javaclass, impl, datafile, inputfilename, outputfilename)
                    subprocess.run(cmd, cwd=cwd)
                    subprocess.run(cmd, cwd=cwd)
                    subprocess.run(cmd, cwd=cwd)


if __name__ == '__main__':
#  "C:\Projects\Assign1-s3679557-s67890-JAVA" path to root dir
#     run_dynamic_eval(sys.argv[1])
    run_fixed_eval(sys.argv[1])
