#vim /opt/storm/src/storm-pomdp/analysis/WinningRegionQueryInterface.cpp
#vim /opt/storm/src/storm-pomdp/analysis/WinningRegion.cpp
#vim /opt/storm/src/storm/storage/BitVector.cpp
module load java
cd /u/ss7dr/prism-pomdp/prism/prism
make JAVA_DIR=/sw/centos-7.4/java/current/
cd /u/ss7dr/prism-pomdp/prism/prism/tests/Shield/ShiledingForPOMDP/Dropbox

#/u/ss7dr/prism-pomdp/prism/prism/bin/prism *centra* -const N=8 ../../safe.props -prop 2 -exportmodel a.all -pomcpverbose 0