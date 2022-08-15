#!/bin/bash         

STORM_POMDP="/Users/shengsheng/Downloads/github/storm-test/storm/build/bin/storm-pomdp"

#MODEL_DIR="/Users/shengsheng/Dropbox/files/rock/N8R6"
MODEL_DIR="/Users/shengsheng/Dropbox/files/obstacle/N10"
TO=99998000
TIMEOUTCOMMAND="timeout $TO"

MODEL_FILE=$(ls $MODEL_DIR/*factor*.nm)
for path_model in $MODEL_FILE; do
  model=$(basename $path_model)
  # echo $model
  $STORM_POMDP --prism ${MODEL_DIR}/${model} --prop "Pmax=? [\"notbad\" U \"goal\"]"\
  --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats \
  --exportwinningregion "${MODEL_DIR}/winningregion/${model%.*}-fixpoint.wr" &> "${MODEL_DIR}/log/${model%.*}-fixpoint.wr.log"
done

# model=rocks_N8R6_factored-4-0-0-3-3.nm
# $STORM_POMDP --prism ${MODEL_DIR}/${model} --prop "Pmax=? [\"notbad\" U \"goal\"]"\
#   --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats \
#   --exportwinningregion "${MODEL_DIR}/winningregion/${model%.*}-fixpoint.wr" &> "${MODEL_DIR}/log/${model%.*}-fixpoint.wr.log"

# rocks_N8R6_factored-4-0-0-3-3.nm
# rocks_N8R6_factored-4-0-4-3-7.nm
# rocks_N8R6_factored-4-4-0-7-3.nm
# rocks_N8R6_factored-4-4-4-7-7.nm

# MODEL_DIR="/Users/shengsheng/Dropbox/files/original"
# #/files/rock/N8R2/

# TO=99998000
# echo "Time out is $TO seconds"

# # model=rocks_N8R2_centralized
# # # $TIMEOUTCOMMAND $STORM_POMDP --prism $MODEL_DIR/$model.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  \
# # #   --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats \
# # #   --exportwinningregion "winningregion/$model-fixpoint.wr" &> logfiles/iterative/$model-fixpoint.wr.log

# # timeout 999 /Users/shengsheng/Downloads/github/storm-test/storm/build/bin/storm-pomdp \
# # --prism /Users/shengsheng/Dropbox/files/rock/N8R2/rocks_N8R2_centralized.nm -const N=8 --prop "Pmax=? [\"notbad\" U \"goal\"]"  \
# #   --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats \
# #   --exportwinningregion "winningregion/model-fixpoint.wr" &> logfiles/iterative/model-fixpoint.wr.log


# /Users/shengsheng/Downloads/github/storm-test/storm/build/bin/storm-pomdp --prism /Users/shengsheng/Dropbox/files/original/avoid.nm -const N=7,RADIUS=4 --prop "Pmax=? [\"notbad\" U \"goal\"]"  \
#   --buildstateval --build-all-labels --qualitative-analysis --memlesssearch iterative --winningregion -stats 
#   --exportwinningregion "winningregion/model-fixpoint.wr" &> logfiles/iterative/model-fixpoint.wr.log