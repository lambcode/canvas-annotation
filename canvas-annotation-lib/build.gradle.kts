import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.30"

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
    }

}

apply {
    plugin("kotlin2js")
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {

            moduleKind = "umd"
            sourceMapEmbedSources = "always"
            sourceMapPrefix = "./"
            sourceMap = true
        }
    }
}

repositories {
    mavenCentral()
}

val kotlin_version: String by extra

dependencies {
    compile(kotlinModule("stdlib-js", kotlin_version))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:0.22.2")
}

