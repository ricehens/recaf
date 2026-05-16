#!/bin/bash
gcc -c system.c -o system.o
gcc -c float.c -o float.o
gcc -c crt.c -o crt.o
ar rcs libsystem.a system.o 
ar rcs libfloat.a float.o 
ar rcs libcrt.a crt.o 
