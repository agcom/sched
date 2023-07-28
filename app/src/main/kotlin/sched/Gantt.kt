package sched

class Gantt {
	private var entries: MutableList<Entry> = mutableListOf()
	
	fun add(pid: Int, time: Int) {
		add(Entry(pid, time))
	}
	
	fun add(pair: Pair<Int, Int>) = add(pair.first, pair.second)
	
	fun add(entry: Entry) {
		if (entry.time == 0) return
		else if (entries.isNotEmpty() && entry == entries.last()) { // Merge
			val l = entries.removeLast()
			entries.add(entry.copy(time = l.time + entry.time))
		} else entries.add(entry)
	}
	
	fun entries(): List<Entry> = entries
	
	operator fun plusAssign(entry: Entry) = add(entry)
	
	operator fun plusAssign(pair: Pair<Int, Int>) = add(pair)
	
	override fun toString(): String {
		val sb = StringBuilder()
		var ct = 0
		
		entries.forEach {
			sb.append('(').append(ct).append(')').append(' ').apply {
				if (it.pid == -1) append("Idle")
				else append('P').append(it.pid)
			}.append(' ')
			ct += it.time
		}
		sb.append('(').append(ct).append(')')
		
		return sb.toString()
	}
	
	data class Entry(
		val pid: Int,
		val time: Int
	) {
		override fun hashCode(): Int = pid.hashCode()
		override fun equals(other: Any?): Boolean = other is Entry && pid == other.pid
	}
}