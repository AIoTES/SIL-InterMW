#/bin/bash

docker build -t interiot-opensplice . 2>/dev/null
docker run -d -it --hostname interiot-opensplice --name interiot-opensplice interiot-opensplice 2>/dev/null

if [ $? != 0 ]; then
    docker start interiot-opensplice
fi