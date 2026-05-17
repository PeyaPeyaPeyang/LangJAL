plugins {
    id("java")
}

group = "tokyo.peya"
version = "1.3.1"

allprojects {
    group = rootProject.group
    version = rootProject.version
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Copy>("copyCliTools") {
    dependsOn(":jalc:jar")
    from(project(":jalc").configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("distribution/lib"))

    from(project(":jalc").tasks.named("jar"))
    into("${layout.buildDirectory.get().asFile.path}/distribution/lib")

    from(project(":jalp").configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("distribution/lib"))

    from(project(":jalp").tasks.named("jar"))
    into("${layout.buildDirectory.get().asFile.path}/distribution/lib")
}

tasks.register<Copy>("copyAccessories") {
    into(layout.buildDirectory.dir("distribution"))

    from("scripts/win") {
        into("bin")
    }
    from("scripts/unix") {
        into("bin")
    }
    from("README.md") {
        into("")
    }
    from("LICENSE") {
        into("")
    }
    from("examples") {
        into("examples")
    }
}

tasks.register<Exec>("createJre") {
    val outputDir = file("${layout.buildDirectory.get().asFile.path}/distribution/runtime")
    onlyIf {
        !outputDir.exists()
    }

    commandLine = listOf(
        "jlink",
        "--add-modules", "java.base",
        "--output", outputDir.absolutePath
    )
}

tasks.register<Zip>("createDistribution") {
    dependsOn("clean", "copyCliTools", "createJre", "copyAccessories")
    from("${layout.buildDirectory.get().asFile.path}/distribution") {
        into("")
    }
     archiveFileName.set(
        providers.gradleProperty("distributionClassifier")
            .map { "langjal-${project.version}-$it.zip" }
            .orElse("langjal-${project.version}.zip")
    )
}
