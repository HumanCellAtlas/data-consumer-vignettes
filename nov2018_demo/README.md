# HCA Scanpy Demo

The vignette in this folder demonstrates the ability to load an expression matrix downloaded 
from the HCA browser into scanpy and briefly explore the data. The downloaded file `pancreas.loom` is included in this repo.

Note! This notebook does _not_ demonstrate an analysis workflow for scRNA-seq data.

The file `ENSG_to_name.csv` contains the Gencode identifiers for the genes in the HCA expression matrices, along with the more commonly used gene symbol (i.e. `ENSG00000115263.14,GCG`). This might be generally useful in your exploration of HCA expression matrices.


## Installation

This notebook assumes that you have installed python 3 on your system, 
and that the `pip` executable installs packages for that python
distribution. For some users, it may be necessary to use `pip3` instead.

```bash
pip install -r ./REQUIREMENTS.txt
```

## Usage

Start up the jupyter kernel, which will open a web browser. You can then 
select the `hca_demo_scanpy.ipynb` notebook from the `Files` sidebar. 

```bash
jupyter lab
```
