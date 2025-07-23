plugins {
    id("java")
    id("com.gradleup.shadow").version("9.0.0-rc1")
}

group = "tokyo.peya"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation(project(":langjal"))
}


tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    archiveFileName.set("jalc.jar")
    manifest {
        attributes(
            "Main-Class" to "tokyo.peya.langjal.cli.Main",
            "Implementation-Title" to "JAL Compiler",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to project.group
        )
    }
}

tasks.shadowJar {
    archiveBaseName.set("jalc")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes(
            "Main-Class" to "tokyo.peya.langjal.cli.Main"
        )
    }

    relocate("com.ibm.icu", "tokyo.peya.langjal.cli.relocated.icu")
    relocate("org.objectweb.asm", "tokyo.peya.langjal.cli.relocated.asm")
    relocate("org.antlr", "tokyo.peya.langjal.cli.relocated.antlr")

    minimize()
}
