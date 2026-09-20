plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("net.fabricmc:sponge-mixin:0.17.4+mixin.0.8.7")
    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-tree:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")
    implementation("org.ow2.asm:asm-util:9.8")

    compileOnly(files("../client_26.2.jar"))
}

tasks.jar {
    manifest {
        attributes(
            "Premain-Class" to "dev.incendiary.agent.IncendiaryAgent",
            "Agent-Class" to "dev.incendiary.agent.IncendiaryAgent",
            "Can-Retransform-Classes" to "true"
        )
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}