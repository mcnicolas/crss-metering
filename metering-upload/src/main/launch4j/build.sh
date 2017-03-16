#!/bin/bash

BUILD_FOLDER=build
LAUNCH4J_FOLDER=$BUILD_FOLDER/launch4j

if [ ! -d $BUILD_FOLDER ]; then
    mkdir $BUILD_FOLDER
fi

if [[ ! -d $LAUNCH4J_FOLDER ]]; then
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        echo "Downloading launch4j-3.9-linux.tgz"
        curl --fail --location --retry 3 \
            https://sourceforge.net/projects/launch4j/files/launch4j-3/3.9/launch4j-3.9-linux.tgz/download | tar xz -C $BUILD_FOLDER
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "Downloading launch4j-3.9-macosx-x86.tgz"
        curl --fail --location --retry 3 \
            https://sourceforge.net/projects/launch4j/files/launch4j-3/3.9/launch4j-3.9-macosx-x86.tgz/download | tar xz -C $BUILD_FOLDER
    fi
fi

echo "Building EXE file"
sh build/launch4j/launch4j launch4j.xml
