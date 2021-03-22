#!/bin/sh

find -name "*.java" > sources.txt
rm -rf gen
mkdir -p gen
mkdir -p filesystem
javac -d gen @sources.txt
rm sources.txt