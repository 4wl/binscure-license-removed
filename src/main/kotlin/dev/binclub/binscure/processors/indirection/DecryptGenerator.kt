package dev.binclub.binscure.processors.indirection

import dev.binclub.binscure.CObfuscator.random
import dev.binclub.binscure.utils.add
import dev.binclub.binscure.utils.internalName
import dev.binclub.binscure.utils.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import kotlin.math.max

/**
 * @author cookiedragon234 12/Feb/2020
 */

fun generateDecryptorMethod(classNode: ClassNode, methodNode: MethodNode) {
	methodNode.instructions.apply {
		val loopStart = newLabel()
		val exitLoop = newLabel()
		val setCharArrVal = newLabel()
		val throwNull = newLabel()
		val getThread = newLabel()
		val tble0 = newLabel()
		val tble1 = newLabel()
		val tble2 = newLabel()
		val tble3 = newLabel()
		val tble4 = newLabel()
		val catch = newLabel()
		val catch2 = newLabel()
		val start = newLabel()
		val end = newLabel()
		val pre15 = newLabel()
		
		add(ACONST_NULL)
		add(VarInsnNode(ASTORE, 1))
		add(ACONST_NULL)
		add(VarInsnNode(ASTORE, 2))
		add(ACONST_NULL)
		add(VarInsnNode(ASTORE, 3))
		add(ACONST_NULL)
		add(VarInsnNode(ASTORE, 4))
		add(ICONST_M1)
		add(VarInsnNode(ISTORE, 5))
		add(ICONST_M1)
		add(VarInsnNode(ISTORE, 6))
		add(ICONST_M1)
		add(VarInsnNode(ISTORE, 7))
		add(ldcInt(7))
		add(VarInsnNode(ISTORE, 8))
		add(ACONST_NULL)
		add(VarInsnNode(ASTORE, 9))
		add(ldcInt(2))
		add(VarInsnNode(ISTORE, 10))
		
		
		
		val rootSwitch = newLabel()
		
		val switch5 = newLabel()
		val switch6 = newLabel()
		val switch7 = newLabel()
		val switch8 = newLabel()
		val switch9 = newLabel()
		val switch10 = newLabel()
		val switch11 = newLabel()
		val switch12 = newLabel()
		val switch13 = newLabel()
		val switch14 = newLabel()
		val switch15 = newLabel()
		val switch16 = newLabel()
		val switch17 = newLabel()
		val switch18 = newLabel()
		
		
		val opaqueTcbStart1 = newLabel()
		val opaqueTcbEnd1 = newLabel()
		val opaqueTcbHandler1 = newLabel()
		
		
		add(JumpInsnNode(GOTO, start))
		
		val blocks = arrayListOf(
			InsnList().apply {
				add(start)
				add(rootSwitch)
				add(VarInsnNode(ILOAD, 8))
				add(
					constructTableSwitch(
						0, // Min
						throwNull, // Default
						tble0, tble1, tble2, tble3, tble4, switch5, switch6, switch7, switch8, switch9, switch10, switch11, switch12, switch13,
						switch14, switch15, switch16, switch17, switch18
					)
				)
			},
			InsnList().apply {
				add(switch7)
				// First we need to decrypt the method description stored at local var 1
				// We will turn it into a char array
				add(VarInsnNode(ALOAD, 0))
				add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false))
				add(VarInsnNode(ASTORE, 1))
				add(ldcInt(9))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch9)
				add(VarInsnNode(ALOAD, 1))
				// Find the array length and create our decrypted char array (store in slot 4)
				add(ARRAYLENGTH)
				add(IntInsnNode(NEWARRAY, T_CHAR))
				add(VarInsnNode(ASTORE, 2))
				add(ldcInt(6))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch6)
				// Get the class and method hash
				add(getThread)
				add(MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false))
				add(VarInsnNode(ASTORE, 3)) // Stored in var 7
				add(ldcInt(8))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(opaqueTcbStart1)
				add(switch8)
				add(VarInsnNode(ALOAD, 3))
				add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false))
				add(VarInsnNode(ASTORE, 9))
				
				add(ldcInt(2))
				add(InsnNode(DUP))
				add(VarInsnNode(ISTORE, 10))
				add(TypeInsnNode(NEW, "java/lang/Integer"))
				add(InsnNode(DUP_X1))
				add(InsnNode(SWAP))
				add(MethodInsnNode(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V"))
				add(InsnNode(MONITOREXIT))
				
				
				add(switch5)
				add(VarInsnNode(ALOAD, 4))
				add(MethodInsnNode(INVOKEVIRTUAL, StackTraceElement::class.internalName, "getClassName", "()Ljava/lang/String;", false))
				add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false))
				add(VarInsnNode(ISTORE, 5))
				add(ldcInt(11))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
				
				add(opaqueTcbHandler1)
				add(InsnNode(POP))
				add(JumpInsnNode(GOTO, opaqueTcbEnd1))
				
				add(InsnNode(ACONST_NULL))
				val stackLoopStart = newLabel()
				add(stackLoopStart)
				add(InsnNode(POP))
				
				add(opaqueTcbEnd1)
				add(VarInsnNode(ALOAD, 9))
				add(VarInsnNode(ILOAD, 10))
				add(ldcInt(1))
				add(InsnNode(IADD))
				add(InsnNode(DUP))
				add(VarInsnNode(ISTORE, 10))
				add(AALOAD)
				add(DUP)
				add(MethodInsnNode(INVOKEVIRTUAL, StackTraceElement::class.internalName, "getClassName", "()Ljava/lang/String;", false))
				/*add(InsnNode(DUP))
				add(printlnAsm("Class"))
				add(printlnAsm())*/
				add(LdcInsnNode("java.lang."))
				add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false))
				add(JumpInsnNode(IFGT, stackLoopStart))
				
				add(VarInsnNode(ASTORE, 4))
				add(ldcInt(5))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
			},
			InsnList().apply {
				add(switch11)
				add(VarInsnNode(ALOAD, 4))
				add(MethodInsnNode(INVOKEVIRTUAL, StackTraceElement::class.internalName, "getMethodName", "()Ljava/lang/String;", false))
				add(MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false))
				add(VarInsnNode(ISTORE, 6))
				add(ldcInt(10))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				// Now loop over our new array
				add(switch10)
				add(ldcInt(0))
				add(VarInsnNode(ISTORE, 7))
				add(ldcInt(12))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch12)
				add(loopStart)
				add(VarInsnNode(ILOAD, 7))
				add(VarInsnNode(ALOAD, 2))
				add(ARRAYLENGTH)
				add(JumpInsnNode(IF_ICMPGE, exitLoop)) // If at the end of the loop go to exit
				add(ldcInt(13))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch13)
				add(VarInsnNode(ILOAD, 7))
				add(ldcInt(5))
				add(IREM)
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(tble0)
				add(VarInsnNode(ALOAD, 1)) // Encrypted Char Array
				add(VarInsnNode(ILOAD, 7)) // index
				add(CALOAD)
				add(ldcInt(2))
				add(IXOR)
				add(JumpInsnNode(GOTO, setCharArrVal))
			},
			InsnList().apply {
				add(tble1)
				add(VarInsnNode(ALOAD, 1)) // Encrypted Char Array
				add(VarInsnNode(ILOAD, 7)) // index
				add(CALOAD)
				add(VarInsnNode(ILOAD, 5)) // Class Hash
				add(IXOR)
				add(JumpInsnNode(GOTO, setCharArrVal))
			},
			InsnList().apply {
				add(tble2)
				add(VarInsnNode(ALOAD, 1)) // Encrypted Char Array
				add(VarInsnNode(ILOAD, 7)) // index
				add(CALOAD)
				add(VarInsnNode(ILOAD, 6)) // method Hash
				add(IXOR)
				add(JumpInsnNode(GOTO, setCharArrVal))
			},
			InsnList().apply {
				add(tble3)
				add(VarInsnNode(ALOAD, 1)) // Encrypted Char Array
				add(VarInsnNode(ILOAD, 7)) // index
				add(CALOAD)
				add(VarInsnNode(ILOAD, 5)) // Class Hash
				add(VarInsnNode(ILOAD, 6)) // method Hash
				add(IADD)
				add(IXOR)
				add(JumpInsnNode(GOTO, setCharArrVal))
			},
			InsnList().apply {
				add(tble4)
				add(VarInsnNode(ALOAD, 1)) // Encrypted Char Array
				add(VarInsnNode(ILOAD, 7)) // index
				add(CALOAD)
				add(VarInsnNode(ILOAD, 7)) // index
				add(IXOR)
				add(JumpInsnNode(GOTO, setCharArrVal))
			},
			InsnList().apply {
				add(setCharArrVal)
				add(I2C)
				add(VarInsnNode(ALOAD, 1)) // Decrypted Char Array
				add(SWAP)
				add(VarInsnNode(ILOAD, 7)) // Index
				add(SWAP)
				add(CASTORE)
				add(ldcInt(16))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch16)
				add(ldcInt(18))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch18)
				add(ldcInt(14))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch14)
				add(ldcInt(17))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(switch17)
				// Increment and go to top of loop
				add(IincInsnNode(7, 1))
				add(JumpInsnNode(GOTO, loopStart))
			},
			InsnList().apply {
				// If we are here then we have a decrypted char array in slot 4
				add(exitLoop)
				add(TypeInsnNode(NEW, "java/lang/String"))
				add(DUP)
				add(VarInsnNode(ALOAD, 1)) // Decrypted Char Array
				add(MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V"))
				add(ARETURN)
			},
			InsnList().apply {
				add(throwNull)
				add(ldcInt(15))
				add(VarInsnNode(ISTORE, 8))
				add(JumpInsnNode(GOTO, rootSwitch))
			},
			InsnList().apply {
				add(catch)
				add(DUP)
				add(JumpInsnNode(IFNULL, pre15))
				add(ATHROW)
			},
			InsnList().apply {
				add(catch2)
				add(DUP)
				add(JumpInsnNode(IFNONNULL, pre15))
				add(ATHROW)
			},
			InsnList().apply {
				add(pre15)
				add(POP)
				add(switch15)
				add(ACONST_NULL)
				add(ATHROW)
				add(end)
			}
		).shuffled(random)
		
		for (block in blocks) {
			add(block)
		}
		
		
		methodNode.tryCatchBlocks.add(TryCatchBlockNode(
			opaqueTcbStart1,
			opaqueTcbEnd1,
			opaqueTcbHandler1,
			"java/lang/IllegalMonitorStateException"
		))
	}
}
