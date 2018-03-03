import java.io.ByteArrayOutputStream

allprojects {
    group = "net.lambcode"
    version = getVersionName()

}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}