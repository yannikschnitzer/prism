cd /opt/experiments
#python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=6 -wr  winningregion/obstacle-6-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (6) with 'initial' shield" --logfile logfiles/rendering/obstacke-6-initial.log
#python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=6 -wr  winningregion/obstacle-6-1-fixpoint.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (6) with 'fixpoint' shield" --logfile logfiles/rendering/obstacke-6-fixpoint.log
#python /opt/rlshield/rlshield/record_trace.py -m obstacle --constants N=6 -wr  winningregion/obstacle-6-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (6) with 'initial' shield" --logfile logfiles/rendering/obstacke-6-initial.log
#python /opt/rlshield/rlshield/translate.py -m concrete --constants N=6
#python /opt/rlshield/rlshield/translate.py -m abstractA --constants N=6
#python /opt/rlshield/rlshield/translate.py -m abstractB --constants N=6
#python /opt/rlshield/rlshield/translate.py -m rocks --constants N=4
python /opt/rlshield/rlshield/translate.py -m obstacle --constants N=6 
python /opt/rlshield/rlshield/translate.py -m obstacleA --constants N=6 
python /opt/rlshield/rlshield/translate.py -m obstacleB --constants N=6 
python /opt/rlshield/rlshield/translate.py -m obstacleC --constants N=6 
python /opt/rlshield/rlshield/translate.py -m obstacleE --constants N=6 
python /opt/rlshield/rlshield/translate.py -m obstacleF --constants N=6 
#python /opt/rlshield/rlshield/translate.py -m obstacleD --constants N=6 

#python /opt/rlshield/rlshield/translate.py -m rocks --constants N=6
#python /opt/rlshield/rlshield/translate.py -m evade --constants N=6,RADIUS=2
#python /opt/rlshield/rlshield/translate.py -m avoid --constants N=6,RADIUS=3 
#python /opt/rlshield/rlshield/translate.py -m intercept --constants N=7,RADIUS=1
#python /opt/rlshield/rlshield/translate.py -m refuel --constants N=6,ENERGY=8

#python /opt/rlshield/rlshield/shield.py -m obstacleE --constants N=6 -wr  winningregion/obstacleE-6-test201509-fixpoint.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (E) with 'initial' shield" --logfile logfiles/rendering/obstacke-6E-initial.log
python /opt/rlshield/rlshield/translate.py -m rocks2Base --constants N=6 
python /opt/rlshield/rlshield/translate.py -m rocks2A --constants N=6 
python /opt/rlshield/rlshield/translate.py -m rocks2B --constants N=6 
python /opt/rlshield/rlshield/translate.py -m rocks2C --constants N=6 
python /opt/rlshield/rlshield/translate.py -m rocks2E --constants N=6 
