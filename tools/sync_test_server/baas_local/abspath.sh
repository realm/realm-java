#!/bin/bash

PLATFORM=$(uname -s)
case $(uname -s) in
    Darwin)
        exec perl -e 'use File::Spec; print File::Spec->rel2abs(shift); print "\n"' $1
        ;;
    CYGWIN*)
        exec cygpath -am $1
        ;;
    *)
        exec realpath -s $1
        ;;
esac