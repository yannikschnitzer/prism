docker cp $1:/opt/experiments/a.txt ./
docker cp $1:/opt/experiments/ ./
docker cp $1:/opt/experiments/test.sh ./

docker cp $1:/opt/experiments/updatStorm.sh ./
docker cp $1:/opt/experiments/updatGridStorm.sh ./
docker cp $1:/opt/experiments/updatShield.sh ./

docker cp $1:/opt/rlshield/rlshield/model_simulator.py ./rlshield/
docker cp $1:/opt/rlshield/rlshield/shield.py ./rlshield/


docker cp $1:/opt/experiments/computeW.sh ./
docker cp $1:/opt/gridstorm/gridstorm/models/files/ ./
docker cp $1:/opt/experiments/translate.txt ./
docker cp $1:/opt/experiments/winningregion/ ./
docker cp $1:/opt/experiments/newvideos/ ./
docker cp $1:/opt/experiments/translation/ ./


chmod -R 777 .


#sudo docker cp d4576eb1d88c:/opt/experiments/newvideos/ ./
