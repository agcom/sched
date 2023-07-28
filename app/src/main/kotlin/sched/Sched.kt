package sched

import java.util.*

private abstract class AbstractSched(processes: Set<Process>) {
	protected val processesArrivalQueue: Queue<Process>
	protected var currentTime: Int = 0
	val gantt = Gantt()
	
	init {
		processesArrivalQueue = PriorityQueue(processes.size,
			Comparator.comparingInt<Process> { it.arrivalTime }.thenComparingInt { it.id })
		processesArrivalQueue.addAll(processes.filterNot { it.serviceTime == 0 })
	}
	
	protected fun addArrivedProcesses() {
		while (processesArrivalQueue.isNotEmpty() && processesArrivalQueue.element()!!.arrivalTime <= currentTime) {
			addFreshProcessToReadyQueue(processesArrivalQueue.remove()!!)
		}
	}
	
	fun schedule() {
		// Add processes arrived earlier than 0 (or at 0) to the ready queue
		addArrivedProcesses()
		
		while (!isReadyQueueEmpty() || processesArrivalQueue.isNotEmpty()) {
			if (isReadyQueueEmpty()) {
				jumpToNextArrivalTime()
				addArrivedProcesses()
			} else {
				burst()
			}
		}
	}
	
	protected abstract fun isReadyQueueEmpty(): Boolean
	
	protected abstract fun addFreshProcessToReadyQueue(process: Process)
	
	protected abstract fun burst()
	
	protected fun jumpToNextArrivalTime() {
		val nextArrivalProcess = processesArrivalQueue.remove()!!
		val nextArrivalTime = nextArrivalProcess.arrivalTime
		
		if (nextArrivalTime < currentTime) error("some processes already arrived (next arrival time is in the past)")
		else {
			gantt += -1 to nextArrivalTime - currentTime
			currentTime = nextArrivalTime
		}
	}
}

private class HRRN(processes: Set<Process>) : AbstractSched(processes) {
	private val readyQueue: Queue<HRRNProcess> = PriorityQueue(processes.size)
	
	override fun isReadyQueueEmpty(): Boolean = readyQueue.isEmpty()
	
	override fun burst() {
		// Run the process on top of the ready queue until it finishes (non-preemptive)
		val toRunProcess = readyQueue.remove()!!
		gantt += toRunProcess.id to toRunProcess.serviceTime
		currentTime += toRunProcess.serviceTime
		
		// Update ready queue processes' waiting times
		readyQueue.toList().onEach { it.waitingTime += toRunProcess.serviceTime }.let { updatedReadyQueueProcesses ->
			readyQueue.clear()
			readyQueue.addAll(updatedReadyQueueProcesses)
		}
		
		// Add processes arrived in between (consider their actual waiting time)
		while (processesArrivalQueue.isNotEmpty() && processesArrivalQueue.element()!!.arrivalTime <= currentTime) {
			val toAddProcess = processesArrivalQueue.remove()!!
			addFreshProcessToReadyQueue(toAddProcess, currentTime - toAddProcess.arrivalTime)
		}
	}
	
	override fun addFreshProcessToReadyQueue(process: Process) {
		addFreshProcessToReadyQueue(process, 0)
	}
	
	private fun addFreshProcessToReadyQueue(process: Process, waitingTime: Int) {
		readyQueue.add(HRRNProcess(process.id, waitingTime, process.serviceTime))
	}
	
	private data class HRRNProcess(
		val id: Int, var waitingTime: Int, val serviceTime: Int
	) : Comparable<HRRNProcess> {
		companion object {
			val comparator: Comparator<HRRNProcess> =
				compareByDescending<HRRNProcess> { it.responseRatio }.thenComparingInt { it.id }
		}
		
		val responseRatio: Double
			get() = waitingTime / serviceTime.toDouble()
		
		override fun hashCode(): Int = id.hashCode()
		override fun equals(other: Any?): Boolean = other is HRRNProcess && id == other.id
		
		override fun compareTo(other: HRRNProcess): Int = comparator.compare(this, other)
	}
}

private class RR(processes: Set<Process>, private val quantumTime: Int) : AbstractSched(processes) {
	private val readyQueue: Queue<RRProcess> = ArrayDeque(processes.size)
	
	override fun burst() {
		// Run the process on top of the ready queue until it finishes or its quantum is over (preempt)
		val toRunProcess = readyQueue.remove()!!
		val burstTime =
			if (toRunProcess.remainingTime <= quantumTime) toRunProcess.remainingTime // Run till it finishes
			else quantumTime // Run till its quantum is over
		
		gantt += toRunProcess.id to burstTime
		currentTime += burstTime
		toRunProcess.remainingTime -= burstTime
		
		// Add processes arrived in between
		addArrivedProcesses()
		
		if (toRunProcess.remainingTime > 0) readyQueue.add(toRunProcess)
	}
	
	override fun isReadyQueueEmpty(): Boolean = readyQueue.isEmpty()
	
	override fun addFreshProcessToReadyQueue(process: Process) {
		readyQueue.add(RRProcess(process.id, process.serviceTime))
	}
	
	private data class RRProcess(
		val id: Int, var remainingTime: Int
	) : Comparable<RRProcess> {
		override fun hashCode(): Int = id.hashCode()
		override fun equals(other: Any?): Boolean = other is RRProcess && id == other.id
		
		override fun compareTo(other: RRProcess): Int = id.compareTo(other.id)
	}
}

private class STRF(processes: Set<Process>) : AbstractSched(processes) {
	private val readyQueue: Queue<STRFProcess> = PriorityQueue(processes.size)
	
	override fun burst() {
		// Run the process on top of the ready queue until it finishes or a new process arrives (preempt)
		val toRunProcess = readyQueue.remove()!!
		val predictedFinishTime = currentTime + toRunProcess.remainingTime
		val nextArrivalTime = processesArrivalQueue.peek()?.arrivalTime
		
		val burstTime =
			if (nextArrivalTime == null || nextArrivalTime > predictedFinishTime) toRunProcess.remainingTime // Run till it finishes
			else {
				// Run till next arrival time, and add the arrived task.
				addFreshProcessToReadyQueue(processesArrivalQueue.remove()!!)
				nextArrivalTime - currentTime
			}
		
		gantt += toRunProcess.id to burstTime
		currentTime += burstTime
		toRunProcess.remainingTime -= burstTime
		
		if (toRunProcess.remainingTime > 0) readyQueue.add(toRunProcess)
	}
	
	override fun isReadyQueueEmpty(): Boolean = readyQueue.isEmpty()
	
	override fun addFreshProcessToReadyQueue(process: Process) {
		readyQueue.add(STRFProcess(process.id, process.serviceTime))
	}
	
	private data class STRFProcess(
		val id: Int, var remainingTime: Int
	) : Comparable<STRFProcess> {
		companion object {
			val comparator = compareBy<STRFProcess> { it.remainingTime }.thenComparingInt { it.id }
		}
		
		override fun hashCode(): Int = id.hashCode()
		override fun equals(other: Any?): Boolean = other is STRFProcess && id == other.id
		
		override fun compareTo(other: STRFProcess): Int = comparator.compare(this, other)
	}
}

private class MLFQ(
	processes: Set<Process>, private val firstQueueQuantumTime: Int, private val secondQueueQuantumTime: Int
) : AbstractSched(processes) {
	private val firstReadyQueue: Queue<MLFQProcess> = LinkedList() // RR with firstQueueQuantumTime
	private val secondReadyQueue: Queue<MLFQProcess> = LinkedList() // RR with secondQueueQuantumTime
	private val thirdReadyQueue: Queue<MLFQProcess> = LinkedList() // FCFS (first come, first served)
	
	override fun burst() {
		if (firstReadyQueue.isNotEmpty()) {
			// Run the process on top of the ready queue until it finishes or its quantum is over (preempt)
			val toRunProcess = firstReadyQueue.remove()!!
			val burstTime =
				if (toRunProcess.remainingTime <= firstQueueQuantumTime) toRunProcess.remainingTime // Run till it finishes
				else firstQueueQuantumTime // Run till its quantum is over
			
			gantt += toRunProcess.id to burstTime
			currentTime += burstTime
			toRunProcess.remainingTime -= burstTime
			
			// Add processes arrived in between
			addArrivedProcesses()
			
			if (toRunProcess.remainingTime > 0) secondReadyQueue.add(toRunProcess) // Move onto the next queue
		} else if (secondReadyQueue.isNotEmpty()) {
			// Run the process on top of the ready queue until it finishes or its quantum is over (preempt)
			val toRunProcess = secondReadyQueue.remove()!!
			val burstTime =
				if (toRunProcess.remainingTime <= secondQueueQuantumTime) toRunProcess.remainingTime // Run till it finishes
				else secondQueueQuantumTime // Run till its quantum is over
			
			gantt += toRunProcess.id to burstTime
			currentTime += burstTime
			toRunProcess.remainingTime -= burstTime
			
			// Add processes arrived in between
			addArrivedProcesses()
			
			if (toRunProcess.remainingTime > 0) thirdReadyQueue.add(toRunProcess) // Move onto the next queue
		} else { // thirdReadyQueue.isNotEmpty()
			// Run the process on top of the ready queue until it finishes
			val toRunProcess = thirdReadyQueue.remove()!!
			
			gantt += toRunProcess.id to toRunProcess.remainingTime
			currentTime += toRunProcess.remainingTime
			toRunProcess.remainingTime = 0
			
			// Add processes arrived in between
			addArrivedProcesses()
		}
	}
	
	override fun isReadyQueueEmpty(): Boolean =
		firstReadyQueue.isEmpty() && secondReadyQueue.isEmpty() && thirdReadyQueue.isEmpty()
	
	override fun addFreshProcessToReadyQueue(process: Process) {
		firstReadyQueue.add(MLFQProcess(process.id, process.serviceTime))
	}
	
	private data class MLFQProcess(
		val id: Int, var remainingTime: Int
	)
}

fun hrrn(processes: Set<Process>): Gantt {
	return HRRN(processes).apply { schedule() }.gantt
}

fun rr(processes: Set<Process>, quantumTime: Int): Gantt {
	return RR(processes, quantumTime).apply { schedule() }.gantt
}

fun strf(processes: Set<Process>): Gantt {
	return STRF(processes).apply { schedule() }.gantt
}

fun mlfq(processes: Set<Process>, firstQueueQuantumTime: Int, secondQueueQuantumTime: Int): Gantt {
	return MLFQ(processes, firstQueueQuantumTime, secondQueueQuantumTime).apply { schedule() }.gantt
}