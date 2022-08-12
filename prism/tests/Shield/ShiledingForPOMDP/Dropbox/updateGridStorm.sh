#vim /opt/storm/src/storm-pomdp/analysis/WinningRegionQueryInterface.cpp

#vim /opt/storm/src/storm/storage/BitVector.cpp
#cp ./gridstorm/__init__.py /opt/gridstorm/gridstorm/models/

for model in ./files/rock/N8R2/*.nm; do
	cp $model /opt/gridstorm/gridstorm/models/files/ 
done
#cd /opt/gridstorm/
#python /opt/gridstorm/setup.py install
