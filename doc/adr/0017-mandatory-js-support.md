# 17. Mandatory client-side JavaScript support

Date: 2023-01-11

## Status

Accepted

## Context

All eServices should be able to integrate our service seamlessly.
ADR 0016 determines this integration by specifying the usage of a JavaScript file that creates an iFrame for displaying the widget.
However, the widget needs to execute / fetch logic, everytime it is loaded, to work correctly.
We need to decide whether we want to have a server-side or client-side approach to prepare the widget.
Most of the reasons and implications come down to the decision between using client-side JavaScript or not, because a client side approach would require the user to have JavaScript enabled.

Client-side approach (JavaScript enabled):

- benefits:
  - client-side event tracking is possible
  - frugal use of data on the server (no need to store the constructed TCTokenURL on the server)
  - additional room for client-side adjustments / use-cases
- costs:
  - potentially fewer users because we cannot enforce to enable JavaScript
  - setting up a test environment requires more effort
  - potential security vectors

Server-side approach (JavaScript disabled):

- benefits:
  - smaller attack surface
  - easier to debug and test, because we could test against static html and wouldn't need a JavaScript runtime
  - potentially more users because we do not rely on enabled JavaScript
- costs:
  - storage of unnecessary data (e.g. TCTokenURL) on the server
  - client-side event tracking becomes more restrictive (possibility to send payload via a form request)
  - might limit our client-side widget use cases in the future

## Decision

Our decision is driven by two major factors: We want to avoid storing unnecessary data on the server, and we want to properly track the client-side user-journey.
Especially the improvements based on the client-side user tracking are a key feature of our product. Therefore, we choose to rely on client-side JavaScript and vote against a server-side approach.

## Consequences

We might face fewer user numbers and setting up a testing infrastructure will come with more effort.
However, we will be able to reason our product and user-experience decision based on client-side data.
We also need to provide a <noscript> tag to inform users that JavaScript is required for our solution.
