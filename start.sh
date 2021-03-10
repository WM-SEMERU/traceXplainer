#! /bin/sh

PORT=$1
TAG=T-Miner

if [ $# -eq 2 ]; then
	if [ "$2" = "--build" ]; then
		# Build the docker container
		docker build -t $TAG .
	fi
fi


# Run the docker container. Add additional -v if
# you need to mount more volumes into the container
# Also, make sure to edit the ports to fix your needs.
#docker run -d --gpus all -v $(pwd):/tf/main \
#	-v /mnt/data/ds4se:/tf/data \
#	-p 8004:8888  $TAG
docker run --gpus all -d -v /mnt/data/ds4se:/tf/data  -v $(pwd):/tf/main -p $PORT:8888 --name $TAG-$(whoami) $TAG
