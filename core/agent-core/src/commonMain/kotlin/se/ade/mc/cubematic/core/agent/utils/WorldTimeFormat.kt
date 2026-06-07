package se.ade.mc.cubematic.core.agent.utils

import kotlin.ranges.contains

class WorldTimeFormat(val value: Long) {
	/**
	 * Returns the current time period, and seconds remaining until next period, e.g. "morning (123 seconds until noon)"
	 *
	 * ticks/second: 20
	 * 		0 ticks (06:00) – sunrise start, dawn.
	 * 		1000 ticks (07:00) – daytime start, morning.
	 * 		6000 ticks (12:00) – noon, midday.
	 * 		12000 ticks (18:00) – sunset start, dusk.
	 * 		13000 ticks (19:00) – night start.
	 * 		18000 ticks (00:00) – midnight.
	 * 		24000 ticks (06:00) – next sunrise, day cycle repeats.
	 */
	fun getTimeString(): String {
		val dayTime = value
		val (period, nextPeriodTime) = when {
			dayTime in 0 until 1000 -> "dawn" to 1000
			dayTime in 1000 until 6000 -> "morning" to 6000
			dayTime in 6000 until 12000 -> "noon" to 12000
			dayTime in 12000 until 13000 -> "dusk" to 13000
			dayTime in 13000 until 18000 -> "night" to 18000
			dayTime in 18000 until 24000 -> "midnight" to 24000
			else -> "unknown" to 0
		}
		val nextPeriod = mapOf(
			"dawn" to "morning",
			"morning" to "noon",
			"noon" to "dusk",
			"dusk" to "night",
			"night" to "midnight",
			"midnight" to "dawn",
		)

		val ticksUntilNext = if(nextPeriodTime >= dayTime) {
			nextPeriodTime - dayTime
		} else {
			24000 - dayTime + nextPeriodTime
		}
		val secondsUntilNext = ticksUntilNext / 20
		return "$period ($secondsUntilNext seconds until ${nextPeriod[period]})"
	}
}