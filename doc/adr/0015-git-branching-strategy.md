# 15. Git branching strategy

Date: 2022-11-28

## Status

Accepted

## Context

During development, the are different git branches in our repository. When merging them, there are multiple possibilities, so for a clean repository, there is a decision needed.

See also the ADR for the corresponding app: [useid-app-ios/0003-branching-strategy](https://github.com/digitalservicebund/useid-app-ios/blob/e76dababb2015fa05ddabb60a29827892502dad0/doc/adr/0003-branching-strategy.md)

## Decision

As our main development branch we use the `main` branch (there is currently no need for a separate `develop` branch). We strive for keeping a linear git history on those branches if possible ([A tidy, linear Git history](https://www.bitsnbites.eu/a-tidy-linear-git-history/) for some rationale), therefore we use fast-forward merges instead of merge commits when merging pull requests. Merges should be done by the author of the pull request by rebasing and/or squashing and fast-forward merging into `main`.

> TBD: Use Github-PR-Squash-and-Merge or not?

All commits on `main` branch are signed and that branch is protected from rewriting history.

## Consequences

Pull Requests are based on `main` and are merged into `main` by default.

For fast-forward merges we can not use the GitHub UI for merging as this either creates merge commits or alters the signature of commits. Rebase and merge via your favorite git client.
