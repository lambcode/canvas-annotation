/**
 * use AnnotationConfig { ... } to create an instance of a config
 *
 * Members are nullable because we don't know if a js consumer will supply all properties.
 */
external interface AnnotationConfig {
    var splashText: String?
    var imageLocation: String?
    var extraButtons: Array<ExtraButtonConfig>?
}

/**
 * use ExtraButtonConfig { ... } to create an instance of a extra button config
 */
external interface ExtraButtonConfig {
    var svgFile: String?
    var callback: (() -> Unit)?
}

@Suppress("FunctionName", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun AnnotationConfig(build: AnnotationConfig.() -> Unit): AnnotationConfig = (js("{}") as AnnotationConfig).apply { this.build() }

@Suppress("FunctionName", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun ExtraButtonConfig(build: ExtraButtonConfig.() -> Unit): ExtraButtonConfig = (js("{}") as ExtraButtonConfig).apply { this.build() }
