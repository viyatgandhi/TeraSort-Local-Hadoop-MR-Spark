To run Spark program for 1 GB 

export AWS_SECRET_ACCESS_KEY
export AWS_ACCESS_KEY_ID

go to folder spark/ec2/ and below command to start Spark cluster

./spark-ec2 -k awskey -i awskey.pem -t d2.xlarge -s 1 --spot-price=0.40 launch Spark 

Login to master and go to root  

Generate file from gensort and put it to hdfs using command 

--> /root/ephemeral-hdfs/bin/hadoop fs -mkdir /user
--> /root/ephemeral-hdfs/bin/hadoop fs -mkdir /user/root
--> /root/ephemeral-hdfs/bin/hadoop fs -put input.txt input.txt

Launch spark-shell  from root/spark/bin/spark-shell 

And run three commands as given in code below to get the output

Once done get the file from hdfs and do valsort 

--> root/ephemeral-hdfs/bin/hadoop fs -get output/* .

As for 1 TB we need to change serveral parameter

Launch spark cluster with 16 or 1 node and perform below tasks to configure spark for 1 TB and raid0 disk

go to /root/spark/sbin and stop spark 
--> ./stop-all.sh

go to /root/ephemeral-hdfs/bin and stop hdfs 
--> ./stop-dfs.sh

Download repo and change raid script 
copy raid script to /root/ephemeral-hdfs/conf
so that it sync to all slaves

run raid script for master

go to /root/spark/conf

edit spark-env.sh 
export SPARK_LOCAL_DIRS="/mnt/raid/spark"

and edit spark-defaults.conf and add below parameter 
spark.local.dir /mnt/raid/tmp

edit core-site.xml

  <property>
    <name>hadoop.tmp.dir</name>
    <value>/mnt/raid/ephemeral-hdfs</value>
  </property>


go to /root/ephemeral-hdfs/conf

edit hdfs-site.xml

change replication to 1

  <property>
    <name>dfs.data.dir</name>
    <value>/mnt/raid/ephemeral-hdfs/data</value>
  </property>

edit core-site.xml

  <property>
    <name>hadoop.tmp.dir</name>
    <value>/mnt/raid/ephemeral-hdfs</value>
  </property>

go to /root/spark-ec2

run below command to RSYNC the above parameter copy to all slave

./copy-dir  /root/ephemeral-hdfs/conf
./copy-dir  /root/spark/conf

Now go to each slave and run same raid script which was edited in previous script.

go to /root/ephemeral-hdfs/bin

--> ./hadoop namenode -format


Come to master and run below command to start hdfs and spark cluster

and run script 

/root/ephemeral-hdfs/bin 
--> ./start-dfs.sh

/root/spark/sbin 
--> start all

check with jps command you can see below item running 

Master

SecondaryNameNode

TachyonMaster

Jps

NameNode

Generate 1 TB data and put it to hdfs  

run ./spark-shell and run below command to sort 1 TB data

type ":paste"
and copy paste below code 
and press Ctrl+D to start the execution 

val lines = sc.textFile("hdfs://ec2-52-36-141-162.us-west-2.compute.amazonaws.com:9000/user/root/input1TB.txt")
val sort = lines.map(_.split("  ")).map(arr => (arr(0) + "  " + arr(1),arr.mkString("  "))).sortByKey().map(_._2)
sort.saveAsTextFile("hdfs://ec2-52-36-141-162.us-west-2.compute.amazonaws.com:9000/user/root/output")

Explanation of code:

-> file is taken from hdfs using sc.textFile
-> than it is mapped and differentiated using double space and again map is used for other other cases well key contains doubel space.
-> after taht mkstring is used to get final key and value.
-> then used sort by key function to sort the keys
-> used a custom mapping function for saveAsTextFile as output by sortByKey will have in tuples-to convert into string 
-> use saveAsTextFile to write it to hdfs which in turn will use above map function to write.

Note: Spark took around 179 minutes for 1 TB 16 node program. But I got some error on stage 2 (you can find in screenshot node_16_16.png). 
And it recomputed some of the tasks which took around 90-100 minutes (I don’t know why it was re-computing the task again. It happened for 3-4 times. 
I was going to try the experiment again but instance got lost although I had bid $0.70). 
So I have considered total time taken by Spark as 179-100 = 79 minutes.  