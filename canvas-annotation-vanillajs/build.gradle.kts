import com.eriwen.gradle.js.tasks.CombineJsTask
import com.eriwen.gradle.js.tasks.MinifyJsTask
import com.google.javascript.jscomp.*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.nio.charset.Charset

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.20"
    val gradle_js_plugin_version = "1.12.1"

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
        classpath("com.eriwen:gradle-js-plugin:$gradle_js_plugin_version")
        classpath("com.google.javascript:closure-compiler:v20160208")
    }

}

apply {
    plugin("kotlin2js")
    plugin("kotlin-dce-js")
    plugin("com.eriwen.gradle.js")
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
}

dependencies {
    compile(project(":canvas-annotation-lib"))
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]!!


configurations.create("js-min")
val artifactFile = file("${project.buildDir}/artifacts/${project.name}_${project.version}.zip")

artifacts {
    add("js-min", artifactFile)
}

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
            keep("canvas-annotation-lib.annotate")
        }
    }
    val minJs by creating(Task::class) {
        doLast {
            val output = file("$buildDir/minified/${project.name}_${project.version}.min.js")
            output.parentFile.mkdirs()
            minify(output,
                    listOf("$buildDir/kotlin-js-min/main/kotlin.js",
                            "$buildDir/kotlin-js-min/main/kotlinx-coroutines-core-js.js",
                            "$buildDir/kotlin-js-min/main/canvas-annotation-lib.js"
                    ))
        }
        dependsOn("runDceKotlinJs")
    }
    val zipArtifact by creating(Zip::class) {
        archiveName = "${project.name}_${project.version}.zip"
        destinationDir = file("$buildDir/artifacts")
        from("$buildDir/minified")

        configurations.compile.forEach { file ->
            from(zipTree(file)) {
                include("*.svg")
            }
        }
        dependsOn(minJs)
    }
    "assemble" {
        dependsOn(zipArtifact)
    }

}

fun minify(output: File, input: List<String>) {
    val compiler = Compiler()
    val options = CompilerOptions().apply {
        languageIn = CompilerOptions.LanguageMode.ECMASCRIPT5
        setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS)
    }

    val result = compiler.compile(CommandLineRunner.getBuiltinExterns(options), input.map { SourceFile.fromFile(file(it)) }, options)
    if (result.success)
        output.writeText(compiler.toSource())
    else
        throw IllegalStateException("unable to minify ${input.joinToString(", ")}")
}

fun CompilerOptions.setCompilationLevel(level: CompilationLevel) = level.setDebugOptionsForCompilationLevel(this)
