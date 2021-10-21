package dev.binclub.binscure.processors.debug

import dev.binclub.binscure.IClassProcessor
import dev.binclub.binscure.configuration.ConfigurationManager.rootConfig
import dev.binclub.binscure.api.transformers.LineNumberAction.*
import dev.binclub.binscure.classpath.ClassSources
import dev.binclub.binscure.forClass
import dev.binclub.binscure.forMethod
import dev.binclub.binscure.utils.InstructionModifier
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode

/**
 * This transformer removes unecessary debugging information typically emitted by javac from class file
 *
 * @author cookiedragon234 22/Jan/2020
 */
class SourceStripper(source: ClassSources): IClassProcessor(source) {
	override val progressDescription: String
		get() = "Stripping source debug data"
	override val config = rootConfig.sourceStrip
	
	override fun process(
		source: ClassSources,
		classes: MutableCollection<ClassNode>,
		passThrough: MutableMap<String, ByteArray>
	) {
		if (!config.enabled)
			return
		
		val action = config.lineNumbers
		
		forClass(classes) { classNode ->
			classNode.sourceDebug = null
			classNode.sourceFile = null
			classNode.signature = null
			classNode.innerClasses?.clear()
			
			forMethod(classNode) { method ->
				if (action != KEEP) {
					val modifier = InstructionModifier()
					for (insn in method.instructions) {
						if (insn is LineNumberNode && action == REMOVE) {
							modifier.remove(insn)
						}
					}
					modifier.apply(method)
				}
				
				method.exceptions = null
				method.signature = null
			}
		}
	}
}
