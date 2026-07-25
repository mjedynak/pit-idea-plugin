plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("com.diffplug.spotless") version "8.8.0"
}

val pitVersion = "1.25.8"
val pitJunit5PluginVersion = "1.2.3"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
}

sourceSets.main {
    java.srcDirs("src/main/java")
}

repositories {
    mavenCentral()
    mavenLocal()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.2")
        bundledPlugin("com.intellij.java")
    }
    // -- pitest dependencies
    implementation("org.pitest:pitest-command-line:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-entry:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-junit5-plugin:$pitJunit5PluginVersion")
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("org.junit.platform:junit-platform-launcher:6.1.1") { isTransitive = false }
    // -- pitest dependencies

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
    testImplementation("org.mockito:mockito-core:5.23.0")
}

tasks.jar {
    metaInf {
        from("META-INF") {
            include("plugin.xml")
            include("pluginIcon.svg")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "9.6.1"
}

spotless {
    java {
        palantirJavaFormat()
    }
    kotlin {
        ktlint()
    }
}
