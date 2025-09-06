# Solution Summary

This lab required modifying an existing .java file called 'WordCount1' into being able to count URLs instead of merely Words from two input Wikipedia articles, generating summaries of these counts into output files, and running both of these java files in a distributed computing system within a cloud environment in order to understand how to create and modify clusters and how changing the number of worker nodes might be able to affect performance of applications. To accomplish this, firstly the appropriate changes were made to WordCount1.java and a new file called UrlCount.java was created, and ran on the CU University coding page in a Jupyter Lab environment, to ensure that UrlCount worked as intended. After verifying this, a Git repo was created/forked (https://github.com/WiHi1131/lab2-url-lister), and all relevant code/changes were pushed to the repo. Then, a cluster was spun-up using Dataproc within the Google Cloud Shell, initially with one master node and two workers. Private Google Access and a Cloud NAT had to be configured in order to clone the Git repo containing the code onto the master node. In order to run the makefile necessary to run both WordCount and UrlCount, an UnsupportedClassVersionError had to be resolved, since these files had initially been run and ".jar" files had been created within JupyterLab, which used JDK version 17, while dataproc uses Java 11, an older version. Rather than altering the makefile itself, commands were input into the Google Cloud Shell that rebuilt the code on the cluster with the correct version of java (javac --release 11 -classpath "$(hadoop classpath)"). Using the command: "time -p hadoop jar UrlCount.jar UrlCount input output_urlcount" on the Google Cloud Shell (changing urlcount to wordcount1 as appropriate), the files were then able to be run and the time that they took to execute and output results was tracked for each application. Results were verified in the Shell output with the command: "hdfs dfs -cat output_urlcount/part*". Outputs for each run of each application were saved in text files and included in the repo. These commands were repeated and times also tracked after updating the cluster from 2 workers to 4, so comparisons could be made regarding the time it took to run these files on the cluster. 

# Performance Comparisons

The timed results are shown below. Real indicates the actual time in seconds the job took to run, while user/sys detail CPU time consumed by the client process that launched the job, and are useful for showing overhead, and are not necessarily important for demonstrating how increasing parallelism can improve performance: 

Time in seconds from running WordCount on a cluster with 2 workers was as follows: 
real 38.32
user 8.84
sys 0.48

Time in seconds from running WordCount on a cluster with 4 workers was as follows: 
real 37.29
user 8.70
sys 0.49

There is only about 1 second of difference in time when the job was run with 4 workers vs 2. This effectively indicates that there was no improvement. This likely happened because of the very small size of the dataset (less than 1 mb total). 

Time in seconds from running UrlCount on a cluster with 2 workers was as follows: 
real 42.80
user 8.73
sys 0.57

Time in seconds from running UrlCount on a cluster with 4 workers was as follows: 
real 34.67
user 8.55
sys 0.47

We see a speedup of just over 8 seconds when doubling worker count for UrlCount. This may be because the map/reduce logic parses more strings per record than the WordCount application. Because there was more computing work to do in this application, increasing parallelization increased efficiency and resulted in a faster runtime. Examining the .txt output files also shows that we had more substantial shuffling in UrlCount than in WordCount, which also reveals that spreading these tasks across twice the number of workers helped substantially more than in WordCount. We can conclude that larger workloads likely benefit much more from increased parallelization than smaller ones. 

# Software Required

To achieve this solution, we required the following software and tools to be used: 
- Google Cloud Platform
- Dataproc (within the GCP)
- The Hadoop Ecosystem, including:
      - YARN for job resource management
      - HDFS
      - the Hadoop MapReduce framework
- Java
- Javac (the Java Compiler)
- JAR files
- Git & Github
- Linux/CLI Tools
- Python
- JupyterLab (via coding.csel)

# Resources

The main resource used was ChatGPT 5 for resolving errors, debugging, and code generation. 



