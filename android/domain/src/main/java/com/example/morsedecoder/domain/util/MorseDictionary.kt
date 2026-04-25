package com.example.morsedecoder.domain.util

/**
 * 모스 부호 사전: 텍스트와 모스 부호 간의 매핑을 관리합니다.
 */
object MorseDictionary {
    private val textToMorseMap = mapOf(
        // 알파벳
        "A" to ".-", "B" to "-...", "C" to "-.-.", "D" to "-..", "E" to ".", "F" to "..-.",
        "G" to "--.", "H" to "....", "I" to "..", "J" to ".---", "K" to "-.-", "L" to ".-..",
        "M" to "--", "N" to "-.", "O" to "---", "P" to ".--.", "Q" to "--.-", "R" to ".-.",
        "S" to "...", "T" to "-", "U" to "..-", "V" to "...-", "W" to ".--", "X" to "-..-",
        "Y" to "-.--", "Z" to "--..",
        // 숫자
        "0" to "-----", "1" to ".----", "2" to "..---", "3" to "...--", "4" to "....-",
        "5" to ".....", "6" to "-....", "7" to "--...", "8" to "---..", "9" to "----.",
        // 특수문자
        "." to ".-.-.-", "," to "--..--", "?" to "..--..", "'" to ".----.", "!" to "-.-.--",
        "/" to "-..-.", "(" to "-.--.", ")" to "-.--.-", "&" to ".-...", ":" to "---...",
        ";" to "-.-.-.", "=" to "-...-", "+" to ".-.-.", "-" to "-....-", "_" to "..--.-",
        "\"" to ".-..-.", "$" to "...-..-", "@" to ".--.-."
    )

    private val morseToTextMap = textToMorseMap.entries.associate { it.value to it.key }

    /**
     * 입력을 모스 부호로 변환합니다.
     */
    fun encode(text: String): String {
        return text.uppercase().map { char ->
            textToMorseMap[char.toString()] ?: ""
        }.filter { it.isNotEmpty() }.joinToString(" ")
    }

    /**
     * 단일 모스 부호 뭉치를 텍스트 문자로 변환합니다.
     */
    fun decodeChar(morse: String): String {
        return morseToTextMap[morse] ?: "?"
    }

    /**
     * 전체 모스 부호 문장을 텍스트로 변환합니다.
     */
    fun decodeSentence(morseSentence: String): String {
        return morseSentence.split(" ").joinToString("") { decodeChar(it) }
    }
}
