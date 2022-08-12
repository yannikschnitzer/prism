
#docker cp ./storm/Model.cpp $1:/opt/storm/src/storm/models/sparse/
#docker cp ./storm/Pomdp.cpp $1:/opt/storm/src/storm/models/sparse/
#docker cp ./storm/Pomdp.h $1:/opt/storm/src/storm/models/sparse/

docker cp ./storm/storm-pomdp.cpp $1:/opt/storm/src/storm-pomdp-cli/
docker cp ./gridstorm/__init__.py $1:/opt/gridstorm/gridstorm/models/

for script in ./compute_script/*.sh; do
	docker cp $script $1:/opt/experiments/
done

for model in ./files/rock/N8R2/*.nm; do
	docker cp $model $1:/opt/gridstorm/gridstorm/models/files/ 
done
#docker cp  ./traces/trace1.txt $1:/opt/rlshield/examples/
#docker cp ./files/obstacle/N6/obstacle_6_factored.nm $1:/opt/gridstorm/gridstorm/models/files/

#d4576eb1d88c
# docker cp  ./rlshield/record_trace.py d4576eb1d88c:/opt/rlshield/rlshield/
#cat /opt/gridstorm/gridstorm/models/files/rocks2A
