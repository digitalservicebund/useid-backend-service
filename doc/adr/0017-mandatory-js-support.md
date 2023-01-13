# 17. Integration through JavaScript files

Date: 2023-01-11

## Status

Accepted

## Context

All eServices should be able to integrate our service seamlessly. There are several ways for this.
Most of the options come down to the decision between using JavaScript or not using JavaScript.

Pros of having a solution without javascript:

- Smaller attack surface
- Easier to debug and test, because we could test against static html and wouldn't need a js runtime
- Potentially support for more browsers

Cons of having a solution without javascript:

- We'd need to store more information on the server
- No event tracking

Our overarching goal

## Decision

We don't want the TCTokenURL to be stored within our server. The TCTokenURL is therefore part of the widget and no further calls to our backend are necessary in order to start the eID-flow at the eService.

## Consequences

Services that want to integrate with our solution need to support JavaScript and clients that are browsing with disabled JavaScript will not be able to use our solution.
