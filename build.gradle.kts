import com.eriwen.gradle.js.tasks.CombineJsTask
import com.eriwen.gradle.js.tasks.MinifyJsTask
import com.google.javascript.jscomp.*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.nio.charset.Charset

group = "net.lambcode"
version = "1.0-SNAPSHOT"

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
    val minJs by creating(Task::class) {
        doLast {
            minify("$buildDir/web/${project.name}-full.min.js", listOf(
                    "$buildDir/web/kotlin.js",
                    "$buildDir/web/kotlinx-coroutines-core-js.js",
                    "$buildDir/web/canvas-annotation.js"
                    ))
        }
        dependsOn(assembleWeb)
    }
    "build" {
        dependsOn(minJs)
    }
}

fun minify(output: String, input: List<String>) {
    val compiler = Compiler()
    val options = CompilerOptions().apply {
        languageIn = CompilerOptions.LanguageMode.ECMASCRIPT5
        setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS)
    }

    val result = compiler.compile(CommandLineRunner.getBuiltinExterns(options), input.map { SourceFile.fromFile(file(it)) }, options)
    if (result.success)
        file(output).writeText(compiler.toSource())
    else
        throw IllegalStateException("unable to minify ${input.joinToString(", ")}")
}

fun CompilerOptions.setCompilationLevel(level: CompilationLevel) = level.setDebugOptionsForCompilationLevel(this)
