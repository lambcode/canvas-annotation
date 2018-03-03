
repositories {
    mavenCentral()
}

configurations.create("js-min")

dependencies {
    add("js-min", project(":canvas-annotation-vanillajs")) {
        this.targetConfiguration = "js-min"
    }
}

tasks {
    val getDependency by creating(Copy::class) {
        into("${buildDir}/web")
        configurations.get("js-min").forEach { file ->
            from(zipTree(file))
        }
    }
    val copyHtml by creating(Copy::class) {
        from("src")
        into("${buildDir}/web")
        filter {
            it.replace("\$JS_LIBRARY", "canvas-annotation-vanillajs_${project.version}.min.js")
        }
    }
    val zipArtifact by creating(Zip::class) {
        archiveName = "${project.name}_${project.version}.zip"
        destinationDir = file("$buildDir/artifacts")
        from("$buildDir/web")

        dependsOn(getDependency)
        dependsOn(copyHtml)
    }
    task("assemble") {
        dependsOn(zipArtifact)
    }

    task("clean") {
        delete("$buildDir")
    }
}
