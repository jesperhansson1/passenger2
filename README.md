# Passenger Android

This repository is set up for the [Git Flow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) branching model.

## Development
* All development work must be done in separate branches that are branched off the `develop` branch.
* All changes to the `develop` branch must be made using _pull requests_.
* All _pull requests_ **must** be reviewed by at least one other developer before accepted.
* Feature branches **must** follow the following naming standard `feature/[JIRA_ISSUE]`

## Releasing
* When release process starts a release branch must be branched off the `develop` branch
* Naming standard for the release branch is `release/{version}`
* When the release is ready, the release branch is merged to `master` and tagged with the version number.
* If several release candidates are created from the same release branch, they must be tagged with the release candidate version number as with any regular release
