package ru.blays.hub.core.logic.utils

@JvmInline
value class VersionName private constructor(private val parts: List<Int>) {
    operator fun compareTo(other: VersionName): Int {
        val otherParts = other.parts

        val maxIndex = maxOf(parts.lastIndex, otherParts.lastIndex)

        for(i in 0..maxIndex) {
            val otherPart = otherParts.getOrNull(i)
            val thisPart = parts.getOrNull(i)

            if(thisPart == otherPart) continue
            if(thisPart == null) return -1
            if(otherPart == null) return 1
            return if(thisPart < otherPart) -1 else 1
        }

        return 0
    }

    override fun toString(): String {
        return parts.joinToString(
            separator = ".",
            prefix = "Version name: "
        )
    }

    companion object {
        fun String.toVersionName(): VersionName? {
            val parts = split('.')
                .map { part ->
                    part.replace(notNumberRegex, "")
                        .toIntOrDefault(0)
                }
            if(parts.isEmpty()) return null
            return VersionName(parts)
        }

        private val notNumberRegex by lazy { Regex("[^0-9]+") }
    }
}

