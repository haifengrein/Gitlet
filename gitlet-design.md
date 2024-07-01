# Gitlet Design Document

**Name**: Haifeng Hou

Overview

The simple Git system is a version control system that allows users to track changes to files and collaborate on software projects. It supports a subset of the full Git feature set, including basic operations like init, add, commit, and rm, as well as additional operations like log, checkout, and status.

Architecture

The simple Git system consists of the following components:

Repository: This class represents a Git repository. It manages the local files and directories, as well as the .git metadata directory.
StagingArea: This class represents the Git staging area. It keeps track of the files and directories that have been added to the staging area using the add operation.
Commit: This class represents a Git commit. It stores the metadata for a commit, including the commit message and the parent commit.
RemoteRepository: This class represents a remote Git repository. It allows the local repository to push and pull changes to and from the remote repository.
Repository

The Repository class is the main component of the simple Git system. It represents a Git repository and manages the local files and directories, as well as the .git metadata directory.

Operations
The Repository class supports the following operations:

init: Initializes a new Git repository. This creates a new .git directory and initializes the metadata for the repository.
add: Adds a file or directory to the Git staging area. This updates the .git/index file with the new entries for the added files, and updates the StagingArea object.
commit: Commits the changes in the staging area to the Git repository. This creates a new Commit object with the commit metadata and the current state of the repository, and adds it to the repository history.
rm: Removes a file or directory from the Git repository. This removes the file from the local repository and the `.git/

Operations
The Repository class supports the following additional operations:

log: Shows the commit history for the Git repository. This retrieves the list of commits in the repository, and displays a summary of each commit.
checkout: Changes the current branch of the Git repository. This updates the repository state to reflect the contents of the selected branch, and updates the .git/HEAD file accordingly.
status: Shows the status of the Git repository, including the current branch and any changes that have been made. This retrieves the current state of the repository and the staging area, and displays a summary of the changes.
StagingArea

The StagingArea class represents the Git staging area. It keeps track of the files and directories that have been added to the staging area using the add operation.

Operations
The StagingArea class supports the following operations:

add: Adds a file or directory to the staging area. This updates the list of added files and directories, and writes the new entries to the .git/index file.
reset: Resets the staging area to its initial state. This removes all of the added files and directories, and updates the .git/index file accordingly.
diff: Shows the differences between the staging area and the repository. This compares the state of the staging area with the state of the repository, and displays a summary of the changes.

Commit

The Commit class represents a Git commit. It stores the metadata for a commit, including the commit message and the parent commit.
Operations
The RemoteRepository class supports the following operations:

push: Pushes the local repository to the remote repository. This updates the remote repository with the changes in the local repository, and updates the local repository with any changes from the remote repository.
pull: Pulls changes from the remote repository to the local repository. This updates the local repository with the changes in the remote repository, and updates the remote repository with any changes from the local repository.
Summary

The simple Git system provides a basic implementation of a version control system, supporting a subset of the full Git feature set. Its architecture is modular, with separate components for the repository, staging area, commits, and remote repositories. The system's operations provide the core functionality of Git, allowing users to track changes to files, collaborate on software projects, and view the history of their changes. The additional operations of rm, log, checkout, and status further enhance the system's functionality and user experience.