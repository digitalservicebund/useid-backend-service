import com.github.jk1.license.filter.LicenseBundleNormalizer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    id("com.diffplug.spotless") version "6.8.0"
    id("jacoco")
    id("org.sonarqube") version "3.4.0.2513"
    id("com.github.jk1.dependency-license-report") version "2.1"
    id("com.adarshr.test-logger") version "3.2.0"
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.8"
}

testlogger { theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    // => CVE-2021-37136, CVE-2021-37137, CVE-2021-43797
    implementation("io.netty:netty-all:4.1.77.Final") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // => CVE-2021-44228, CVE-2021-45105
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    // => CVE-2021-44228, CVE-2021-45105
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.18.0")
    // => CVE-2021-42550
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("ch.qos.logback:logback-core:1.2.9")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    implementation("com.google.zxing:javase:3.5.0")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.9")
    runtimeOnly("org.springdoc:springdoc-openapi-kotlin:1.6.9")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.mockito", "mockito-core")
        because("Use MockK instead of Mockito since it is better suited for Kotlin")
    }
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:0.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.6.1")
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
                }
            )
        )

        // Avoid untested prototype code skewing coverage...
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude("**/ApplicationKt**")
                    }
                }
            )
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

        imageName = "$containerRegistry/$containerImageName:$containerImageVersion"
        builder = "paketobuildpacks/builder:tiny"
        isPublish = false

        docker {
            publishRegistry {
                username = System.getenv("CONTAINER_REGISTRY_USER") ?: ""
                password = System.getenv("CONTAINER_REGISTRY_PASSWORD") ?: ""
                url = "https://$containerRegistry"
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
                "**/config/**"
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
            "**/*.yml"
        )
        prettier(
            mapOf(
                "prettier" to "2.6.1",
                "prettier-plugin-sh" to "0.7.1",
                "prettier-plugin-properties" to "0.1.0"
            )
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
        LicenseBundleNormalizer(null, true)
    )
}
