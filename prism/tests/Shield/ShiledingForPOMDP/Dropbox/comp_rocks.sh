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

TO=18000
echo "Time out is $TO seconds"
TIMEOUTCOMMAND="timeout $TO"
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/rocks_N8R4_exit.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/rocks_N8R4_exit-fixpoint.wr" &> logfiles/iterative/rocks_N8R4_exit-fixpoint.wr.log
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/rocks_N8R4_0_0_3_3.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/rocks_N8R4_0_0_3_3-fixpoint.wr" &> logfiles/iterative/rocks_N8R4_0_0_3_3-fixpoint.wr.log
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/rocks_N8R4_0_4_3_7.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/rocks_N8R4_0_4_3_7-fixpoint.wr" &> logfiles/iterative/rocks_N8R4_0_4_3_7-fixpoint.wr.log
#$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/rocks_N8R4_4_0_3_7.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/rocks_N8R4_4_0_3_7-fixpoint.wr" &> logfiles/iterative/rocks_N8R4_4_0_3_7-fixpoint.wr.log
$TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/rocks_N8R4_4_4_7_7.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats --exportwinningregion "winningregion/rocks_N8R4_4_4_7_7-fixpoint.wr" &> logfiles/iterative/rocks_N8R4_4_4_7_7-fixpoint.wr.log
