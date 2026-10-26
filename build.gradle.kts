plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("org.jetbrains.dokka") version "2.2.0"
}

group = "io.github.yesitskev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.4.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    testImplementation(platform("com.squareup.okhttp3:okhttp-bom:5.4.0"))
    testImplementation("com.squareup.okhttp3:mockwebserver3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
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
