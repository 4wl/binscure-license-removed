package dev.binclub.binscure.utils

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier
import java.security.SecureRandom
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * @author cookiedragon234 20/Jan/2020
 */
fun String.replaceLast(oldChar: Char, newChar: Char): String {
	return this.replaceLast(oldChar, newChar.toString())
}

fun String.replaceLast(oldChar: Char, newString: String): String {
	return when (val index = lastIndexOf(oldChar)) {
		-1 -> this
		else -> this.replaceRange(index, index + 1, newString)
	}
}

infix fun Char.xor(int: Int): Char {
	return (this.toInt() xor int).toChar()
}

fun KClass<*>.getDescriptor(): String = Type.getDescriptor(this.java)

fun CharArray.random(random: SecureRandom): Char {
	if (isEmpty())
		throw NoSuchElementException("Array is empty.")
	return get(random.nextInt(size))
}

fun String.clampStart(length: Int, padding: Char = ' '): String {
	return when {
		this.length == length -> this
		this.length > length -> this.substring(0, length)
		else -> this.padStart(length, padding)
	}
}

fun String.clampEnd(length: Int, padding: Char = ' '): String {
	return when {
		this.length == length -> this
		this.length > length -> this.substring(0, length)
		else -> this.padEnd(length, padding)
	}
}

fun <K, V> Map<K, V>.toPrettyString(): String {
	val sb = StringBuilder("Map(${this.size}):\n")
	for (entry in this) {
		sb.append('\t')
		sb.append(entry.key)
		sb.append("=")
		if (entry.value is Map<*, *>) {
			sb.append((entry.value as Map<*, *>).toPrettyString().prependIndent("\t"))
		} else {
			sb.append(entry.value)
		}
		sb.append('\n')
	}
	return sb.removeSuffix("\n").toString()
}

fun <T> Collection<T>.random(random: SecureRandom): T {
	if (isEmpty())
		throw NoSuchElementException("Collection is empty.")
	return elementAt(random.nextInt(size))
}

fun <T> Array<T>.random(random: SecureRandom): T {
	if (isEmpty())
		throw NoSuchElementException("Collection is empty.")
	return elementAt(random.nextInt(size))
}

inline fun <reified T: Any> Any?.cast(type: KClass<T>): T = this as T
inline fun <reified T: Any> Any?.cast(type: Class<T>): T = this as T
inline fun <reified T: Any> Any?.cast(): T = this as T

inline val <T: Any> KClass<T>.internalName: String
	inline get() = Type.getInternalName(this.java)

inline val KFunction<*>.descriptor: String
	inline get() {
		val params = parameters.map { Type.getType(it.type.classifier.cast(KClass::class).java) }
		val returnType = Type.getType(returnType.classifier.cast(KClass::class).java)
		return Type.getMethodDescriptor(returnType, *params.toTypedArray())
	}
inline val KProperty<*>.descriptor: String
	inline get() {
		val returnType = returnType.classifier.cast(KClass::class).java
		return Type.getDescriptor(returnType)
	}

//public infix fun Int.xor(other: Int): Int = this.xor(other)

fun Handle.toInsn() = MethodInsnNode(this.tag, this.owner, this.name, this.desc, this.isInterface)

fun MethodNode.isStatic() = Modifier.isStatic(this.access)

inline fun <T> T?.ifNotNull(block: (T) -> Unit) = this.whenNotNull(block)

inline fun <T> T?.whenNotNull(block: (T) -> Unit): T? {
	if (this != null) {
		block(this)
	}
	return this
}

fun InsnList.add(opcode: Int) = this.add(InsnNode(opcode))

fun InsnList.clone(): InsnList {
	val clonedLabels = object: Map<LabelNode, LabelNode> {
		val inner = HashMap<LabelNode, LabelNode>()
		
		override val entries: Set<Map.Entry<LabelNode, LabelNode>>
			get() = TODO("Not yet implemented")
		override val keys: Set<LabelNode>
			get() = TODO("Not yet implemented")
		override val size: Int
			get() = TODO("Not yet implemented")
		override val values: Collection<LabelNode>
			get() = TODO("Not yet implemented")
		
		override fun containsKey(key: LabelNode): Boolean = TODO("Not yet implemented")
		override fun containsValue(value: LabelNode): Boolean = TODO("Not yet implemented")
		override fun isEmpty(): Boolean = TODO("Not yet implemented")
		
		override fun get(key: LabelNode): LabelNode?
			 = inner.getOrPut(key) {
				LabelNode()
			}
		
	}
	return InsnList().also {
		for (insn in this) {
			it.add(insn.clone(clonedLabels))
		}
	}
}

fun Int.removeAccess(access: Int) = this and access.inv()
fun Int.addAccess(access: Int) = this or access
fun Int.hasAccess(access: Int) = this and access != 0

fun AbstractInsnNode.opcodeString(): String {
	when (this) {
		is BlameableLabelNode -> return this.toString()
		is LabelNode -> return "lbl_${hashCode()}"
		is JumpInsnNode -> return "${implOpToStr(opcode)}: ${label.opcodeString()}"
		is VarInsnNode -> return "${implOpToStr(opcode)} $`var`"
		is FieldInsnNode -> return "${implOpToStr(opcode)} $owner.$name$desc"
		is MethodInsnNode -> return "${implOpToStr(opcode)} $owner.$name$desc"
		is TypeInsnNode -> return "${implOpToStr(opcode)} $desc"
		is InvokeDynamicInsnNode -> {
			return buildString {
				append("$name.$desc -> ${bsm.owner}.${bsm.name}${bsm.desc}")
				append(" : (")
				for (arg in bsmArgs) {
					append("$arg, ")
				}
				append(")")
			}
		}
		else -> {
			if (opcode == -1) return this::class.java.simpleName ?: this::class.java.name!!
			return implOpToStr(opcode)
		}
	}
}

private fun implOpToStr(op: Int): String {
	return opcodeStrings.getOrDefault(op, "0x${Integer.toHexString(op)} <invalid>")
}

fun InsnList.toOpcodeStrings(highlight: AbstractInsnNode? = null, info: Map<AbstractInsnNode, Any?>? = null): String {
	val insnList = this
	return buildString {
		for ((i, insn) in insnList.iterator().withIndex()) {
			append("\t $i: ${insn.opcodeString()}")
			info?.get(insn)?.let {
				append(" ($it)")
			}
			if (highlight == insn) {
				append(" <---------------------- HERE")
			}
			append('\n')
		}
		//println(this)
	}
}

fun ClassNode.getVersion(): Pair<Int, Int> {
	return (this.version and 0xFFFF) to (this.version shr 16)
}

fun ClassNode.versionAtLeast(minVersion: Int): Boolean {
	val thisMajor = this.version and 0xFFFF
	val minMajor = minVersion and 0xFFFF
	
	return thisMajor >= minMajor
}

fun ClassNode.versionAtMost(maxVersion: Int): Boolean {
	val thisMajor = this.version and 0xFFFF
	val maxMajor = maxVersion and 0xFFFF
	
	return thisMajor <= maxMajor
}

val Type.doubleSize: Boolean
	get() = (this.sort == Type.DOUBLE || this.sort == Type.LONG)

fun <T> Stack<T>.cloneStack(): Stack<T> {
	return Stack<T>().also {
		it.addAll(this)
	}
}

val AbstractInsnNode.isAsmInsn
	get() = this.opcode < 0

fun Stack<Type>.typesEqual(other: Stack<Type>): Boolean {
	if (this.size != other.size) return false
	
	for (i in this.indices) {
		val type1 = this[i]
		val type2 = other[i]
		
		if (type1.descriptor == type2.descriptor) return true
		
		if (
			(type1.descriptor == "Lnull;" && type2.sort == Type.OBJECT)
			||
			(type2.descriptor == "Lnull;" && type1.sort == Type.OBJECT)
		) return true
	}
	
	return false
}

inline fun <reified T> fixedSizeList(size: Int): List<T?> = listOf(*arrayOfNulls<T>(size))

inline fun <T> block(block: () -> T): T = block()

fun InsnList.addLineNumber(lineNumber: Int) {
	val lbl = newLabel()
	add(lbl)
	add(LineNumberNode(lineNumber, lbl))
}

fun InsnList.populateWithLineNumbers() {
	for ((i, insn) in this.iterator().withIndex()) {
		if (!insn.isAsmInsn) {
			this.insertBefore(insn, InsnList().apply {
				addLineNumber(i)
			})
		}
	}
}

fun AbstractInsnNode.constantValue() : Any? {
	return when (this) {
		is LdcInsnNode -> this.cst
		is InsnNode -> {
			when (this.opcode) {
				ICONST_0 -> 0
				ICONST_M1 -> -1
				ICONST_1 -> 1
				ICONST_2 -> 2
				ICONST_3 -> 3
				ICONST_4 -> 4
				ICONST_5 -> 5
				LCONST_0 -> 0L
				LCONST_1 -> 1L
				FCONST_0 -> 0.0f
				FCONST_1 -> 1.0f
				FCONST_2 -> 2.0f
				DCONST_0 -> 0.0
				DCONST_1 -> 1.0

				else -> null
			}
		}
		else -> null
	}
}
