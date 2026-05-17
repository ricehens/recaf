#!/bin/bash
# set to path of jar
java -jar $(dirname -- "$(readlink -f -- "$BASH_SOURCE")")/build/libs/recaf-1.0.1.jar "$@"

