#!/bin/bash
# set to path of jar
/home/ericshen/.sdkman/candidates/java/25.0.3-graal/bin/java -jar $(dirname -- "$(readlink -f -- "$BASH_SOURCE")")/build/libs/recaf-1.1.0.jar "$@"

