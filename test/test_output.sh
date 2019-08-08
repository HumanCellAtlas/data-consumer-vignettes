#!/bin/sh
# test/test_output.sh 
test $(grep -oc '"outputs": ' "$1") -eq $(grep -oc '"cell_type": "code"' "$1")
if [ $? -gt 0 ]; then
    echo "FAIL: Was output committed with this notebook? $1"
fi
exit 0