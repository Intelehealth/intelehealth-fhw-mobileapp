package org.intelehealth.app.utilities

class SoundexHelper {
    companion object{

        @JvmStatic
        fun encode(name: String): String {
            if (name.isEmpty()) return ""

            val map = mapOf(
                'B' to '1', 'F' to '1', 'P' to '1', 'V' to '1',
                'C' to '2', 'G' to '2', 'J' to '2', 'K' to '2', 'Q' to '2', 'S' to '2', 'X' to '2', 'Z' to '2',
                'D' to '3', 'T' to '3',
                'L' to '4',
                'M' to '5', 'N' to '5',
                'R' to '6'
            )

            val upperName = name.uppercase()
            val firstLetter = upperName[0]

            val encoded = StringBuilder()
            var lastDigit: Char? = null

            for (char in upperName.drop(1)) {
                val digit = map[char]
                if (digit != null && digit != lastDigit) {
                    encoded.append(digit)
                    lastDigit = digit
                } else if (digit == null) {
                    lastDigit = null
                }
            }

            val code = firstLetter + encoded.toString().padEnd(3, '0')
            return code.substring(0, 4)
        }
    }
}