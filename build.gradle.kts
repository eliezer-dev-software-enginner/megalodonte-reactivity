plugins {
    id("java")
    id("maven-publish")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "megalodonte"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    // gerar javadoc
    withSourcesJar()
    // withJavadocJar() // Temporariamente desabilitado devido a erros de tags
}

// Configuração do Javadoc
tasks.named<Javadoc>("javadoc") {
    if (JavaVersion.current().isJava9Compatible()) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        (options as StandardJavadocDocletOptions).addStringOption("source", "17")
        (options as StandardJavadocDocletOptions).addStringOption("encoding", "UTF-8")
        (options as StandardJavadocDocletOptions).addStringOption("docencoding", "UTF-8")
        (options as StandardJavadocDocletOptions).addStringOption("charset", "UTF-8")
        (options as StandardJavadocDocletOptions).links(
            "https://docs.oracle.com/en/java/javase/17/docs/api/"
        )
        // Ignorar warnings para problemas menores
        (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
    }
}


// 🛑 2. CONFIGURA O PLUGIN DO JAVAFX
javafx {
    // Define a versão do JavaFX para ser usada em todos os módulos
    version = "17.0.10" // Mantida a versão 17.0.10.

    // Lista os módulos JavaFX que sua biblioteca PRECISA para compilar.
    // O plugin adiciona automaticamente a dependência para a sua plataforma de build.
    modules("javafx.controls")
}

dependencies {
    // Dependências de teste (mantidas)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")

    implementation("megalodonte:components:1.0.0")

    // Dependências JavaFX removidas (agora gerenciadas pelo bloco 'javafx { ... }')
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    dependsOn(tasks.named("jar"))
}

tasks.jar {
    archiveBaseName.set("reactivity")

    manifest {
        attributes(
            "Implementation-Title" to "JavaFX Reactivity components",
            "Implementation-Version" to project.version
        )
    }
}

// Configuração de Publicação (mantida)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "reactivity"
        }
    }
}