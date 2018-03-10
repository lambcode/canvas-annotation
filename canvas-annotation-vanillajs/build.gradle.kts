import com.google.common.collect.ImmutableMap
import com.google.javascript.jscomp.*
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.io.FileWriter
import java.nio.charset.Charset
import javax.swing.UIManager.put



buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.30"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
        classpath("com.google.javascript:closure-compiler:1.0-SNAPSHOT")
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
            moduleKind = "umd"
        }
    }
    "runDceKotlinJs"(KotlinJsDce::class) {
        dceOptions {

            devMode = false //change this if it gets to slow to be dynamic (should be false for production)
            keep("canvas-annotation-lib.annotateAsPromise",
                    "canvas-annotation-lib.annotateAsCallback",
                    "canvas-annotation-lib.CanvasWrapper.annotatedImage",
                    "canvas-annotation-lib.CanvasWrapper.canvas")
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

fun minify(output: File, inputFileNames: List<String>) {
    val compiler = Compiler()
    val options = CompilerOptions().apply {
        languageIn = CompilerOptions.LanguageMode.ECMASCRIPT5
        setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS)
        setSourceMapIncludeSourcesContent(true)
        setApplyInputSourceMaps(true)
        setInputSourceMaps(mapToMinified(inputFileNames))
        setSourceMapLocationMappings(listOf(
                SourceMap.LocationMapping("$buildDir/kotlin-js-min/main/", ""),
                SourceMap.LocationMapping("./", "")
                ))
        setSourceMapOutputPath("${output.parent}/${output.nameWithoutExtension}.map.js")
    }

    val result = compiler.compile(CommandLineRunner.getBuiltinExterns(options.environment), inputFileNames.map { SourceFile.fromPath(file(it).toPath(), Charset.defaultCharset()) }, options)
    if (result.success) {
        output.writeText(compiler.toSource())
        val fileWriter = FileWriter(file("${output.path}.map"))
        compiler.sourceMap.appendTo(fileWriter, output.name)
        fileWriter.flush()


        //Need to append sourceMappingUrl because it is not done by closure compiler https://github.com/google/closure-compiler/wiki/FAQ#source-maps-and-sourcemappingurl
        output.appendText("\n//# sourceMappingURL=${output.name}.map")
    }
    else
        throw IllegalStateException("unable to minify ${inputFileNames.joinToString(", ")}")
}

fun mapToMinified(filePaths: List<String>): ImmutableMap<String, SourceMapInput> {
    val inputSourceMaps = ImmutableMap.Builder<String, SourceMapInput>()
    for (filePath in filePaths) {
        val sourceMap = SourceFile.fromFile("${filePath}.map", Charset.defaultCharset())
        inputSourceMaps.put(
                filePath, SourceMapInput(sourceMap))
    }
    return inputSourceMaps.build()
}

fun CompilerOptions.setCompilationLevel(level: CompilationLevel) = level.setDebugOptionsForCompilationLevel(this)
