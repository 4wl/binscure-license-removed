package dev.binclub.binscure.processors.flow

import dev.binclub.binscure.CObfuscator
import dev.binclub.binscure.IClassProcessor
import dev.binclub.binscure.classpath.ClassSources
import dev.binclub.binscure.configuration.ConfigurationManager.rootConfig
import dev.binclub.binscure.utils.add
import dev.binclub.binscure.utils.hasAccess
import dev.binclub.binscure.processors.runtime.opaqueSwitchJump
import dev.binclub.binscure.utils.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

/**
 * @author cookiedragon234 27/Feb/2020
 */
class CfgFucker(source: ClassSources): IClassProcessor(source) {
	override val progressDescription: String
		get() = "Obfuscating method flow"
	override val config = rootConfig.flowObfuscation
	
	override fun process(
		source: ClassSources,
		classes: MutableCollection<ClassNode>,
		passThrough: MutableMap<String, ByteArray>
	) {
		if (!config.enabled) {
			return
		}
		
		val methodParameterObfuscator = CObfuscator.processor<MethodParameterObfuscator>()
		val aggresiveness = config.severity
		
		for (classNode in classes.toTypedArray()) {
			if (isExcluded(classNode) || classNode.access.hasAccess(ACC_INTERFACE))
				continue
			
			for (method in classNode.methods) {
				if (method.instructions.size() < 5 || isExcluded(classNode, method))
					continue
				
				val modifier = InstructionModifier()
				val endings = hashSetOf<InsnList>()
				
				for (insn in method.instructions) {
					if (
						insn.next != null
						&&
						random.nextInt(aggresiveness) == 0
					) {
						if (insn is MethodInsnNode || insn is FieldInsnNode || insn is VarInsnNode) {
							val (list, ending) = opaqueSwitchJump(mnStr = methodParameterObfuscator.mnToStr(classNode, method))
							modifier.prepend(insn, list)
							endings.add(ending)
						} else if (insn is JumpInsnNode && insn.opcode != GOTO) {
							val falseNum = randomInt()
							val trueNum = falseNum + 1
							val key = randomInt()
							val list = InsnList().apply {
								val trueLdc = newLabel()
								val switch = newLabel()
								val dflt = newLabel()
								val after = newLabel()
								add(JumpInsnNode(insn.opcode, trueLdc))
								add(dflt)
								var endMillis = rootConfig.expiryDate?.getEpochSecond()
								if (endMillis != null) {
									endMillis += (random.nextInt(9000).toLong())
									add(ldcInt((falseNum xor key) xor -592))
									add(MethodInsnNode(
										INVOKESTATIC,
										"java/time/Instant",
										"now",
										"()Ljava/time/Instant;",
										false
									))
									add(ldcLong(endMillis))
									add(MethodInsnNode(
										INVOKESTATIC,
										"java/time/Instant",
										"ofEpochSecond",
										"(J)Ljava/time/Instant;",
										false
									))
									add(MethodInsnNode(
										INVOKEVIRTUAL,
										"java/time/Instant",
										"compareTo",
										"(Ljava/time/Instant;)I",
										false
									))
									add(ldcInt(592))
									add(InsnNode(IMUL))
									add(InsnNode(IXOR))
								} else {
									add(ldcInt(falseNum xor key))
								}
								add(JumpInsnNode(GOTO, switch))
								add(trueLdc)
								add(ldcInt(trueNum xor key))
								add(switch)
								add(ldcInt(key))
								add(IXOR)
								add(constructTableSwitch(
									falseNum,
									dflt,
									after, insn.label
								))
								add(after)
							}
							modifier.replace(insn, list)
						}
					}
				}
				for (ending in endings) {
					method.instructions.add(ending)
				}
				endings.clear()
				modifier.apply(method)
			}
		}
	}
}
