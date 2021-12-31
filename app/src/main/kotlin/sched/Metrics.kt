package sched

import main.processes

private fun turnAroundTimes(gantt: Gantt): Map<Int, Int> {
	val arrivalTimes = processes.sortedBy { it.id }.map { it.arrivalTime }
	val finishTimes = finishTimes(gantt).entries.sortedBy { it.key }.map { it.value }

	return finishTimes.zip(arrivalTimes).mapIndexed { i, it -> i to (it.first - it.second) }.toMap()
}

private fun finishTimes(gantt: Gantt): Map<Int, Int> {
	val finishTimesMap = processes.map { it.id }.associateWith { 0 }.toMutableMap()

	var currentTime = 0
	gantt.entries().forEach {
		currentTime += it.time
		finishTimesMap.replace(it.pid, currentTime)
	}

	return finishTimesMap
}

private fun waitingTimes(g: Gantt): Map<Int, Int> {
	val serviceTimes = processes.sortedBy { it.id }.map { it.serviceTime }
	val turnAroundTimes = turnAroundTimes(g).entries.sortedBy { it.key }.map { it.value }

	return turnAroundTimes.zip(serviceTimes).mapIndexed { i, it -> i to (it.first - it.second) }.toMap()
}

fun avgTurnAroundTime(gantt: Gantt): Double {
	return turnAroundTimes(gantt).values.average()
}

fun avgWaitingTime(g: Gantt): Double {
	return waitingTimes(g).values.average()
}
