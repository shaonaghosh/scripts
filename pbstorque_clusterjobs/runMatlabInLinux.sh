#!/bin/bash
#PBS -S /bin/bash
#PBS -l walltime=6:00:00
#PBS -l nodes=1:ppn=1
startTime=$(date +%s)

module load matlab/2013a

MATLAB_dir="/home/local/software/matlab/2013a"

cd /scratch/experiments/
#./run_wholedatasetdistance.sh $MATLAB_dir allop.txt 2>&1
#matlab -nojvm -nodisplay -r wholedatasetdistance > output_file

matlab -nojvm -nodisplay -r wholedistancegraphcpy > output_file_graph2  
#
wait
endTime=$(date +%s)
runtime=$(( $endTime - $startTime ))
