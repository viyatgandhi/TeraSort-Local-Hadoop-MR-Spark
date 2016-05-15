# TeraSort-Local-Hadoop-MR-Spark

Refer http://sortbenchmark.org/

Three version of program are made. 

input file is: 

One is local one, where program uses external sort to sort the data and uses im-memory data-stucture.

Hadoop-MR and Spark uses 16 nodes of d2.xlarge on aws to sort the 1 TB data.

For deployment use the script in hadoopsetup repo while follow manual steps for Spark. 
