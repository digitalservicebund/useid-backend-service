import com.github.jk1.license.filter.LicenseBundleNormalizer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.spring)
    alias(libs.plugins.com.diffplug.spotless)
    id("jacoco")
    id("jacoco-report-aggregation")
    alias(libs.plugins.org.sonarqube)
    alias(libs.plugins.com.github.jk1.dependency.license.report)
    alias(libs.plugins.com.adarshr.test.logger)
    alias(libs.plugins.com.gorylenko.gradle.git.properties)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus-ext.governikus.de/nexus/content/groups/public/")
    }
    maven {
        url = uri("https://build.shibboleth.net/nexus/content/repositories/releases/")
    }
}

jacoco {
    toolVersion = "0.8.8"
}

testlogger { theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA }

dependencies {
    implementation(platform(libs.org.jetbrains.kotlin.kotlin.bom))
    /** Webservice **/
    implementation(libs.org.springframework.boot.spring.boot.starter.web)
    implementation(libs.org.springframework.boot.spring.boot.starter.validation)

    /** Security **/
    implementation(libs.org.springframework.boot.spring.boot.starter.security)

    /** UI **/
    implementation(libs.org.springframework.boot.spring.boot.starter.thymeleaf)
    implementation(libs.nz.net.ultraq.thymeleaf.thymeleaf.layout.dialect)

    /** Persistence **/
    implementation(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    runtimeOnly(libs.org.postgresql)
    implementation(libs.org.flywaydb.flyway.core)
    implementation(libs.io.hypersistence.hypersistence.utils.hibernate)
    implementation(libs.org.springframework.data.spring.data.redis)
    implementation(libs.io.lettuce.lettuce.core)

    /** Monitoring **/
    implementation(libs.org.springframework.boot.spring.boot.starter.actuator)
    implementation(libs.io.micrometer.micrometer.registry.prometheus)
    implementation(libs.org.springframework.boot.spring.boot.starter.aop)
    implementation(libs.io.sentry.sentry.spring.boot.starter.jakarta)

    /** Data processing **/
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)

    /** Kotlin specific **/
    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk8)
    implementation(libs.io.github.microutils.kotlin.logging.jvm)

    /** Docs **/
    implementation(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.org.springdoc.springdoc.openapi.starter.webmvc.api)

    /** Governikus Panstar SDK **/
    implementation(libs.de.governikus.panstar.sdk.panstar.soap.sdk)
    implementation(libs.de.governikus.identification.report.impl.java)
    implementation(libs.javax.xml.ws.jaxws.api)
    implementation(libs.com.sun.xml.bind.jaxb.impl)
    implementation(libs.com.sun.xml.messaging.saaj.saaj.impl)

    /** WebAuthn **/
    implementation(libs.com.yubico.webauthn.server.core)

    /** Helpers **/
    implementation(libs.com.github.ua.parser.uap.java)

    /** Development **/
    implementation(libs.org.springframework.boot.spring.boot.configuration.processor)
    developmentOnly(libs.org.springframework.boot.spring.boot.devtools)

    /** Spring Cloud **/
    implementation(libs.org.springframework.cloud.spring.cloud.starter.kubernetes.client.config)
    // => CVE-2022-3171
    implementation(libs.com.google.protobuf.protobuf.java)

    /** Scheduling **/
    implementation(libs.net.javacrumbs.shedlock.shedlock.spring)
    implementation(libs.net.javacrumbs.shedlock.shedlock.provider.jdbc.template)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.org.springframework.boot.spring.boot.starter.test) {
                    exclude("org.mockito", "mockito-core")
                    because("Use MockK instead of Mockito since it is better suited for Kotlin")
                }
                implementation(libs.org.springframework.boot.spring.boot.starter.webflux)
                implementation(libs.com.ninja.squad.springmockk)
                implementation(libs.org.springframework.security.spring.security.test)
            }
        }

        val archTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(sourceSets.main.get().output)
                implementation(libs.com.tngtech.archunit.archunit.junit5)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(libs.org.springframework.boot.spring.boot.starter.test) {
                    exclude("org.mockito", "mockito-core")
                    because("Use MockK instead of Mockito since it is better suited for Kotlin")
                }
                implementation(libs.org.springframework.boot.spring.boot.starter.webflux)
                implementation(libs.com.ninja.squad.springmockk)
                implementation(libs.org.testcontainers.junit.jupiter)
                implementation(libs.org.testcontainers.postgresql)
                implementation(libs.org.testcontainers)
                implementation(libs.org.testcontainers.jdbc)
                implementation(libs.org.awaitility)
                implementation(libs.org.jsoup)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test, archTest)
                    }
                }
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val journeyTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.org.springframework.boot.spring.boot.starter.test) {
                    exclude("org.mockito", "mockito-core")
                    because("Use MockK instead of Mockito since it is better suited for Kotlin")
                }
                implementation(libs.org.springframework.boot.spring.boot.starter.webflux)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test, archTest, integrationTest)
                    }
                }
            }
        }
    }
}

configurations["integrationTestImplementation"].extendsFrom(configurations.implementation.get())
configurations["journeyTestImplementation"].extendsFrom(configurations.implementation.get())

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    withType(com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class) {
        fun isStable(version: String): Boolean {
            val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
            val regex = "^[0-9,.v-]+(-r)?$".toRegex()
            return stableKeyword || regex.matches(version)
        }
        gradleReleaseChannel = "current"
        revision = "release"
        rejectVersionIf { !isStable(candidate.version) }
    }

    check {
        dependsOn(getByName("archTest"), getByName("integrationTest"))
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
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

    getByName("sonarqube") {
        dependsOn(getByName("testCodeCoverageReport"), getByName("integrationTestCodeCoverageReport"))
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "digitalservicebund_useid-backend-service")
        property("sonar.organization", "digitalservicebund")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/*/*.xml")
        property(
            "sonar.coverage.exclusions",
            // TODO USEID-737: Remove the ignored packages once the desktop prototype development is done
            "**/config/**,**/de/bund/digitalservice/useid/transactioninfo/**/*,**/de/bund/digitalservice/useid/timebasedtokens/**/*,**/de/bund/digitalservice/useid/eventstreams/**/*,**/de/bund/digitalservice/useid/credentials/**/*",
        )
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
