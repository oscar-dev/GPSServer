#!/bin/bash

export BASE_DIR=/home/socket/daemon/
export BIN_DIR=$BASE_DIR/bin
export LIB_DIR=$BASE_DIR/lib

#export CLASSPATH=$LIB_DIR/mysql-connector-java-5.1.23-bin.jar:$LIB_DIR/log4j-api-2.11.0.jar:$LIB_DIR/log4j-core-2.11.0.jar

nohup java -Djava.net.preferIPv4Stack=true -Dlog4j.configurationFile=$BIN_DIR/log4j2.xml -jar GPSServer.jar &

