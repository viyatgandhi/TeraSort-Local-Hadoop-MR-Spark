Complie java files

Note : tmp file size depends upon minimum size of jvm so you can adjust file size depending upon Xms value.

For 1 GB simply run the script  externalsort.sh 

It will run 1 GB sort for 1 to 8 threads

For 1 TB run the program manually as it takes too much time no script is prepared. 

Run the program with below arguments: 

java -Xms256m -Xmx29000m ExternalSortMulti input.txt output.txt <NoOfThreads>

It will create 128 MB file tmp files and it can use max of 29000 MB of heap as d2.xlarge has 31GB RAM.

If you are running program for 1 TB please edit below system level parameter as in merge phase
it will open more than 1024 files if size is less than 500 MB for tmp file. Other wise you will get 
error "too many files open" (which i got and sadly wasted more than 8+ hour on d2.xlarge )

open /etc/security/limits.conf with su 

edit and add below two lines 
* hard nofile 65536
* soft nofile 65536

relogin back to putty and run command 
--> ulimit -n 65536 
and run above java program

Explanation of code :

Proper inline comments are provided in code.
Below is the overview how programs works:

-> input file is passed through buffer reader for fast reading.
-> After that block size is determined for file size according to min jvm free size
-> each line is read from file and concurrently it calculates file size,
if file size reaches threashold then that list is passed to another class and thread is started.
That thread sorts the data using merge sort and writes it to temp file which has approx size of block size decided earlier.
-> This way line reading continues and thread keeps on getting started.
-> After one point when threads reaches say 4, it will wait for all threads to complete and again start reading.
-> After all the data is read and written into different temp files merge phase started.
-> All is read through buffer and added to priority queue.
-> PQ uses a custom comparator so that only first 10 lines are chekced for the lexigraphic comparision.
-> After files are added in PQ, we will have lowest value at the top of the file which will be written into final single file.
-> Each time a line is read and again added to PQ so that top the queue will have lowest value at the top.
-> This continuous till PQ becomes empty and all the files are added again. 