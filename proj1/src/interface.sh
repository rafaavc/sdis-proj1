#!/bin/sh

mkdir -p filesystem
cd filesystem
java -cp "../gen" BackupServiceInterface $@