package util

import java.util.regex.Pattern

private val fr = Pattern.compile("0*$").toRegex()
private val sr = Pattern.compile("\\.$").toRegex()
fun Double.toStringRemoveTrailingZeros(): String = toString().replace(fr, "").replace(sr, "")