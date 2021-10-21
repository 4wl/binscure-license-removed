package dev.binclub.binscure.api

import dev.binclub.binscure.api.transformers.*
import dev.binclub.binscure.utils.replaceLast
import java.io.File
import java.time.Instant
import org.objectweb.asm.tree.ClassNode

/**
 * @author cookiedragon234 25/Jan/2020
 */
data class RootConfiguration(
	val sources: List<CodeSource>,
	val postProcessor: (classes: MutableMap<String, ClassNode>, passthrough: MutableMap<String, ByteArray>) -> Unit = {a,b -> Unit },
	val mappingFile: File?,
	val libraries: List<File> = arrayListOf(),
	private val exclusions: List<String> = arrayListOf(),
	val hardExclusions: MutableList<String> = arrayListOf(),
	val arithmetic: ArithmeticObfuscationConfiguration,
	val remap: RemapConfiguration,
	val sourceStrip: SourceStripConfiguration,
	val kotlinMetadata: KotlinMetadataConfiguration,
	val crasher: CrasherConfiguration,
	val indirection: IndirectionConfiguration,
	val stringObfuscation: StringObfuscationConfiguration,
	val flowObfuscation: FlowObfuscationConfiguration,
	val methodParameter: MethodParameterConfiguration,
	val optimisation: OptimisationConfiguration,
	val numberObfuscation: NumberObfuscationConfiguration,
	val expiryDate: Instant?,
	val ignoreClassPathNotFound: Boolean = false,
	val useJavaClassloader: Boolean = false,
	val shuffleClasses: Boolean = true,
	val shuffleMethods: Boolean = true,
	val shuffleFields: Boolean = true,
	val writeStackMap: Boolean = true,
	val resetLineProgress: Boolean = true,
	val printProgress: Boolean = true,
	val watermark: Boolean = true,
	val upgradeVersions: Boolean = false
): TransformerConfiguration(true, exclusions) {
	init {
		// This might fail
		try {
			for (i in 0 until hardExclusions.size) {
				hardExclusions[i] = hardExclusions[i].trim()
			}
		} catch (t: Throwable) {}
	}
	
	fun getLineChar(): Char = if (resetLineProgress) '\r' else '\n'
	
	override fun toString(): String  = """
		|RootConfig
		|   Sources: $sources
		|   Libraries: $libraries
		|   MappingFile: $mappingFile
		|   Exclusions: $tExclusions
		|   Remap: $remap
		|   SourceStrip: $sourceStrip
		|   Kotlin Metadata: $kotlinMetadata
		|   Crasher: $crasher
		|   Indirection: $indirection
		|   StringObfuscation: $stringObfuscation
	""".trimMargin()
}
