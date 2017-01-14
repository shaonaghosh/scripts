#!/usr/bin/env python

import os, stat, sys, shutil
import time
import subprocess
import tarfile


#varK = ['k2','k3','k4','k5', 'k6', 'k7']
#varL = ['l8','l16','l32','l64','l128']
#varN = ['n200', 'n400', 'n600', 'n800','n1000']

#will do the same for the digits dataset
nameofdataset = 'isingapp'


binarydest = '/scratch/sg1g10/0tempjulyexperiments/offlineexp/newsgroup/december/wholegraphconnected3012/class2/longpathofflinesrc/'
expsdest = '/scratch/sg1g10/0tempjulyexperiments/offlineexp/newsgroup/december/wholegraphconnected3012/cls1highLK3MST/'
graphsfile = 'label1000'

graphsdest = expsdest + graphsfile + '/'
graphsfilezipname = graphsfile + '.tar' + '.gz'
zipfiledest = expsdest  + graphsfilezipname
datasetdest = expsdest + nameofdataset + '/'

if not os.path.exists(graphsdest):
            os.makedirs(graphsdest)
#first extract the graphs
#subprocess.Popen(['tar', '-zxvf', graphsdest, graphsfilezipname])
#tar = tarfile.open(zipfiledest)
#tar.extractall()
#tar.close()
#cmd = "tar -zxvf " + zipfiledest
#print cmd
os.chdir(expsdest)
#os.system(cmd)
           


#for nodes in varN:
 #   for neigh in varK:
  #      for labels in varL:
   #         settingsdir = expsdest +  graphsfile + '/' + nodes + la#bels + neigh + '/'
settingsdir = expsdest + graphsfile + '/' 
if not os.path.exists(settingsdir):
    os.makedirs(settingsdir)
scriptFile = open(settingsdir+'/run.sh', 'w')
scriptFile.write('#!/bin/bash\n')
#scriptFile.write('#PBS -q highmem') 
scriptFile.write('#PBS -l walltime=60:00:00\n')
scriptFile.write('#PBS -l nodes=1:ppn=4\n')
#scriptFile.write('#PBS -l mem=30gb\n')
scriptFile.write('#PBS -q highmem\n') 
#scriptFile.write('startTime=$(date +%s)\n')
bincmd = 'zerotemp allop.txt 2>&1'  
scriptFile.write('cd '+settingsdir+'\n')
scriptFile.write(bincmd+'\n')                  
#scriptFile.write('wait\n')
#scriptFile.write('endTime=$(date +%s)\n')
#scriptFile.write('runtime=$(( $endTime - $startTime ))\n')
scriptFile.close()
                                       
#copy binary from relevant directory
dirforbinsrc = binarydest 
srcfilepath = dirforbinsrc + 'zerotemp'
destfilepath = settingsdir + 'zerotemp'
shutil.copy(srcfilepath, destfilepath)

            #copy the job script for the batch system
            #srcfilepathjob = expsdest + 'run.sh'
            #destfilepathjob = settingsdir + 'run.sh'
            #shutil.copy(srcfilepathjob, destfilepathjob)

            #submit the job to the batch system after changing directory
            #os.chdir(settingsdir)
            #os.system('qsub run.sh -e run.err -o run.out')
            #os.system(runzerotemp)
os.chmod(settingsdir+'/run.sh', 00700)
os.chdir(settingsdir)
os.system('qsub run.sh -e run.err -o run.out')
                             
