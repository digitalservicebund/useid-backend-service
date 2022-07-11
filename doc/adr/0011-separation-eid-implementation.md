# 11. eID implementation in separate Microservice (Java based server due to Java SDK) and additional custom API for eService

Date: 2022-07-11

## Status

Accepted

## Context

There are 2 types of online administration website (eService) available

1. eService with eID integration (Magnus Flow)
2. eService without eID integration (Paula flow)

This document is a decision record for any eService who wishes to use eID with our solution (Paula Flow).

## Decision

1. We separate the identification logic as a standalone backend rather than embedding the logic in the eService backend.
2. We build a Java-based backend because we are going to utilize Java SDK from the eID server provider.
3. The microservice should work as an intermediate or proxy server without storing any personal data.
4. The microservice should provide an interface for the eService to initiate identification securely.

## Consequences

1. We are not re-writing the SDK in another programming language because it will increase our development time.
2. The interoperability of Java and Kotlin will ease our development.
3. Any eService can integrate easily with Paula Flow.
