#!/bin/sh
# test/test_output.sh 
echo $1
test $(grep -oc '"outputs": ' "$1") -eq $(grep -oc '"cell_type": "code"' "$1") || exit 1