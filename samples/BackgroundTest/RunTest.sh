#!/bin/sh

TEST_NAME=BackgroundTest

CLSPATHDIR=$(pwd)
echo $CLSPATHDIR

cd $(pwd)/..
ROOTDIR=$(pwd)
echo ${ROOTDIR}

if [ ! -d ${ROOTDIR}/out ]; then
    mkdir -p ${ROOTDIR}/out
else
    echo ${ROOTDIR}/out
fi

LIB_DIR=$(pwd)/lib
echo $LIB_DIR

JAR_NAME=$(ls "$LIB_DIR" | grep -E '^compdfkit-[0-9][0-9.]*\.jar$' | sort -V | tail -n 1)
if [ -z "$JAR_NAME" ]; then
    echo "Error: no compdfkit-*.jar found in $LIB_DIR"
    exit 1
fi
echo $JAR_NAME

COMMON_DIR=$ROOTDIR/common

cd $CLSPATHDIR
javac -cp $LIB_DIR/$JAR_NAME -d . $COMMON_DIR/CPDFLicense.java *.java
java -Djava.library.path=$LIB_DIR -cp .:$LIB_DIR/$JAR_NAME $TEST_NAME $ROOTDIR

rm -rf compdfkit
rm -rf *.class
