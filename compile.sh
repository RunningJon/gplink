#!/bin/bash
set -e
echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: ExternalData" >> manifest.txt
echo "Specification-Title: \"PivotalGuruGPLink\"" >> manifest.txt
echo "Specification-Version: \"1.0\"" >> manifest.txt
echo "Created-By: 1.6.0_65-b14-462-11M4609" >> manifest.txt
d=`date`
echo "Build-Date: $d" >> manifest.txt

javac -Xbootclasspath:/usr/java/jdk1.6.0_45/jre/lib/rt.jar -source 1.6 -target 1.6 *.java
jar cfm PivotalGuruGPLink.jar manifest.txt ExternalData.class CommonDB.class SQLFile.class TargetData.class Listener.class Validation.class GPLink.class
mv PivotalGuruGPLink.jar jar/
