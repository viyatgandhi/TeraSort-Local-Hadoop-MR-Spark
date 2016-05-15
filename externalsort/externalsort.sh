#!/bin/bash

echo "--INFO-- Genrating 1GB input.txt file"

./gensort -a 10000000 input.txt


echo "--INFO-- Starting External Sort for 1 to 8 threads"

for i in {1..8}
do
rm output.txt

echo ""
echo "--INFO-- Running External Sort for $i Threads"

l1=output
l2=Thread.log
name="$l1$i$l2"

java -Xms128m -Xmx2048m ExternalSortMulti input.txt output.txt $i | tee -a "$name"

echo ""
echo "Completed for $i Thread"

done