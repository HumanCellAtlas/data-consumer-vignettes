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
    └── requirements.txt        Dependencies for running the notebooks

