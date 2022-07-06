# 10. UseID backend with Kotlin and Spring Boot

Date: 2022-07-06

## Status

Accepted

## Context

There are a lot of programming language and its web framework, since we decided that we use Spring Webflux as our web framework, there are 2 options available in Spring Webflux, Java and Kotlin. Both of them are first-class citizen which means they will get both support from Spring contributors.

## Decision

We would use Kotlin for UseID backend

## Consequences

1. Kotlin eliminates boilerplate code which is usually often seen in Java
2. Kotlin is null-safe by default
3. Interoperability between Java and Kotlin. We could still use Java third-party libraries in Kotlin.
4. It will take time for us to write code in Kotlin way
5. We will attract more engineers in the future
