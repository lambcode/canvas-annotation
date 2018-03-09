/**
 * use AnnotationConfig().apply { ... } to create an instance of a config
 *
 * Members are nullable because we don't know if a js consumer will supply all properties.
 */
external interface AnnotationConfig {
    var splashText: String?
    var imageLocation: String?
    var saveButtonCallback: (() -> Unit)?
}

@Suppress("UnsafeCastFromDynamic", "FunctionName")
fun AnnotationConfig(): AnnotationConfig = js("{}")
