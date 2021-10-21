package dev.binclub.binscure.processors

import dev.binclub.binscure.IClassProcessor
import dev.binclub.binscure.classpath.ClassSources
import dev.binclub.binscure.configuration.ConfigurationManager.rootConfig
import dev.binclub.binscure.utils.InstructionModifier
import org.objectweb.asm.Opcodes.CHECKCAST
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList

/**
 * @author cookiedragon234 10/Feb/2020
 */
class RemoveCheckCast(source: ClassSources): IClassProcessor(source) {
	override val progressDescription: String
		get() = "Removing checkcasts"
	override val config = rootConfig
	
	override fun process(
		source: ClassSources,
		classes: MutableCollection<ClassNode>,
		passThrough: MutableMap<String, ByteArray>
	) {
		for (classNode in classes) {
			for (method in classNode.methods) {
				val modifier = InstructionModifier()
				for (insn in method.instructions) {
					if (insn.opcode == CHECKCAST) {
						val list = InsnList().apply {
							//add(InsnNode(ACONST_NULL))
						}
						modifier.replace(insn, list)
					}
				}
				modifier.apply(method)
			}
		}
	}
	
}
