plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("com.diffplug.spotless") version "8.8.0"
}

val pitVersion = "1.20.0"
val pitJunit5PluginVersion = "1.2.3"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
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
        intellijIdea("2024.3")
        bundledPlugin("com.intellij.java")
    }
    implementation("org.pitest:pitest-command-line:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-entry:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-junit5-plugin:$pitJunit5PluginVersion")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.junit.platform:junit-platform-launcher:1.9.2") { isTransitive = false }
    implementation("com.google.guava:guava:32.1.3-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testRuntimeOnly("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
