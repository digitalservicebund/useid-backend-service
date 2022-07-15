# 9. API Keys

Date: 2022-07-06

## Status

Accepted

## Context

We want to filter incoming requests in the UseID backend, so that only authorized clients are able to access the API.

## Decision

We use Bearer authentication. The Client (eService) should add a token in the Authorization header when requesting to the endpoints and the token should be a string of 51 characters which was issued to the client by us.

e.g.

`Authorization: Bearer <token>`

The token should, for MVP purpose, be configured as environment variable. For the future releases, a lifecycle of token should be defined, e.g. validity of the token.

## Consequences

1. Reduce our security risk, since we only allow authorized clients to access the API
2. Reduce resource usage, since we are filtering unknown requests, and we do not process them further
