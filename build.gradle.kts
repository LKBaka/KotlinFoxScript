plugins {
    kotlin("jvm") version "2.0.21"
    id("application") // 应用程序插件
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("net.java.dev.jna:jna:5.13.0")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("me.user.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.user.MainKt"
    }

    // 修正后的依赖打包方式
    from(sourceSets.main.get().output)

    // 添加依赖项（推荐使用官方方式）
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    }) {
        exclude("META-INF/versions/9/module-info.class") // 排除冲突文件
    }
}