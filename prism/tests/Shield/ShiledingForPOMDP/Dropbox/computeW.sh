#!/bin/bash         
# S1= /opt/storm/build
# /opt/gridstorm/gridstorm/models/files/= /opt/gridstorm/gridstorm/models/files/
# 900= 900

if ! [ -e "/opt/storm/build" ]; then
  echo "/opt/storm/build not found" >&2
  exit 1
fi
if ! [ -d "/opt/storm/build" ]; then
  echo "/opt/storm/build not a directory" >&2
  exit 1
fi
if ! [ -e "/opt/gridstorm/gridstorm/models/files/" ]; then
  echo "/opt/gridstorm/gridstorm/models/files/ not found" >&2
  exit 1
fi
if ! [ -d "/opt/gridstorm/gridstorm/models/files/" ]; then
  echo "/opt/gridstorm/gridstorm/models/files/ not a directory" >&2
  exit 1
fi

if ! [ -e "logfiles/iterative" ]; then
  echo "logfiles/iterative not found. Please create directory" >&2
  exit 1
fi

if ! [ -e "winningregion" ]; then
  echo "winningregion not found. Please create directory" >&2
  exit 1
fi

# Pass the location in which storm is found:
echo "Storm build folder is assumed to be found at: /opt/storm/build"
STORM_BUILD_DIR="/opt/storm/build"
STORM_POMDP="${STORM_BUILD_DIR}/bin/storm-pomdp"
echo "Storm-binary: $STORM_POMDP"

echo "Models expected at folder /opt/gridstorm/gridstorm/models/files/"
MODEL_DIR=/opt/gridstorm/gridstorm/models/files/

TO=9000
echo "Time out is $TO seconds"
TIMEOUTCOMMAND="timeout $TO"

#concrete
#echo "Running test benchmarks..."

#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/$1.nm -const N=$2,primaryMinX=$3,primaryMinY=$4,primaryMaxX=$5,primaryMaxY=$6 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/$1-$2-$3-$4-$5-$6-fixpoint.wr" &> logfiles/iterative/$1-$2-$3-fixpoint.log
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_6_centralized.nm -const N=$2 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_6_centralized-$2-$3-$4-$5-$6-fixpoint.wr" &> logfiles/iterative/$1-$2-fixpoint.log
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle.nm -const N=6 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle-6-fixpoint.wr" &> logfiles/iterative/dfixpoint.log

$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_6_centralized.nm -const N=6 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_6_centralized-fixpoint.wr" &> logfiles/iterative/obstacle_6_centralized-fixpoint.wr.log
$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_50_centralized.nm -const N=50 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_6_centralized-fixpoint.wr" &> logfiles/iterative/obstacle_50_centralized-fixpoint.wr.log
$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_100_centralized.nm -const N=100 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_6_centralized-fixpoint.wr" &> logfiles/iterative/obstacle_100_centralized-fixpoint.wr.log
$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_1000_centralized.nm -const N=1000 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_6_centralized-fixpoint.wr" &> logfiles/iterative/obstacle_1000_centralized-fixpoint.wr.log

N=$2

shieldSizeX=3
shieldSizeY=3
maxX=0
maxY=0

minX=0
while [ "$minX" -lt "$N" ];do
  minY=0
  while [ "$minY" -lt "$N" ];do
    maxX=`expr $minX + $shieldSizeX - 1`
    maxY=`expr $minY + $shieldSizeY - 1`
    echo $minX $minY $maxX $maxY
    $TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/$1.nm -const N=$2,primaryMinX=$minX,primaryMinY=$minY,primaryMaxX=$maxX,primaryMaxY=$maxY --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/$1-$2-$minX-$minY-$maxX-$maxY-fixpoint.wr" &> logfiles/iterative/$1-$2-$minX-$minY-$maxX-$maxY-fixpoint.log
    minY=`expr $minY + $shieldSizeY`
  done
  minX=`expr $minX + $shieldSizeX`
done
