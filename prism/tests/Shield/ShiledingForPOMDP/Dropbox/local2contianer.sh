# 4b2ca477743c
#8f77a7641b3f

#docker cp  ./a.txt $1:/opt/experiments/
docker cp  ./test.sh $1:/opt/experiments/
#docker cp ./storm/Model.cpp $1:/opt/storm/src/storm/models/sparse/
#docker cp ./storm/Pomdp.cpp $1:/opt/storm/src/storm/models/sparse/
#docker cp ./storm/Pomdp.h $1:/opt/storm/src/storm/models/sparse/

docker cp ./storm/storm-pomdp.cpp $1:/opt/storm/src/storm-pomdp-cli/


docker cp  ./updatShield.sh $1:/opt/experiments/
docker cp  ./updatGridStorm.sh $1:/opt/experiments/
docker cp ./updatStorm.sh $1:/opt/experiments/

docker cp  ./rlshield/model_simulator.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/shield.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/translate.py $1:/opt/rlshield/rlshield/
docker cp  ./rlshield/record_trace.py $1:/opt/rlshield/rlshield/

#docker cp  ./traces/trace1.txt $1:/opt/rlshield/examples/
docker cp ./files/obstacle/N6/obstacle_6_factored.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/obstacle/N6/obstacle_6_centralized.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp ./files/obstacle/N50/obstacle_50_factored.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/obstacle/N50/obstacle_50_centralized.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp ./files/obstacle/N100/obstacle_100_factored.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/obstacle/N100/obstacle_100_centralized.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp ./files/obstacle/N1000/obstacle_1000_factored.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/obstacle/N1000/obstacle_1000_centralized.nm $1:/opt/gridstorm/gridstorm/models/files/


docker cp ./files/obstacle/N1000/obstacle_1000_centralized.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp ./files/rock/N8R4/rocks_N8R4.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/rock/N8R4/rocks_N8R4_0_0_3_3.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/rock/N8R4/rocks_N8R4_0_4_3_7.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/rock/N8R4/rocks_N8R4_4_0_3_7.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/rock/N8R4/rocks_N8R4_4_4_7_7.nm $1:/opt/gridstorm/gridstorm/models/files/
docker cp ./files/rock/N8R4/rocks_N8R4_exit.nm $1:/opt/gridstorm/gridstorm/models/files/

#docker cp ./files/abstractA.prism $1:/opt/gridstorm/gridstorm/models/files/

#docker cp ./files/abstractB.prism $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/concrete.prism $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/abstractA.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/abstractB.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/concrete.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/obstacleA.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/obstacleB.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/obstacleC.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/obstacleE.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/obstacleF.nm $1:/opt/gridstorm/gridstorm/models/files/

#docker cp ./files/rocks2_test_720/rocks2A.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/rocks2_test_720/rocks2B.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/rocks2_test_720/rocks2C.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/rocks2_test_720/rocks2D.nm $1:/opt/gridstorm/gridstorm/models/files/
#docker cp ./files/rocks2_test_720/rocks2Base.nm $1:/opt/gridstorm/gridstorm/models/files/

docker cp ./gridstorm/__init__.py $1:/opt/gridstorm/gridstorm/models/

docker cp  ./comp_rocks.sh $1:/opt/experiments/comp_rocks.sh 
docker cp  ./computeW.sh $1:/opt/experiments/computeW.sh 
docker cp  ./computeW_Obstacle_N.sh $1:/opt/experiments/computeW_Obstacle_N.sh 
docker cp  ./computeW_Obstacle_N_ShieldSizeX_ShieldSizeY.sh $1:/opt/experiments/computeW_Obstacle_N_ShieldSizeX_ShieldSizeY.sh 

docker cp  ./p.sh $1:/opt/experiments/p.sh 

#d4576eb1d88c
# docker cp  ./rlshield/record_trace.py d4576eb1d88c:/opt/rlshield/rlshield/
#cat /opt/gridstorm/gridstorm/models/files/rocks2A
