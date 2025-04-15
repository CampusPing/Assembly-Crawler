plugins {
    kotlin("jvm") version "1.9.0"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create("maven-public", MavenPublication::class) {
            groupId = "com.campusping"
            artifactId = "assembly-crawler"
            version = "1.0.0-rc1"
            from(components.getByName("java"))
        }
    }
}
