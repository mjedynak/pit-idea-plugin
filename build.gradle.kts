import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("com.diffplug.spotless") version "8.8.0"
}


val pitVersion = "1.25.8"
val pitJunit5PluginVersion = "1.2.3"

kotlin {
    jvmToolchain(25)
}


sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("testSupport") {
        kotlin.srcDirs("src/testSupport/kotlin")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    intellijPlatform {
        defaultRepositories()
    }
}

val integrationTestImplementation = configurations.getByName("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // -- pitest dependencies marker
    implementation("org.pitest:pitest-command-line:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-entry:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest:$pitVersion") { isTransitive = false }
    implementation("org.pitest:pitest-junit5-plugin:$pitJunit5PluginVersion")
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("org.junit.platform:junit-platform-launcher:6.1.1") { isTransitive = false }
    // -- pitest dependencies marker

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
    testImplementation("org.junit.vintage:junit-vintage-engine:6.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
    testImplementation("org.mockito:mockito-core:5.23.0")

    intellijPlatform {
        intellijIdea("2026.2")
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Starter, configurationName = "integrationTestImplementation")
    }
    integrationTestImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.20.2")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    integrationTestImplementation("com.jetbrains.intellij.tools:ide-starter-product-idea-ultimate:262.8665.258")
}

tasks.jar {
    from(sourceSets["testSupport"].output)
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

val testProjectDir = layout.buildDirectory.dir("testProject").get().asFile
val testProjectSourceDir = file("src/integrationTest/resources/testProject")

val setupTestProject = tasks.register<Sync>("setupTestProject") {
    from(testProjectSourceDir)
    from(rootDir) {
        include("gradlew")
        include("gradlew.bat")
        include("gradle/wrapper/**")
    }
    into(testProjectDir)
}

val compileTestProject = tasks.register<Exec>("compileTestProject") {
    dependsOn(setupTestProject)
    workingDir = testProjectDir
    val gradleCmd = if (System.getProperty("os.name").lowercase().contains("windows")) "gradlew.bat" else "./gradlew"
    commandLine(gradleCmd, "copyTestLib", "classes", "testClasses", "--no-daemon")
}

val integrationTest = intellijPlatformTesting.testIdeUi.register("integrationTest") {
    task {
        dependsOn(compileTestProject)
        val integrationTestSourceSet = sourceSets.getByName("integrationTest")
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        useJUnitPlatform()
        systemProperty("test.project.path", testProjectDir.absolutePath)
    }
}

tasks.wrapper {
    gradleVersion = "9.6.1"
}

spotless {
    kotlin {
        ktlint()
    }
}
