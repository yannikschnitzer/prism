# 4b2ca477743c
docker cp  ./a.txt $1:/opt/experiments/
docker cp  ./test.sh $1:/opt/experiments/

docker cp  ./updatShield.sh $1:/opt/experiments/
docker cp  ./updatGridStorm.sh $1:/opt/experiments/
docker cp ./updatStorm.sh $1:/opt/experiments/

docker cp  ./model_simulator.py $1:/opt/rlshield/rlshield/

docker cp ./files/obstacle.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp  ./computeW.sh $1:/opt/experiments/computeW.sh 

