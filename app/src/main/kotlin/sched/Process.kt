package sched

data class Process(
	val id: Int,
	val arrivalTime: Int,
	val serviceTime: Int
) : Comparable<Process> {
	override fun hashCode(): Int = id.hashCode()
	override fun equals(other: Any?): Boolean = other is Process && id == other.id
	
	override fun compareTo(other: Process): Int = id.compareTo(other.id)
}