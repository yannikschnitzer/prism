cd /opt/experiments
#python /opt/rlshield/rlshield/shield.py -m rocks --constants N=6 -wr precomputed_winningregions/rocks-6-initial.wr -s 150 -N 1  --finishers-only --title "Rocks (6) with 'initial' shield" --logfile logfiles/rendering/rocks-6-initial.log

#python /opt/rlshield/rlshield/shield.py -m rocks --constants N=6 -wr precomputed_winningregions/rocks-6-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Rocks (6) with 'initial' shield" --logfile logfiles/rendering/rocks-6-initial.log

python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=4 -wr winningregion/obstacle-4-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (4) with 'initial' shield" --logfile logfiles/rendering/obstacke-4-initial.log

python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=4 -wr winningregion/obstacle-4-fixpoint.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (4) with 'initial' shield" --logfile logfiles/rendering/obstacke-4-fixpoint.log


python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=2 -wr winningregion/obstacle-2-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (4) with 'initial' shield" --logfile logfiles/rendering/obstacke-2-initial.log

python /opt/rlshield/rlshield/shield.py -m obstacle --constants N=2 -wr winningregion/obstacle-2-fixpoint.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Obstacle (4) with 'initial' shield" --logfile logfiles/rendering/obstacke-2-fixpoint.log

#python -m pdb /opt/rlshield/rlshield/shield.py -m rocks --constants N=6 -wr precomputed_winningregions/rocks-6-initial.wr -s 150 -N 1 --video-path newvideos/ --finishers-only --title "Rocks (6) with 'initial' shield" --logfile logfiles/rendering/rocks-6-initial.log
