#!/bin/bash/env usr

echo "## Overview
Welcome to the DCP Vignette repository, containing walkthrough tutorials to help you get started with the DCP primarily via command-line access. For downstream application development, please refer to the [HCA DCP API documentation](https://prod.data.humancellatlas.org/apis)." > ToC.md

echo "The DCP welcomes any contributed notebooks or other tutorials to the list below. You can create your own branch and submit a pull request. " >> ToC.md

echo "Vignettes Table of Contents:" >> Toc.md

git ls-files */{README.md,*.ipynb} | while read line; do
    echo "'$line'" | tr -d "'" | awk -F "/" '{x=$0; gsub(/ /,"%",$1); gsub(/ /,"%",$2); print "- [" x " ](" $1 "/" $2 ")" }' >> ToC.md
done

