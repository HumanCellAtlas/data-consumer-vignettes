# HCA Scanpy Demo - May 2019

## About

The vignette in this folder demonstrates the ability to load an expression matrix downloaded
from the HCA Data Browser into Scanpy and briefly explore the data.  This tutorial was based on the
[original tutorial](https://github.com/HumanCellAtlas/data-consumer-vignettes/tree/master/Explore%20an%20HCA%20Data%20Set%20in%20Scanpy%20(Nov%202018)) created in November of 2018 by Genevieve Haliburton and other members of the HCA
DCP team.  It has been updated to work directly with the loom-based matrix produced by the
matrix service that you can use from the [HCA Data Browser](https://prod.data.humancellatlas.org/explore).

The downloaded file, `pancreas.loom`, is the result of end-to-end processing in the Data Coordination Platform workflow, and is included in this repo.  However, you can use the matrix service on the [HCA Data Browser](https://prod.data.humancellatlas.org/explore/projects?filter=%5B%7B%22facetName%22%3A%22organ%22%2C%22terms%22%3A%5B%22pancreas%22%5D%7D%2C%7B%22facetName%22%3A%22projectId%22%2C%22terms%22%3A%5B%22e8642221-4c2c-4fd7-b926-a68bce363c88%22%5D%7D%5D) to generate a live matrix that will work with this demo as well.  The link takes you to the appropriate query and you can then click the "Request Expression Matrix" button to get a live matrix for this project.

The file `ENSG_to_name.csv` contains the Gencode identifiers for the genes in the HCA expression matrices, along with the more commonly used gene symbol (i.e. `ENSG00000115263,GCG`). This might be generally useful in your exploration of HCA expression matrices.  Note, the matrix service now also contains gene symbols so this mapping file is used in this tutorial but not, strictly, needed.

## Setup

This Jupyter notebook requires Python3 and a Jupyter environment to run in.
The easiest way to create the appropriate environment locally is to use the
Conda tool.  This comes pre-configured in the [Anaconda distribution](https://www.anaconda.com/) and we
recommend that approach if you're working locally on a Mac, Windows, or Linux PC.

### Create an Environment

Once you have Anaconda installed you can create an environment that will isolate this
work form other Python environments you may use for other projects.

```bash
$ conda create --name hca_demo_scanpy python=3
$ conda activate hca_demo_scanpy
```

### Install Dependencies

This notebook assumes that you have various Python libraries installed. Make sure you are in
the activated hca_demo_scanpy environment and then use the following command to install
library dependencies.

```bash
$ pip install -r requirements.txt
```

NOTE: the Python dependencies may have additional system dependencies that you will need to install depending on your environment.  For example on Debian 4.9.144-3.1 (2019-02-19) you need to install the following dependencies before the pip install will work:

```bash
$ sudo apt-get install libc++-dev g++
$ sudo ln -s /usr/lib/x86_64-linux-gnu/libstdc++.so.6 /usr/lib/libstdc++.so
$ export PATH=/usr/lib/gcc/x86_64-linux-gnu/6:$PATH
$ pip install -r ./REQUIREMENTS.txt
```


### Run the Notebook

Start up the jupyter kernel, which will open a web browser.

```bash
$ jupyter notebook --notebook-dir="$(pwd)"
```

Once you have the notebook server running you can navigate to:

http://localhost:8888

To interact with the notebook.  You can then
select the `notebooks_hca_demo_scanpy.ipynb` notebook from the `Files` section.

## Tutorial Walkthrough

You can find a complete walkthrough in the [Guide](https://prod.data.humancellatlas.org/guides) section of the HCA Data Portal.

In short, if you run the notebook as is, it will use the following file as the input file:

    file:///f9e363cd-7fa5-4349-add2-1c9bd86d10c8.loom

Alternatively, you can replace the value in the field in the second code block with a Loom URL
from the matrix download feature of the portal, such as:

    https://s3.amazonaws.com/dcp-matrix-service-results-prod/3938cf5a-3159-4eb7-aef5-59589795c268.loom
