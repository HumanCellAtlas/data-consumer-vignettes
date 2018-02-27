# DCP Data Consumer Experience


| Status | Task |
| --- | --- |
| :white_check_mark: | [Install the HCA CLI](tasks/Install)
| :large_orange_diamond: | [Log in to the DSS](tasks/Log%20In) |
| :full_moon: | [Download any BAM file](tasks/Download%20BAM) |
| :large_orange_diamond: | Download FASTQs associated with a known sample ID |
| :white_circle: | Download SmartSeq2 expression matrix as an input to scanpy |
| :white_circle: | Find out how many liver (whatever) cells are available |
| :white_circle: | Download all bundles for T-cells sequenced with 10x |
| :white_circle: | Compare QC metrics between one experiment and another |


The DCP is responsible for making HCA data available and useful to downstream
users. There are a few ways this can be done, including data portals and
various "red box" GUIs. But we also expect that many users will want to access
HCA data via the command line or through a library in their preferred
programming language. This repo examines the experience of those users.

This repo contains a number of "tasks" of increasing complexity and one or more
jupyter notebooks that attempt to accomplish each task.
