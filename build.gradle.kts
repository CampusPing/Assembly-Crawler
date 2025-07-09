val groupName = "com.campusping"
val projectArtifactId = "assembly-crawler"
val currentVersion = "1.0.0-rc10"

plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = groupName
version = currentVersion

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

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = groupName
            artifactId = projectArtifactId
            version = currentVersion
            from(components["kotlin"])
        }
    }
}
