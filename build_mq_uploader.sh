#!/bin/bash

BUILD_FOLDER=metering-upload/target
CONFIG_FOLDER=metering-upload/src/main/launch4j
LAUNCH4J_FOLDER=$BUILD_FOLDER/launch4j
DOWNLOAD_PATH=https://sourceforge.net/projects/launch4j/files/launch4j-3/3.9

if [ ! -d $LAUNCH4J_FOLDER ]; then
    mkdir $LAUNCH4J_FOLDER

    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        echo "Downloading launch4j-3.9-linux.tgz"
        curl --fail --location --retry 3 \
            $DOWNLOAD_PATH/launch4j-3.9-linux.tgz/download | tar xz -C $LAUNCH4J_FOLDER
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "Downloading launch4j-3.9-macosx-x86.tgz"
        curl --fail --location --retry 3 \
            $DOWNLOAD_PATH/launch4j-3.9-macosx-x86.tgz/download | tar xz -C $LAUNCH4J_FOLDER
    fi
fi

echo "Building EXE file"
sh metering-upload/target/launch4j/launch4j/launch4j metering-upload/src/main/launch4j/launch4j.xml
