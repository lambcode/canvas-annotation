import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream

allprojects {
    group = "net.lambcode"
    version = getVersionName()

}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    try {
        exec {
            commandLine("git", "describe", "--tags")
            standardOutput = stdout
        }
    }
    catch (ex: ExecException) {
        throw IllegalStateException("Unable to resolve version from git. Must have at least one tag as ancestor", ex)
    }

    return stdout.toString().trim()
}