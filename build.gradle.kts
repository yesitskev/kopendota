plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    id("org.jetbrains.dokka") version "2.2.0"
    `maven-publish`
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

java {
    withSourcesJar()
}

// GitHub Packages asks for no javadoc, unlike Maven Central, but the jar is cheap to build from the reference Dokka
// already generates and spares consumers a trip to the repository for documentation.
val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/yesitskev/kopendota")

            // Credentials come from ~/.gradle/gradle.properties locally and from the environment in Actions, where
            // GITHUB_ACTOR and a GITHUB_TOKEN carrying write:packages are already present.
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orNull
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orNull
            }
        }
    }

    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifact(javadocJar)

            groupId = project.group.toString()
            artifactId = providers.gradleProperty("POM_ARTIFACT_ID").getOrElse(project.name)
            version = project.version.toString()

            pom {
                name.set(providers.gradleProperty("POM_NAME"))
                description.set(providers.gradleProperty("POM_DESCRIPTION"))
                url.set(providers.gradleProperty("POM_URL"))
                inceptionYear.set(providers.gradleProperty("POM_INCEPTION_YEAR"))

                licenses {
                    license {
                        name.set(providers.gradleProperty("POM_LICENCE_NAME"))
                        url.set(providers.gradleProperty("POM_LICENCE_URL"))
                        distribution.set(providers.gradleProperty("POM_LICENCE_DIST"))
                    }
                }

                developers {
                    developer {
                        id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                        name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                        url.set(providers.gradleProperty("POM_DEVELOPER_URL"))
                    }
                }

                scm {
                    url.set(providers.gradleProperty("POM_SCM_URL"))
                    connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
                    developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
                }

                issueManagement {
                    system.set(providers.gradleProperty("POM_ISSUE_SYSTEM"))
                    url.set(providers.gradleProperty("POM_ISSUE_URL"))
                }
            }
        }
    }
}
