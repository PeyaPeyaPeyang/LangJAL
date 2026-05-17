plugins {
    id("java")
    id("com.gradleup.shadow").version("9.0.0-rc1")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    implementation("org.ow2.asm:asm:9.10")
    implementation("org.ow2.asm:asm-commons:9.10")

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation(project(":langjal"))
}


tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "tokyo.peya.langjal.jalp.Main",
            "Implementation-Title" to "JAL Processor",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to project.group
        )
    }
}

tasks.shadowJar {
    dependsOn(tasks.jar)
    archiveBaseName.set("jalp")
    archiveClassifier.set("")
    archiveVersion.set("")

    relocate("com.ibm.icu", "tokyo.peya.langjal.cli.relocated.icu")
    relocate("org.objectweb.asm", "tokyo.peya.langjal.cli.relocated.asm")

    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
