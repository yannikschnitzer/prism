docker cp $1:/opt/experiments/a.txt ./
docker cp $1:/opt/experiments/test.sh ./

docker cp $1:/opt/experiments/updatStorm.sh ./
docker cp $1:/opt/experiments/updatGridStorm.sh ./
docker cp $1:/opt/experiments/updatShield.sh ./

docker cp $1:/opt/rlshield/rlshield/model_simulator.py ./
docker cp $1:/opt/experiments/computeW.sh ./


docker cp $1:/opt/gridstorm/gridstorm/models/files/ ./

docker cp $1:/opt/experiments/translate.txt ./
docker cp $1:/opt/experiments/winningregion/ ./
docker cp $1:/opt/experiments/newvideos/ ./
chmod 777 *
chmod 777 files/*
chmod 777 winningregion/*
chmod 777 newvideos/*
