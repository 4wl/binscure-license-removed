package dev.binclub.binscure.processors.indirection

import dev.binclub.binscure.CObfuscator
import dev.binclub.binscure.IClassProcessor
import dev.binclub.binscure.classpath.ClassSources
import dev.binclub.binscure.configuration.ConfigurationManager.rootConfig
import dev.binclub.binscure.processors.renaming.generation.NameGenerator
import dev.binclub.binscure.utils.*
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import kotlin.collections.ArrayList

/**
 * @author cookiedragon234 22/Jan/2020
 */
class DynamicCallObfuscation(source: ClassSources): IClassProcessor(source) {
	private val targetOps = arrayOf(INVOKESTATIC, INVOKEVIRTUAL, INVOKEINTERFACE)
	
	private var isInit: Boolean = false
	private val decryptNode: ClassNode by lazy {
		isInit = true
		ClassNode().apply {
			access = ACC_PUBLIC + ACC_FINAL
			version = V1_8
			name = CObfuscator.classNamer.uniqueUntakenClass(source)
			signature = null
			superName = "java/lang/Object"
		}
	}
	
	private val stringDecryptMethod: MethodNode by lazy {
		MethodNode(
			ACC_PRIVATE + ACC_STATIC,
			"a",
			"(Ljava/lang/String;)Ljava/lang/String;",
			null,
			null
		).apply {
			generateDecryptorMethod(decryptNode, this)
			decryptNode.methods.add(this)
		}
	}
	
	private val bootStrapMethod: MethodNode by lazy {
		MethodNode(
			ACC_PUBLIC + ACC_STATIC,
			"b",
			"(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",
			null,
			null
		).apply {
			generateBootstrapMethod(decryptNode.name, stringDecryptMethod, this)
			decryptNode.methods.add(this)
		}
	}
	
	private val handler: Handle by lazy {
		Handle(H_INVOKESTATIC, decryptNode.name, bootStrapMethod.name, bootStrapMethod.desc, false)
	}
	override val progressDescription: String
		get() = "Transforming method calls to dynamic invokes"
	override val config = rootConfig.indirection
	
	private val IGNORE_RET_OPS = arrayOf(POP, POP2, RETURN, IFNONNULL, IFNULL, CHECKCAST)
	
	override fun process(
		source: ClassSources,
		classes: MutableCollection<ClassNode>,
		passThrough: MutableMap<String, ByteArray>
	) {
		if (!config.enabled || !config.methodCalls) {
			return
		}
		
		for (classNode in ArrayList(classes)) {
			if (isExcluded(classNode))
				continue
			if (!classNode.versionAtLeast(V1_7))
				continue
			
			for (method in classNode.methods) {
				if (isExcluded(classNode, method) || CObfuscator.noMethodInsns(method))
					continue
				if (classNode.access.hasAccess(ACC_ENUM) && method.name == "values")
					continue
				
				method.instructions = InsnList().apply {
					for (insn in method.instructions) {
						if (
							insn is MethodInsnNode
							&& targetOps.contains(insn.opcode)
							&& !insn.owner.startsWith('[')
						) {
							
							// We cannot obfusccate caller sensitive methods
							if (
								insn.owner == "java/lang/invoke/MethodHandles"
								&&
								insn.name == "lookup"
								&&
								insn.desc == "()Ljava/lang/invoke/MethodHandles\$Lookup;"
							) {
								continue
							}
							
							var newDesc = insn.desc
							if (insn.opcode != INVOKESTATIC) {
								newDesc = if ((insn.owner.startsWith('L') || insn.owner.startsWith("["))) {
									newDesc.replaceFirst("(", "(${insn.owner}")
								} else {
									newDesc.replaceFirst("(", "(L${insn.owner};")
								}
							}
							val returnType = Type.getReturnType(newDesc)
							val newReturnType = downCastType(returnType)
							
							val args = Type.getArgumentTypes(newDesc)
							
							// Downcast types to java/lang/Object
							for (i in args.indices) {
								if (insn.opcode != INVOKESTATIC && i == 0) continue
								args[i] = downCastType(args[i])
							}
							
							//newDesc = Type.getMethodDescriptor(downCastType(returnType), *args)
							newDesc = Type.getMethodDescriptor(newReturnType, *args)
							
							val paramOwner = insn.owner.replace('/', '.')
							
							val indyNode = InvokeDynamicInsnNode(
								"i",
								newDesc,
								handler,
								insn.opcode,
								encryptName(classNode, method, paramOwner),
								encryptName(classNode, method, insn.name),
								encryptName(classNode, method, insn.desc)
							)
							add(indyNode)
							
							// Cast return type to expected type (since we downcasted to Object earlier)
							if (returnType.sort == Type.OBJECT) {
								val checkCast = TypeInsnNode(CHECKCAST, returnType.internalName)
								if (!IGNORE_RET_OPS.contains(insn.next?.opcode)) {
									if (checkCast.desc != Any::class.internalName) {
										add(checkCast)
									}
								}
							}
							continue
						}
						add(insn)
					}
				}
			}
		}
		
		if (isInit) {
			source.classes[decryptNode.name] = decryptNode
		}
	}
	
	private fun encryptName(classNode: ClassNode, methodNode: MethodNode, originalStr: String): String {
		val classHash = classNode.name.replace('/', '.').hashCode()
		val methodHash = methodNode.name.replace('/', '.').hashCode()
		
		val original = originalStr.toCharArray()
		val new = CharArray(original.size)
		
		for (i in original.indices) {
			val char = original[i]
			new[i] = when (i % 5) {
				0 -> char xor 2
				1 -> char xor classHash
				2 -> char xor methodHash
				3 -> char xor (classHash + methodHash)
				4 -> char xor i
				else -> throw IllegalStateException("Illegal ${i % 6}")
			}
		}
		return String(new)
	}
}
