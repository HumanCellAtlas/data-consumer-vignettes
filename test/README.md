# Vignette integration testing

This folder configures integration testing for the vignettes in this repository.
(See also `.travis.yml` at the root of the repository, which configures Travis
to run the tests.)

## Design

The test harness is a wrapper around [ReviewNB/treon](https://github.com/ReviewNB/treon)
with some extra code to ignore tests, install dependencies, and run all of the above
on Travis. That's it.

## Structure

    .
    ├── README.md               You are here!
    ├── ignore                  Add a directory to this file (one per line) to skip during testing
    ├── requirements-dev.txt    Dependencies for running the tests
    └── requirements.txt        Dependencies for running notebooks (global)

## Specifying requirements

To specify requirements for a notebook, add the depenencies to a `requirements.txt` next
to the notebook. (This is a convention for organization - all dependencies are installed
into the same environment.) All `requirements.txt` files in this repo will be installed
prior to testing.

`test/requirements.txt` is used to list dependencies used by all notebooks.

## Ignoring notebooks

To ignore a notebook during testing, add its path to `test/ignore`, one path per line.
Paths should be relative to the repository root.
The specified path will be deleted before tests are run. (Ham-fisted, I know.)

