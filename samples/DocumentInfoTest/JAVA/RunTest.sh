#!/bin/sh

TEST_NAME=DocumentInfoTest
JAR_NAME=compdfkit-1.0-SNAPSHOT.jar

CLSPATHDIR=$(pwd)
echo $CLSPATHDIR

cd $(pwd)/../../
ROOTDIR=$(pwd)
echo ${ROOTDIR}

LIB_DIR=$(pwd)/lib
echo $LIB_DIR

cd $CLSPATHDIR
javac -cp $LIB_DIR/$JAR_NAME *.java
java -Djava.library.path=LIB_DIR -cp .:$LIB_DIR/$JAR_NAME $TEST_NAME $ROOTDIR

rm -rf $ROOTDIR/out
rm -rf $ROOTDIR/compdfkit
rm -rf compdfkit
rm -rf *.class
