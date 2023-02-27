plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

val libGroupId = "com.sd.lib.android"
val libArtifactId = "view-dialog"
val libVersionName = "1.0.0-alpha03"

android {
    namespace = "com.sd.lib.vdialog"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        kotlinOptions.freeCompilerArgs += "-module-name=$libGroupId.$libArtifactId"
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = libGroupId
                artifactId = libArtifactId
                version = libVersionName
            }
        }
    }
}