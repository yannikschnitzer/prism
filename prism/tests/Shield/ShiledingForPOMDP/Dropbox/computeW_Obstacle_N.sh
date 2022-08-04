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
N=$1
$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle_$1_centralized.nm -const N=$1 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle_$1_centralized-fixpoint.wr" &> logfiles/iterative/obstacle_$1_centralized-fixpoint.wr.log

