/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.util.Base64

plugins {
    plugin(Deps.Plugins.androidLibrary)
    plugin(Deps.Plugins.kotlinMultiplatform)
    plugin(Deps.Plugins.mobileMultiplatform)
    plugin(Deps.Plugins.mavenPublish)
    plugin(Deps.Plugins.signing)
}

group = "dev.icerock.moko"
version = Deps.mokoMediaVersion

kotlin {
    sourceSets {
        val iosArm64Main by getting
        val iosX64Main by getting

        iosArm64Main.dependsOn(iosX64Main)
    }
}

dependencies {
    commonMainImplementation(Deps.Libs.MultiPlatform.coroutines)
    commonMainApi(Deps.Libs.MultiPlatform.mokoPermissions.common)

    androidMainImplementation(Deps.Libs.Android.appCompat)
    androidMainImplementation(Deps.Libs.Android.exifInterface)

    // TODO #34 remove external dependency
    androidMainImplementation(Deps.Libs.Android.mediaFilePicker)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }

    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("MOKO media")
            description.set("Media selection & presenting for mobile (android & ios) Kotlin Multiplatform development")
            url.set("https://github.com/icerockdev/moko-media")
            licenses {
                license {
                    url.set("https://github.com/icerockdev/moko-media/blob/master/LICENSE.md")
                }
            }

            developers {
                developer {
                    id.set("Alex009")
                    name.set("Aleksey Mikhailov")
                    email.set("aleksey.mikhailov@icerockdev.com")
                }
                developer {
                    id.set("Tetraquark")
                    name.set("Vladislav Areshkin")
                    email.set("vareshkin@icerockdev.com")
                }
                developer {
                    id.set("RezMike")
                    name.set("Mikhail Reznichenko")
                    email.set("mreznichenko@icerockdev.com")
                }
            }

            scm {
                connection.set("scm:git:ssh://github.com/icerockdev/moko-media.git")
                developerConnection.set("scm:git:ssh://github.com/icerockdev/moko-media.git")
                url.set("https://github.com/icerockdev/moko-media")
            }
        }
    }

    signing {
        val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
        val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
        val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
            String(Base64.getDecoder().decode(base64Key))
        }
        if (signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}
