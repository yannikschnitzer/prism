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

TO=90
echo "Time out is $TO seconds"
TIMEOUTCOMMAND="timeout $TO"

#concrete
#echo "Running test benchmarks..."
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/test.nm -const N=6 --prop "Pmax=? [\"notbad\" U \"goal\"]" --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative -stats --exportwinningregion "winningregion/test-6-initial.wr" &> logfiles/iterative/test-6-initial.log

#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/concrete.prism --prop "Pmax=? [\"notbad\" U \"goal\"]" --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative -stats --exportwinningregion "winningregion/concrete-initial.wr" &> logfiles/iterative/concrete-initial.log

$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/$1.nm --prop "Pmax=? [\"notbad\" U \"goal\"]" --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative -stats --exportwinningregion "winningregion/$1-initial.wr" &> logfiles/iterative/$1-initial.log

#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/abstract_1.nm --prop "Pmax=? [\"notbad\" U \"goal\"]" --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative -stats --exportwinningregion "winningregion/abstract_1-initial.wr" &> logfiles/iterative/abstract_1-initial.log

#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/abstract_2.nm --prop "Pmax=? [\"notbad\" U \"goal\"]" --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative -stats --exportwinningregion "winningregion/abstract_2-initial.wr" &> logfiles/iterative/abstract_2-initial.log

#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/obstacle.nm -const N=6 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/obstacle-6-fixpoint.wr" &> logfiles/iterative/obstacle-6-fixpoint.log

