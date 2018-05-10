#!/usr/bin/env bash

# This script prints the name of the current Git branch since it sometimes isn't easily
# available on Jenkins and pipes doesn't work well with Jenkins shell support.

set -euo pipefail
git branch | grep \* | cut -f 2 -d " "
