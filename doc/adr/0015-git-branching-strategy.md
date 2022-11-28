# 15. Git branching strategy

Date: 2022-11-28

## Status

Accepted

## Context

During development, there are different git branches in our repository. When merging them, there are multiple possibilities, so for a clean repository, there is a decision needed.

See also the ADR for the corresponding app: [useid-app-ios/0003-branching-strategy](https://github.com/digitalservicebund/useid-app-ios/blob/e76dababb2015fa05ddabb60a29827892502dad0/doc/adr/0003-branching-strategy.md)

## Decision

As our main development branch we use the `main` branch. We strive for keeping a linear git history on this branch if possible ([A tidy, linear Git history](https://www.bitsnbites.eu/a-tidy-linear-git-history/) for some rationale), therefore we use fast-forward merges instead of merge commits when merging pull requests.

Merges should be done locally by rebasing and/or squashing and fast-forward merging into `main`. Since all commits inside pull requests are signed and pull requests are reviewed by team members, we also accept to merge using GitHub's "Squash and merge".

Merges should be done by the author of the pull request after getting approval by another team member. The merged branch should be deleted afterwards to keep the repository clean.

All commits on `main` branch are signed (either by committer or by GitHub) and that branch is protected from rewriting history.

## Consequences

We keep a linear git history without merge commits.

Pull Requests are based on `main` and are merged into `main` by default.

For merging pull requests, either GitHub UI (only "Squash and merge") or a local git client (only fast-forward merges, if needed after rebase and/or squash) can be used.
