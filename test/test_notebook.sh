#!/bin/sh
# test/test_output.sh path_to_notebook
# See ReviewNB/treon#12 
DIRECTORY=$(dirname "$1")
cd "$DIRECTORY"
treon
