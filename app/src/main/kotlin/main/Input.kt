package main

import sched.Process
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

private lateinit var pProcesses: Set<Process>
val processes by lazy {
	readInput()
	pProcesses
}

private var pFirstQuantumTime: Int = 0
val firstQuantumTime by lazy {
	readInput()
	pFirstQuantumTime
}

private var pSecondQuantumTime = 0
val secondQuantumTime by lazy {
	readInput()
	pSecondQuantumTime
}

private val read = AtomicBoolean()
fun readInput() {
	if (read.compareAndSet(false, true)) {
		val n =
			readln().let { requireNotNull(it.toIntOrNull()) { "processes' quantity myst be a 32-bit integer (was \"$it\")" } }
		require(n >= 0) { "processes' quantity must be non-negative (was $n)" }

		val delim = Pattern.compile("[ \t]*,[ \t]*").toRegex() // ',' delimiter with any spaces around
		val sts = readln().split(delim).map {
			requireNotNull(it.toIntOrNull()) { "each process's service time must be a 32-bit integer (one was \"$it\")" }
		}
		val ats = readln().split(delim).map {
			requireNotNull(it.toIntOrNull()) { "each process's arrival time must be a 32-bit integer (one was \"$it\")" }
		}
		require(sts.size == n) { "processes' quantity mismatch; found ${sts.size} service times (n=$n)" }
		require(ats.size == n) { "processes' quantity mismatch; found ${ats.size} arrival times (n=$n)" }
		sts.forEach {
			require(it >= 0) { "each process's service time must be non-negative (one was $it)" }
		}

		readln().let {
			pFirstQuantumTime =
				requireNotNull(it.toIntOrNull()) { "quantum time must be a 32-bit integer (first one was \"$it\")" }
		}
		readln().let {
			pSecondQuantumTime =
				requireNotNull(it.toIntOrNull()) { "quantum time must be a 32-bit integer (second one was \"$it\")" }
		}

		require(firstQuantumTime > 0) { "quantum time must be positive (first one was $firstQuantumTime)" }
		require(secondQuantumTime > 0) { "quantum time must be positive (second one was $secondQuantumTime)" }

		pProcesses = ats.zip(sts).mapIndexed { i, it -> Process(i, it.first, it.second) }.toHashSet()
	}
}