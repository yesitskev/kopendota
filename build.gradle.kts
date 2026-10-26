import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

// group and version live in gradle.properties, where the version is a snapshot. A release overrides it with
// -Pversion=X.Y.Z, taken from the tag of the GitHub release being published.

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.5.1"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    testImplementation(platform("com.squareup.okhttp3:okhttp-bom:5.5.1"))
    testImplementation("com.squareup.okhttp3:mockwebserver3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

dokka {
    moduleName.set(providers.gradleProperty("POM_NAME").orElse("kopendota"))
    moduleVersion.set(version.toString())

    dokkaSourceSets.main {
        includes.from("dokka/module.md")

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/yesitskev/kopendota/blob/main/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLinks.register("kotlinx-serialization") {
            url("https://kotlinlang.org/api/kotlinx.serialization/")
            packageListUrl("https://kotlinlang.org/api/kotlinx.serialization/package-list")
        }
    }

    dokkaPublications.html {
        suppressInheritedMembers.set(true)

        // Generated into docs/ so the reference can be served straight from the repository, which is also why the
        // module and package overviews that feed it live in dokka/ rather than alongside their own output.
        outputDirectory.set(layout.projectDirectory.dir("docs"))
    }

    pluginsConfiguration.html {
        footerMessage.set("kopendota — a non-official Kotlin client for the OpenDota API")
    }
}

mavenPublishing {
    // Uploads a deployment but stops short of releasing it. The release workflow calls
    // publishAndReleaseToMavenCentral instead, so the GitHub release is the only thing that makes a version public.
    publishToMavenCentral()

    // Maven Central rejects unsigned artifacts. Locally the signing.* properties apply; in CI the key arrives
    // through ORG_GRADLE_PROJECT_signingInMemoryKey.
    signAllPublications()

    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"), sourcesJar = true))

    // Coordinates and POM are read from gradle.properties: group and version, plus the POM_* keys that Maven
    // Central requires a complete set of.
}
