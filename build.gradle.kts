import com.github.jk1.license.filter.LicenseBundleNormalizer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("com.diffplug.spotless") version "6.18.0"
    id("jacoco")
    id("org.sonarqube") version "4.0.0.2929"
    id("com.github.jk1.dependency-license-report") version "2.1"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.gorylenko.gradle-git-properties") version "2.4.0"
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus-ext.governikus.de/nexus/content/groups/public/")
    }
}

jacoco {
    toolVersion = "0.8.8"
}

testlogger { theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA }

dependencies {
    /** Webservice **/
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    /** Security **/
    implementation("org.springframework.boot:spring-boot-starter-security")

    /** UI **/
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")

    /** Persistence **/
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core:9.16.0")
    implementation("io.hypersistence:hypersistence-utils-hibernate-60:3.3.1")

    /** Monitoring **/
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:6.17.0")

    /** Data processing **/
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    /** Kotlin specific **/
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")

    /** Docs **/
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.1.0")

    /** Governikus Autent SDK **/
    implementation("de.governikus.autent.sdk:eid-webservice-sdk:3.73.9")
    implementation("de.governikus.autent.utils:autent-key-utils:4.0.14")
    // => CVE-2015-7501, CVE-2015-6420
    implementation("commons-collections:commons-collections:3.2.2")
    // => CVE-2021-40690
    implementation("org.apache.santuario:xmlsec:3.0.0")
    // => CVE-2020-28052
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    // => CVE-2022-40153
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.0")

    /** WebAuthn **/
    implementation("com.yubico:webauthn-server-core:2.4.0")

    /** Helpers **/
    implementation("com.github.ua-parser:uap-java:1.5.4")

    /** Development **/
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    /** Testing **/
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.mockito", "mockito-core")
        because("Use MockK instead of Mockito since it is better suited for Kotlin")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("com.ninja-squad:springmockk:4.0.0")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.0.0")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:postgresql:1.18.0")
    testImplementation("org.testcontainers:testcontainers:1.18.0")
    testImplementation("org.testcontainers:jdbc:1.18.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("org.jsoup:jsoup:1.15.3")

    /** Spring Cloud **/
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config:3.0.2")
    // => CVE-2022-3171
    implementation("com.google.protobuf:protobuf-java:3.22.0")

    /** Scheduling **/
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.2.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.2.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    withType<Test> {
        useJUnitPlatform {
            excludeTags("integration", "journey")
        }
    }

    register<Test>("integrationTest") {
        description = "Runs the integration tests."
        group = "verification"
        useJUnitPlatform {
            includeTags("integration")
        }
        // So that running integration test require running unit tests first,
        // and we won"t even attempt running integration tests when there are
        // failing unit tests.
        dependsOn(test)
        finalizedBy(jacocoTestReport)
    }
    check {
        dependsOn(getByName("integrationTest"))
    }

    register<Test>("journeyTest") {
        description = "Runs the journey tests."
        group = "verification"
        useJUnitPlatform {
            includeTags("journey")
        }
    }

    jacocoTestReport {
        // Jacoco hooks into all tasks of type: Test automatically, but results for each of these
        // tasks are kept separately and are not combined out of the box... we want to gather
        // coverage of our unit and integration tests as a single report!
        executionData.setFrom(
            files(
                fileTree(project.buildDir.absolutePath) {
                    include("jacoco/*.exec")
                },
            ),
        )

        // Avoid untested prototype code skewing coverage...
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude("**/ApplicationKt**")
                    }
                },
            ),
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        dependsOn(getByName("integrationTest")) // All tests are required to run before generating a report..
    }

    bootBuildImage {
        val containerRegistry = System.getenv("CONTAINER_REGISTRY") ?: "ghcr.io"
        val containerImageName = System.getenv("CONTAINER_IMAGE_NAME")
            ?: "digitalservicebund/${rootProject.name}"
        val containerImageVersion = System.getenv("CONTAINER_IMAGE_VERSION") ?: "latest"

        imageName.set("$containerRegistry/$containerImageName:$containerImageVersion")
        builder.set("paketobuildpacks/builder:0.1.342-tiny")
        publish.set(false)

        docker {
            publishRegistry {
                username.set(System.getenv("CONTAINER_REGISTRY_USER") ?: "")
                password.set(System.getenv("CONTAINER_REGISTRY_PASSWORD") ?: "")
                url.set("https://$containerRegistry")
            }
        }
    }

    sonarqube {
        // NOTE: sonarqube picks up combined coverage correctly without further configuration from:
        // build/reports/jacoco/test/jacocoTestReport.xml
        properties {
            property("sonar.projectKey", "digitalservicebund_useid-backend-service")
            property("sonar.organization", "digitalservicebund")
            property("sonar.host.url", "https://sonarcloud.io")
            property(
                "sonar.coverage.exclusions",
                // TODO USEID-737: Remove the ignored packages once the desktop prototype development is done
                "**/config/**,**/de/bund/digitalservice/useid/transactioninfo/**/*,**/de/bund/digitalservice/useid/timebasedtokens/**/*,**/de/bund/digitalservice/useid/eventstreams/**/*,**/de/bund/digitalservice/useid/credentials/**/*",
            )
        }
    }
    getByName("sonarqube") {
        dependsOn(jacocoTestReport)
    }

    jar {
        // We have no need for the plain archive, thus skip creation for build speedup!
        enabled = false
    }
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
    format("misc") {
        target(
            "**/*.html",
            "**/*.js",
            "**/*.json",
            "**/*.md",
            "**/*.properties",
            "**/*.sh",
            "**/*.yml",
        )
        prettier(
            mapOf(
                "prettier" to "2.6.1",
                "prettier-plugin-sh" to "0.7.1",
                "prettier-plugin-properties" to "0.1.0",
            ),
        ).config(mapOf("keySeparator" to "="))
    }
}

licenseReport {
    // If there's a new dependency with a yet unknown license causing this task to fail
    // the license(s) will be listed in build/reports/dependency-license/dependencies-without-allowed-license.json
    allowedLicensesFile = File("$projectDir/allowed-licenses.json")
    filters = arrayOf(
        // With second arg true we get the default transformations:
        // https://github.com/jk1/Gradle-License-Report/blob/7cf695c38126b63ef9e907345adab84dfa92ea0e/src/main/resources/default-license-normalizer-bundle.json
        LicenseBundleNormalizer(null, true),
    )
}

springBoot {
    buildInfo()
}
