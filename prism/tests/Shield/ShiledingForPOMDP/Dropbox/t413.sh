#!/bin/bash         
shieldSizeX=2
shieldSizeY=2
maxX=0
maxY=0
minX=0
minY=0
N=$2
while [ "$minX" -lt "$N" ];do
	minY=0
  while [ "$minY" -lt "$N" ];do
    echo $minX $minY
    minY=`expr $minY + $shieldSizeY`
  done
  minX=`expr $minX + $shieldSizeX`
done
t=0
for((i=1;i<10;i+=$shieldSizeX));
do
	for((j=1;j<10;j+=$shieldSizeY));
	do
		t=`expr $i / 2`
		echo $i $j $t
	done
done