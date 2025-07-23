plugins {
    id("java")
    id("antlr")
    id("io.github.sgtsilvio.gradle.maven-central-publishing") version "0.4.1"
    id("io.github.sgtsilvio.gradle.metadata") version "0.6.0"
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

    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")

    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
    implementation("org.ow2.asm:asm-util:9.8")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
    withJavadocJar()
}

metadata {
    readableName = "JAL: Java Assembly Language"
    description = "The official implementation of Java Assembly Language."
    license {
        shortName = "MIT"
        fullName = "MIT License"
        url = "https://github.com/PeyaPeyaPeyang/LangJAL/blob/main/LICENSE"
    }
    developers {
        register("peyang") {
            fullName = "Peyang"
            email = "peyang@peya.tokyo"
        }
    }

    github {
        org = "PeyaPeyaPeyang"
        repo = "LangJAL"
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

publishing {
    publications {
        register<MavenPublication>("main") {
            from(components["java"])
        }
    }
}

tasks.named("sourcesJar", Zip::class) {
    dependsOn("generateGrammarSource")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
