#!/bin/sh

find -name "*.java" > sources.txt
mkdir -p gen
mkdir -p filesystem
javac -d gen @sources.txt
rm sources.txt