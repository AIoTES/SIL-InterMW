#docker build -t interiot-opensplice .
#docker run -d --hostname interiot-opensplice --name interiot-opensplice -v data:/opt/data interiot-opensplice

FROM relateiq/oracle-java8

COPY copy/* /opt/

RUN apt-get update -y && \
    apt-get install -y \
    vim \
    zip \
    locate && \
    cd /opt && \
    chmod +x *.sh && \
    tar -zxf *.tar.gz && \
    sed -i "s|@@INSTALLDIR@@|$PWD|g" HDE/x86_64.linux/release.com && \
    ulimit -c unlimited

CMD ["/opt/init.sh"]