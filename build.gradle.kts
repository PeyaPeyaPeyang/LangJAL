plugins {
    id("java")
}

group = "tokyo.peya"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Copy>("copyCliTools") {
    dependsOn(":jalc:shadowJar")
    from(project(":jalc").tasks.named("shadowJar"))
    into("${layout.buildDirectory.get().asFile.path}/distribution/tools")
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

tasks.register<Zip>("packageAll") {
    dependsOn("copyCliTools", "createJre")
    from("${layout.buildDirectory.get().asFile.path}/distribution") {
        into("")
    }
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
    archiveFileName.set("langjal-${project.version}.zip")
}
