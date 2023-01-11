# 17. Mandatory JavaScript Support

Date: 2023-01-11

## Status

Accepted

## Context

For eServices to embed our widget logic correctly, they need to execute a JavaScript file. In terms of scaling and availability of our widget,
we need a decision on whether we can ship the widget without having the need for enabled JavaScript on the client-side. There are ways in which we
can provide our widget without JavaScript (e.g. NoScript), but they would require us to alter our server side logic as well as the storage of session-information like the TCTokenURL.

## Decision

We don't want the TCTokenURL to be stored within our server. The TCTokenURL is therefore part of the widget and no further calls to our backend are necessary in order to start the eID-flow at the eService.

## Consequences

Services that want to integrate with our solution need to support JavaScript and clients that are browsing with disabled JavaScript will not be able to use our solution.
