buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.22")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.4.3")
        classpath(kotlin("serialization"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
