plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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
    archiveBaseName.set("jalp")
    archiveClassifier.set("")
    archiveVersion.set("")

    manifest {
        attributes(
            "Main-Class" to "tokyo.peya.langjal.jalp.Main",
            "Implementation-Title" to "JAL Processor",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to project.group
        )
    }
}
