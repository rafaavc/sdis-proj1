#!/bin/sh

find -name "*.java" > sources.txt
rm -rf gen
mkdir -p gen
javac -d gen @sources.txt
rm sources.txt

