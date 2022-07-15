# 10. UseID backend with Kotlin and Spring Boot

Date: 2022-07-06

## Status

Accepted

## Context

Our programming language and framework ideally interoperates with the eID-Server Java SDK. Due to security needs and our open source policy, we need a well maintained and proven tech-stack. 

## Decision

We use Kotlin as our language due to the interoperability with Java and Spring as a well known web framework to fulfil our needs for security, stability and maintenance.

## Consequences

1. Kotlin eliminates boilerplate code which is usually often seen in Java
2. Kotlin is null-safe by default
3. Interoperability between Java and Kotlin. We could still use Java third-party libraries in Kotlin.
4. It will take time for us to write code in Kotlin way
5. We will attract more engineers in the future
