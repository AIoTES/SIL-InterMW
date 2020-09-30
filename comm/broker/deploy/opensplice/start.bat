docker build -t interiot-opensplice .
docker run -d -it --hostname interiot-opensplice --name interiot-opensplice -v data:/opt/data interiot-opensplice