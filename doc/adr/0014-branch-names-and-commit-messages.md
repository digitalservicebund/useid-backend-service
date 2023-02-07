# 14. Branch names and commit messages

Date: 2022-11-28

## Status

Accepted

## Context

The tasks of this project need to be distributed over the actively working team members. But when many people work concurrently on different tasks of a project in git, there might be a lot of commits and branches, so one could lose overview.

Developers tend to have their own way of writing git messages, so there is a risk of chaos in a git history.

## Decision

The project tasks are organized via tickets with a unique number.

### Branch names

- If a ticket is available, prefix branch names with "USEID-" and the ticket number.
- After the ticket number, add a short description (e.g. ticket name) in snake_case.

> Example: `USEID-123_my_new_feature`

### Commit messages

- If the changes of a commit are described by a ticket, prefix the commit message with "USEID-" and the ticket number.
- If there is no ticket (e.g. hotfixes or external contributions), use "NO-TICKET" as prefix.
- After the prefix, followed by a colon, describe the changes.
- The change description is written in the present (not past present), starting with a verb and a capital first letter.

> Examples: `USEID-123: Implement new feature` or `NO-TICKET: Fix DB connection`

## Consequences

The git history becomes clearer with a consistent structural pattern throughout the commit messages, which greatly increases readability and general understanding.

The purpose of each commit and each active branch is easily understandable and can immediately be connected to the associated ticket by looking at the number.
