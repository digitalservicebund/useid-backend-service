# UseId Backend Service

[![Pipeline](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/scan.yml)

Kotlin service built with the [Spring Web MVC stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc).

## UseId project
> **Important:  This project has been discontinued**
​
This repository is part of the UseId project, that provided the BundesIdent mobile app.  You can find other repositories related to this project in the following list:
​
- Architecture
	- [Architecture](https://github.com/digitalservicebund/useid-architecture/tree/main): Documentation and overview of the UseId architecture
- Backend
	- [Backend](https://github.com/digitalservicebund/useid-backend-service): Kotlin service that acts as the backend for the mobile apps and eID-Service integration for eServices.
- eService
	- [eService-example](https://github.com/digitalservicebund/useid-eservice-example): An example application for an eService integrating with the UseId identity solution.
	- [eService-SDK](https://github.com/digitalservicebund/useid-eservice-sdk): Javascript SDK to easily integrate with the UseId identity solution.
- eID client (mobile app)
	- [iOS client for BundesIdent](https://github.com/digitalservicebund/useid-app-ios)
	- [Android client for BundesIdent](https://github.com/digitalservicebund/useid-app-android)
	- [AusweisApp2 Wrapper iOS](https://github.com/digitalservicebund/AusweisApp2Wrapper-iOS-SPM): Forked repository of the Governikus AusweisApp2 Wrapper for iOS

## Prerequisites

Kotlin 1.8 w/ Java 17, Docker for building and running the containerized application:

```bash
brew install openjdk@17
brew install --cask docker # or just `brew install docker` if you don't want the Desktop app
```

For the provided Git hooks you will need:

```bash
brew install lefthook node
```

## Getting started

**To get started with development run:**

```bash
./run.sh init
```

This will replace placeholders in the application template and install a couple of Git hooks.

## Run locally

To run the application locally, a local PostgreSQL database is required.

**To spin up the database run:**

```bash
docker-compose up -d
```

**To start the application locally run:**

You need to set the environment variable `TRACKING_MATOMO_DOMAIN`.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Tests

The project has distinct unit and integration test sets.

**To run just the unit tests:**

```bash
./gradlew test
```

**To run the integration tests:**

```bash
./gradlew integrationTest
```

**Note:** Running integration tests requires passing unit tests (in Gradle terms: integration tests depend on unit
tests), so unit tests are going to be run first. In case there are failing unit tests we won't attempt to continue
running any integration tests.

**To run integration tests exclusively, without the unit test dependency:**

```bash
./gradlew integrationTest --exclude-task test
```

Denoting an integration test is accomplished by using a JUnit 5 tag annotation: `@Tag("integration")`.

Furthermore, there is another type of test worth mentioning. We're
using [ArchUnit](https://www.archunit.org/getting-started)
for ensuring certain architectural characteristics, for instance making sure that there are no cyclic dependencies.

## Contributing

Everyone is welcome to contribute the development of this project. You can contribute by opening pull request, providing
documentation or answering questions or giving feedback. Please always follow the guidelines and our
[Code of Conduct](CODE_OF_CONDUCT.md).

## Contributing code

Open a pull request with your changes and it will be reviewed by someone from the team. When you submit a pull request,
you declare that you have the right to license your contribution to the DigitalService and the community. By submitting
the patch, you agree that your contributions are licensed under the MIT license.

Please make sure that your changes have been tested before submitting a pull request.

## Formatting & Linting

For linting and formatting Kotlin code [ktlint](https://ktlint.github.io) is used.

Consistent formatting for Kotlin, as well as various other types of source code (JSON, Markdown, Yaml, ...), is being
enforced via [Spotless](https://github.com/diffplug/spotless).

**Check formatting:**

```bash
./gradlew spotlessCheck
```

**Autoformat sources:**

```bash
./gradlew spotlessApply
```

### IntelliJ IDEA setup

See https://github.com/pinterest/ktlint#-with-intellij-idea

```bash
brew install ktlint
ktlint applyToIDEAProject
```

## Git hooks

The repo contains a [Lefthook](https://github.com/evilmartians/lefthook) configuration,
providing a Git hooks setup out of the box.

**To install these hooks, run:**

```bash
./run.sh init
```

The hooks are supposed to help you to:

- commit properly formatted source code only (and not break the build otherwise)
- write [conventional commit messages](https://chris.beams.io/posts/git-commit/)
- not accidentally push [secrets and sensitive information](https://thoughtworks.github.io/talisman/)

## Code quality analysis

Continuous code quality analysis is performed in the pipeline upon pushing to trunk; it requires a token provided
as `SONAR_TOKEN` repository secret that needs to be obtained from https://sonarcloud.io.

**To run the analysis locally:**

```bash
SONAR_TOKEN=[sonar-token] ./gradlew sonarqube
```

Go to [https://sonarcloud.io](https://sonarcloud.io/dashboard?id=digitalservicebund_useid-backend-service)
for the analysis results.

## Container image

Container images running the application are automatically published by the pipeline to
the [GitHub Packages Container registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
.

**To run the latest published image:**

```bash
docker run -p8080:8080 "ghcr.io/digitalservicebund/useid-backend-service:$(git log -1 origin/main --format='%H')"
```

The service will be accessible at `http://localhost:8080`.

We are using Spring's built-in support for producing an optimized container image:

```bash
./gradlew bootBuildImage
docker run --name useid-backend-service -e "SPRING_PROFILES_ACTIVE=local" --network="host" -d ghcr.io/digitalservicebund/useid-backend-service
```

Container images in the registry
are [signed with keyless signatures](https://github.com/sigstore/cosign/blob/main/KEYLESS.md).

**To verify an image**:

```bash
cosign verify "ghcr.io/digitalservicebund/useid-backend-service:$(git log -1 origin/main --format='%H')"
```

If you need to push a new container image to the registry manually there are two ways to do this:

**Via built-in Gradle task:**

```bash
export CONTAINER_REGISTRY=ghcr.io
export CONTAINER_IMAGE_NAME=digitalservicebund/useid-backend-service
export CONTAINER_IMAGE_VERSION="$(git log -1 --format='%H')"
CONTAINER_REGISTRY_USER=[github-user] CONTAINER_REGISTRY_PASSWORD=[github-token] ./gradlew bootBuildImage --publishImage
```

**Note:** Make sure you're using a GitHub token with the necessary `write:packages` scope for this to work.

**Using Docker:**

```bash
echo [github-token] | docker login ghcr.io -u [github-user] --password-stdin
docker push "ghcr.io/digitalservicebund/useid-backend-service:$(git log -1 --format='%H')"
```

**Note:** Make sure you're using a GitHub token with the necessary `write:packages` scope for this to work.

## Deployment

Changes in trunk are continuously deployed in the pipeline. After the staging deployment, the pipeline runs a
verification step in form of journey tests against staging, to ensure we can safely proceed with deploying to
production.

Denoting a journey test is accomplished by using a JUnit 5 tag annotation: `@Tag("journey")`. Journey tests are excluded
from unit and integration test sets.

**To run the journey tests:**

```bash
STAGING_URL=[staging-url] && STAGING_API_KEY=[staging-api-key] ./gradlew journeyTest
```

When omitting the `STAGING_URL` and `STAGING_API_KEY` env variable journey tests run against the local spring
application.

### Deploy specific commits or branches to staging

If you want to deploy a different commit than the latest to `main` to staging(e.g. for testing purposes), you can use
the custom Github Actions. First you need to run the `Build image` workflow
manually [here](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/build-image.yml). You have
to reference the branch or commit you want to base the image on and give a name to the resulting image. After you have
built the image, you can deploy it to staging using the `Deploy staging`
action [here](https://github.com/digitalservicebund/useid-backend-service/actions/workflows/deploy-staging.yml). You
have to use the same image name as before.

## Vulnerability Scanning

Scanning container images for vulnerabilities is performed with [Trivy](https://github.com/aquasecurity/trivy)
as part of the pipeline's `build` job, as well as each night for the latest published image in the container repository.

**To run a scan locally:**

Install Trivy:

```bash
brew install aquasecurity/trivy/trivy
```

```bash
./gradlew bootBuildImage
trivy image --severity HIGH,CRITICAL ghcr.io/digitalservicebund/useid-backend-service:latest
```

## License Scanning

License scanning is performed as part of the pipeline's `build` job. Whenever a production dependency is being added
with a yet unknown license the build is going to fail.

**To run a scan locally:**

```bash
./gradlew checkLicense
```

## Architecture Decision Records

[Architecture decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
are kept in the `docs/adr` directory. For adding new records install
the [adr-tools](https://github.com/npryce/adr-tools) package:

```bash
brew install adr-tools
```

See https://github.com/npryce/adr-tools regarding usage.

## Slack notifications

Opt in to CI posting notifications for failing jobs to a particular Slack channel by setting a repository secret with
the name `SLACK_WEBHOOK_URL`, containing a url for [Incoming Webhooks](https://api.slack.com/messaging/webhooks).

## API documentation

- Production:
  - [Swagger UI](https://eid.digitalservicebund.de/api/docs)
  - [JSON](https://eid.digitalservicebund.de/api/docs.json)
- Staging:
  - [Swagger UI](https://useid.dev.ds4g.net/api/docs)
  - [JSON](https://useid.dev.ds4g.net/api/docs.json)
- Local
  - [Swagger UI](http://localhost:8080/api/docs)
  - [JSON](http://localhost:8080/api/docs.json)

## Note: Prototype Code

This repository includes code used only for a prototype version of the widget. The code is tagged with `// PROTOTYPE` comments.

Find more details about the prototype in this [proposal for a device switch](https://github.com/digitalservicebund/useid-architecture/blob/8b4e0ae9b1536f7d62f8d089b7bc135e71ceba63/research/device-switch/proposal-qr-code-based-device-switch-with-webauthn.md).
