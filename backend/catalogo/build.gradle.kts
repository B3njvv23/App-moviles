// Aquí configuro el servicio REST del catálogo.
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

group = "cl.duocuc.induztek"
version = "1.0.0"
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }


