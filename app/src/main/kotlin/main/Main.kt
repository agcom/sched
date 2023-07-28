package main

import sched.*
import util.toStringRemoveTrailingZeros
import kotlin.system.exitProcess

fun main() {
	// Avoid leaking eye damaging exceptions to clients
	runCatching {
		p("Shortest remaining time first") { strf(processes) }
		p("Highest response ratio next") { hrrn(processes) }
		p("Round-robin") { rr(processes, firstQuantumTime) }
		p("Multilevel feedback queue") { mlfq(processes, firstQuantumTime, secondQuantumTime) }
	}.onFailure {
		System.err.println(if (it.message != null) "Error: ${it.message}" else "Error: unknown")
		exitProcess(1)
	}
}

private fun p(title: String, ganttProducer: () -> Gantt) {
	val g = ganttProducer()
	
	println(
		"""
		$title:
			Gantt: $g
			Avg. TT: ${avgTurnAroundTime(g).toStringRemoveTrailingZeros()}
			Avg. WT: ${avgWaitingTime(g).toStringRemoveTrailingZeros()}
	""".trimIndent()
	)
}

