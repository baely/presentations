#!/bin/bash

# An absolute hack to run java code from golang.org/x/tools/present

cp /dev/stdin /tmp/runjava1.jsh
cat /tmp/runjava1.jsh | tail -n +4 > /tmp/runjava.jsh
/usr/bin/java --source 17 /tmp/runjava.jsh
rm /tmp/runjava.jsh /tmp/runjava1.jsh
