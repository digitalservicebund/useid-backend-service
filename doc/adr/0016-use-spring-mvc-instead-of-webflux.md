# 16. Use Spring MVC instead of WebFlux

Date: 2021-12-22

## Status

Accepted

## Context

The team continues to struggle with getting comfortable & confident with WebFlux and reactive programming.

### Cognitive (over-)load

Due to the lack of team experience with the framework and paradigm, a significant learning effort is required. Learning reactive programming and the WebFlux APIs increases an already high team cognitive load.

The team has to navigate a complex project context:

- Complex political stakeholder landscape → short planning horizons, frequently shifting vision and goals.
- Developers need to participate in frequent product discovery work, incl. understanding where we fit into the existing landscape of technology & service providers.
- Direct engagement of developers with sophisticated technical stakeholders: BSI, CCC, D-Trust, Adesso, etc.
- Complex landscape of domain-specific technical requirements: eID Technische Richtlinien, EIDAS, NKB, etc.
- Limited team experience with the hosting platform: ArgoCD, Terraform, Kubernetes.
- Limited team experience with the application stack: Kotlin, Spring, WebFlux.

High load means:

- little time for the team to learn their stack together leads to…
- low levels of familiarity & confidence, which leads to…
- perceived productivity issues,
- increased development & debugging time,
- lower code / software quality (due to mix / misuse of paradigms).

Many aspects of this are outside the team’s control, but the application stack can be changed comparatively easily.

### How a switch to Spring MVC can help

WebFlux and reactive programming are perceived by the team to…

- …add a layer of complexity to an already unfamiliar stack.
  - Developers need to learn and juggle Spring Framework (mostly imperative) and WebFlux (reactive).
  - Documentation, tutorials, and examples are abundant for Spring MVC, but less so for WebFlux.
    - → Spring MVC patterns must be adapted to WebFlux, which is time-consuming.
  - Using non-reactive Spring Framework features requires coming up with additional boilerplate code & workarounds.
- …have a steep learning curve.
- …be the most complex aspect of the application currently.

### Secondary issues with WebFlux

Issues the team encountered which would not warrant a switch on their own:

- Lack of library support.
- Limited library choice → having to choose less popular libraries with fewer tutorials and less documentation.
- eID-specific libraries are not reactive.
- The project seeks open-source contributions. Spring MVC remains the more common framework, which makes contributing more accessible.

### WebFlux's benefits do not match project needs

Scalability: The project is still in an early MVP phase, where iteration speed is critical. Scalability concerns are not on the horizon, and so far, both WebFlux and MVC appear plausible candidates for meeting the project's requirements at all stages of development.

Fitness for microservice architectures: The project does not use nor plan to use a microservices architecture.

More readable code: The way the team has written code has not ended up in more readable code.

### Alternatives considered

- Switch to an entirely different application stack that the team already has experience with, such as NodeJS/Express. -> Rejected as too radical.
- Mix paradigms and use both Spring MVC and WebFlux according to their strengths. -> Rejected because it is not an intended design by Spring and increases complexity and learning space even further.

## Decision

We will switch from WebFlux to Spring MVC.

## Consequences

All controller code will need to be migrated to Spring MVC and all database code to JPA. Tests and documentation need to be adapted as well.

Operational parameters (threads, thread pools, etc.) need to be revisited because requirements will change.

Spring MVC comes with its own learning curve, and not everyone on the team has experience with it. We might be underestimating this learning curve. Even if it is easier to learn than WebFlux, it could still be significant enough that our fundamental problems remain.

If the project becomes successful, scaling may soon become a dominant constraint and will need to be addressed (though improvements could be focused on the most scale-critical parts).

Planned and upcoming developer training will fully focus on Spring / MVC topics.

Other teams at DS use WebFlux, and building skills and experience in common technologies could be beneficial. Sharing patterns, libraries, knowledge won’t be possible to the same degree anymore.

Project code size is still small → making the change now is cheaper than it would be later.
