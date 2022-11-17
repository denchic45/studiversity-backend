val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.21"
    id("io.ktor.plugin") version "2.1.3"
                id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"

    // Shadow plugin - enable support for building our UberJar
    id("com.github.johnrengelman.shadow") version "7.1.2"
    war
}

group = "com.studiversity"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

tasks {
    val shadowJarTask = named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        // explicitly configure the filename of the resulting UberJar
        val uberJarFileName =
            "com.denchic45.studiversity-backend-0.0.1.jar"
//            "${project.name}-${project.version}-all.jar\"" //todo переиспользовать!
        archiveFileName.set(uberJarFileName)

        // Appends entries in META-INF/services resources into a single resource. For example, if there are several
        // META-INF/services/org.apache.maven.project.ProjectBuilder resources spread across many JARs the individual
        // entries will all be concatenated into a single META-INF/services/org.apache.maven.project.ProjectBuilder
        // resource packaged into the resultant JAR produced by the shading process -
        // Effectively ensures we bring along all the necessary bits from Jetty
        mergeServiceFiles()

        // As per the App Engine java11 standard environment requirements listed here:
        // https://cloud.google.com/appengine/docs/standard/java11/runtime
        // Your Jar must contain a Main-Class entry in its META-INF/MANIFEST.MF metadata file
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass.get()))
        }
    }

    // because we're using shadowJar, this task has limited value
    named("jar") {
        enabled = false
    }

    // update the `assemble` task to ensure the creation of a brand new UberJar using the shadowJar task
    named("assemble") {
        dependsOn(shadowJarTask)
    }
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}