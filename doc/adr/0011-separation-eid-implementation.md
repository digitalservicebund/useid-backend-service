# 11. eID implementation in separate Microservice

Date: 2022-07-11

## Status

Accepted

## Context

Some eService ("Dienstanbieter") don't have an existing eID integration in place. Therefore, we need to implement the eID integration in our backend to extend our possible customer range.

## Decision

- We separate the identification logic as a standalone backend rather than embedding the logic in the eService backend.
- We build a Java-compatible backend because we are going to utilize the Java SDK from the eID server provider.
- The microservice should work as an intermediate or proxy server without storing any personal data.
- The microservice should provide an interface for the eService to initiate identification securely.

## Consequences

- We are not re-writing the SDK in another programming language because it will increase our development time.
- The interoperability of Java and Kotlin will ease our development.
- Any eService can integrate with our solution.
