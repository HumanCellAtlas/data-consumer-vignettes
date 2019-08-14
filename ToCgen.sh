#!/bin/bash/env usr

if test -f "ToC.md"; then
    echo "ToC.md exist" |  rm ToC.md
    echo "Updating new ToC.md" | touch ToC.md 
else 
    echo "Creating ToC.md" | touch ToC.md
fi

echo "## Overview
Welcome to the DCP Vignette repository, containing walkthrough tutorials to help you get started with the DCP primarily via command-line access. For downstream application development, please refer to the [HCA DCP API documentation](https://prod.data.humancellatlas.org/apis)." >> ToC.md

echo "The DCP welcomes any contributed notebooks or other tutorials to the list below. You can create your own branch and submit a pull request. " >> ToC.md

echo "Vignettes" >> Toc.md

find . -name "*.ipynb" | while read line; do
    echo "'$line'"
    echo "'$line'" | awk -F'/' '{print $1; print $2; print $3; print $4; print $5}'
    echo "'$line'" | awk -F "/" '{ gsub(/#/,"  ",$2); gsub(/[ ]/,"-",$3); print $2 "- [" $2 "](" $2 "/" $3 ")" }'
    echo "'$line'" | awk -F "/" '{ print "- [ "$2" ](" $2 "/" $3 ")" }' >> ToC.md 
done

