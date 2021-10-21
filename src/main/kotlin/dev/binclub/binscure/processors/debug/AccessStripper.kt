package dev.binclub.binscure.processors.debug

import dev.binclub.binscure.*
import dev.binclub.binscure.api.TransformerConfiguration
import dev.binclub.binscure.classpath.ClassSources
import dev.binclub.binscure.configuration.ConfigurationManager.rootConfig
import dev.binclub.binscure.utils.addAccess
import dev.binclub.binscure.utils.hasAccess
import dev.binclub.binscure.utils.removeAccess
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode

/**
 * This transformer removes all unecessary access flags, such as private, protected, final, etc
 *
 * @author cookiedragon234 21/Feb/2020
 */
class AccessStripper(source: ClassSources): IClassProcessor(source) {
	override val progressDescription: String
		get() = "Stripping access flags"
	override val config: TransformerConfiguration
		get() = rootConfig
	
	override fun process(
		source: ClassSources,
		classes: MutableCollection<ClassNode>,
		passThrough: MutableMap<String, ByteArray>
	) {
		forClass(classes) { classNode ->
			classNode.access = makePublic(classNode.access)
			
			forMethod(classNode) { method ->
				// Dont run on static init
				if (method.name != "<clinit>" && !isExcluded(classNode, method)) {
					method.access = makePublic(method.access)
				}
			}
			
			forField(classNode) { field ->
				field.access = makePublic(field.access, classNode.access.hasAccess(ACC_INTERFACE))
			}
		}
	}
	
	private fun makePublic(access: Int, isInterface: Boolean = false): Int {
		var access = access
		if (access.hasAccess(ACC_PRIVATE))
			access = access.removeAccess(ACC_PRIVATE)
		if (access.hasAccess(ACC_PROTECTED))
			access = access.removeAccess(ACC_PROTECTED)
		if (access.hasAccess(ACC_SYNTHETIC))
			access = access.removeAccess(ACC_SYNTHETIC)
		if (access.hasAccess(ACC_BRIDGE))
			access = access.removeAccess(ACC_BRIDGE)
		if (access.hasAccess(ACC_FINAL) && !isInterface)
			access = access.removeAccess(ACC_FINAL)
		if (!access.hasAccess(ACC_PUBLIC))
			access = access.addAccess(ACC_PUBLIC)
		return access
	}
}
