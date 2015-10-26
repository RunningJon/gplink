#!/bin/bash
set -e
echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: GPLink" >> manifest.txt
echo "Specification-Title: \"PivotalGuruGPLink\"" >> manifest.txt
echo "Specification-Version: \"1.0\"" >> manifest.txt
echo "Created-By: 1.6.0_65-b14-462-11M4609" >> manifest.txt
d=`date`
echo "Build-Date: $d" >> manifest.txt

javac GPLink.java
jar cfm PivotalGuruGPLink.jar manifest.txt GPLink.class
mv PivotalGuruGPLink.jar jar/
