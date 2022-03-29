import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("com.diffplug.spotless") version "6.3.0"
    id("jacoco")
    id("org.sonarqube") version "3.3"
    id("com.github.jk1.dependency-license-report") version "2.1"
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

// Force version for transient dependencies...=> CVE-2021-43797
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty" && requested.name != "netty-tcnative-classes") {
            useVersion("4.1.72.Final")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    // => CVE-2021-37136, CVE-2021-37137, CVE-2021-43797
    implementation("io.netty:netty-all:4.1.72.Final") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // => CVE-2021-44228, CVE-2021-45105
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    // => CVE-2021-44228, CVE-2021-45105
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.0")
    // => CVE-2021-42550
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("ch.qos.logback:logback-core:1.2.9")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:0.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.4.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    withType<Test> {
        useJUnitPlatform {
            excludeTags("integration")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register<Test>("integrationTest") {
        description = "Runs the integration tests."
        group = "verification"
        useJUnitPlatform {
            includeTags("integration")
        }
        testLogging {
            events("passed", "skipped", "failed")
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

    jacocoTestReport {
        // Jacoco hooks into all tasks of type: Test automatically, but results for each of these
        // tasks are kept separately and are not combined out of the box.. we want to gather
        // coverage of our unit and integration tests as a single report!
        val executionDataTree = fileTree(project.buildDir.absolutePath) {
            include("jacoco/*.exec")
        }
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
        dependsOn(getByName("integrationTest")) // All tests are required to run before generating a report..
    }

    bootBuildImage {
        val containerRegistry = System.getenv("CONTAINER_REGISTRY") ?: "ghcr.io"
        val containerImageName = System.getenv("CONTAINER_IMAGE_NAME")
            ?: "digitalservice4germany/${rootProject.name}"
        val containerImageVersion = System.getenv("CONTAINER_IMAGE_VERSION") ?: "latest"

        imageName = "$containerRegistry/$containerImageName:$containerImageVersion"
        setPublish(false)
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
            property("sonar.projectKey", "digitalservice4germany_kotlin-application-template")
            property("sonar.organization", "digitalservice4germany")
            property("sonar.host.url", "https://sonarcloud.io")
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

    val prettierVersion = "2.6.1"

    format("misc") {
        target("**/*.js", "**/*.json", "**/*.md", "**/*.yml")
        prettier(prettierVersion)
    }
    format("properties") {
        target("**/*.properties")
        prettier(mapOf("prettier" to prettierVersion, "prettier-plugin-properties" to "0.1.0"))
            .config(mapOf("keySeparator" to "="))
    }
    format("shellscript") {
        target("**/*.sh")
        prettier(mapOf("prettier" to prettierVersion, "prettier-plugin-sh" to "0.7.1"))
    }
}

licenseReport {
// If there's a new dependency with a yet unknown license causing this task to fail
// the license(s) will be listed in build/reports/dependency-license/dependencies-without-allowed-license.json
    allowedLicensesFile = File("$projectDir/allowed-licenses.json")
}
