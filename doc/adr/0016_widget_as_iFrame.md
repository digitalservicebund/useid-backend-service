# 16. Widget as iFrame

Date: 2023-01-10

## Status

Accepted

## Context

We want to provide a standardized entry point for the users in order for them to recognize
the eID process across different eServices. We believe that a unified user facing entry point
will benefit to high user numbers. To achieve this, we have multiple options, among others: provide a framework specific web component (e.g. react, vue),
enable CORS headers or ship an iFrame.

## Decision

We ship an iFrame, because it is the simplest and safest way to ensure that the styling of our widget stays the same across various eServices that might have implemented different front end stacks.

## Consequences

Using an iFrame comes with certain limitations, which, at this point, are okay. For example:
Communication to the parent DOM element is not possible.
