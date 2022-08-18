#vim /opt/storm/src/storm-pomdp/analysis/WinningRegionQueryInterface.cpp
#vim /opt/storm/src/storm-pomdp/analysis/WinningRegion.cpp
#vim /opt/storm/src/storm/storage/BitVector.cpp
module load java
cd /u/ss7dr/prism-pomdp/prism/prism
git pull
make JAVA_DIR=/sw/centos-7.4/java/current/
cd /u/ss7dr/prism-pomdp/prism/prism/tests/Shield/ShiledingForPOMDP/Dropbox

#/u/ss7dr/prism-pomdp/prism/prism/bin/prism  -javamaxmem 64g -javastack 16g *centra* ../../safe.props -prop 2 -exportmodel a.all -pomcpverbose 0 -expconst 200 -nruns 10