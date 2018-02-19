import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

group = "net.lambcode"
version = "1.0-SNAPSHOT"

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.20"

    repositories {
        mavenCentral()
    }
    
    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
    }
    
}

apply {
    plugin("kotlin2js")
    plugin("kotlin-dce-js")
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlinModule("stdlib-js", kotlin_version))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:0.22.2")
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]!!

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            outputFile = "${mainSourceSet.output.resourcesDir}/${project.name}.js"
            sourceMapEmbedSources = "always"
            sourceMap = true
            moduleKind = "plain"
        }
    }
    "runDceKotlinJs"(KotlinJsDce::class) {
        dceOptions {
            devMode = false //change this if it gets to slow to be dynamic (should be false for production)
            keep("canvas-annotation.annotate")
        }
    }
    val assembleWeb by creating(Copy::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false
        from("$buildDir/kotlin-js-min/main")
        from(mainSourceSet.output) {
            exclude("**/*.kjsm")
            exclude("${project.name}*.js*")
        }
        into("$buildDir/web")
        dependsOn("runDceKotlinJs")
    }
    "build" {
        dependsOn(assembleWeb)
    }
//    val unpackKotlinJsStdlib by creating { unpackJsFromJar("kotlin-stdlib-js") }
//    val unpackKotlinxCoroutines by creating { unpackJsFromJar("kotlinx-coroutines-core-js") }
//    val assembleWeb by creating(Copy::class) {
//        group = "build"
//        description = "Assemble the web application"
//        includeEmptyDirs = false
//        from(unpackKotlinJsStdlib)
//        from(unpackKotlinxCoroutines)
//        from(mainSourceSet.output) {
//            exclude("**/*.kjsm")
//        }
//        into("$buildDir/web")
//    }
//    "assemble" {
//        dependsOn(assembleWeb)
//    }
}

fun Task.unpackJsFromJar(artifactName: String) {
    group = "build"
    description = "Unpack the artifact $artifactName"
    val outputDir = file("$buildDir/$name")
    val compileClasspath = configurations["compileClasspath"]
    inputs.property("compileClasspath", compileClasspath)
    outputs.dir(outputDir)
    doLast {
        val kotlinStdLibJar = compileClasspath.single {
            it.name.matches(Regex("${artifactName}-.+\\.jar"))
        }
        copy {
            includeEmptyDirs = false
            from(zipTree(kotlinStdLibJar))
            into(outputDir)
            include("**/*.js")
            exclude("META-INF/**")
        }
    }
}
