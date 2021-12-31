# Sched

[![Usage](https://asciinema.org/a/sLXMnvwrGpQoWJHY3jltyMuJA.svg)](https://asciinema.org/a/sLXMnvwrGpQoWJHY3jltyMuJA)

An application that produces [Gantt charts](https://en.wikipedia.org/wiki/Gantt_chart) for some process scheduling algorithms:

- [Shortest remaining time first](https://en.wikipedia.org/wiki/Shortest_remaining_time) (preemptive version of [SJF](https://en.wikipedia.org/wiki/Shortest_job_next))
- [Highest response ratio next](https://en.wikipedia.org/wiki/Highest_response_ratio_next)
- [Round-robin](https://en.wikipedia.org/wiki/Round-robin_scheduling)
- [Multilevel feedback queue](https://en.wikipedia.org/wiki/Multilevel_feedback_queue) (RR, RR, and [FCFS](https://en.wikipedia.org/wiki/FIFO_(computing_and_electronics)))

The project was made as an assessment  for an **operating systems** college course.

## Simple build & run

Open the project directory in a terminal, and then issue the following command to build and run the application.

- Linux:

	```sh
	cat example-input.txt | ./gradlew -q --console=plain :app:run
	```

- Windows:

	```powershell
	cat example-input.txt | gradlew.bat -q --console=plain :app:run
	```

> Note: The appropriate versions of Gradle and JDK (plus other dependencies) will be downloaded if they're not installed.

### Example input description

```
5
3, 6, 4, 5, 2
0, 2, 4, 6, 8
1
2

```

Line-by-line description:

1. Processes' quantity
2. Processes' service times
3. Processes' arrival times
4. First quantum time (for RR, and MLFQ's first RR queue)
5. Second quantum time (for MLFQ's second RR queue)
