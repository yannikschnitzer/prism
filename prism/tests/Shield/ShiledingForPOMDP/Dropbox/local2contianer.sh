# 4b2ca477743c
docker cp  ./a.txt $1:/opt/experiments/
docker cp  ./test.sh $1:/opt/experiments/

docker cp  ./updatShield.sh $1:/opt/experiments/
docker cp  ./updatGridStorm.sh $1:/opt/experiments/
docker cp ./updatStorm.sh $1:/opt/experiments/

docker cp  ./rlshield/model_simulator.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/shield.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/translate.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/record_trace.py $1:/opt/rlshield/rlshield/

docker cp  ./traces/trace1.txt $1:/opt/rlshield/examples/

#docker cp ./files/abstractA.prism $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/abstractB.prism $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/concrete.prism $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/abstractA.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/abstractB.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/concrete.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./gridstorm/__init__.py $1:/opt/gridstorm/gridstorm/models/

docker cp  ./computeW.sh $1:/opt/experiments/computeW.sh 

#d4576eb1d88c
# docker cp  ./rlshield/record_trace.py d4576eb1d88c:/opt/rlshield/rlshield/
