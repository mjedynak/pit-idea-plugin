plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Sync>("copyTestLib") {
    from(configurations.testRuntimeClasspath)
    into(layout.buildDirectory.dir("testLib"))
}
