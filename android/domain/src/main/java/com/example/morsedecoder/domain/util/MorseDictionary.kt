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

    private val prosignMap = mapOf(
        ".-.-." to "AR: End of Message",
        ".-..." to "AS: Wait/Stand by",
        "-.-" to "K: Go ahead / Over",
        "-.-.-" to "KN: Go ahead (specific station)",
        "...-.-" to "SK: End of Work / Clear",
        "-...-" to "BT: Break / Separator",
        ".-." to "R: Roger / Received",
        "...---..." to "SOS: Distress Call",
        "........" to "Error",
        ".-.-" to "New Line",
        "--.- - ...." to "QTH: Your location?",
        "--.- ... .-.." to "QSL: Acknowledge receipt",
        "--... ...--" to "73: Best regards",
        "-.-. --.-" to "CQ: Calling any station",
        "--.- .-. -" to "QRT: Stop sending"
    )

    /**
     * 입력을 모스 부호로 변환합니다.
     */
    fun encode(text: String): String {
        return text.uppercase().map { char ->
            if (char == ' ') "" // Space is represented as an empty segment to maintain index
            else textToMorseMap[char.toString()] ?: ""
        }.joinToString(" ")
    }

    /**
     * 단일 모스 부호 뭉치를 텍스트 문자로 변환합니다.
     */
    fun decodeChar(morse: String): String {
        if (morse.isEmpty()) return " "
        val meaning = prosignMap[morse]
        if (meaning != null) {
            val label = meaning.substringBefore(":")
            if (label.length == 1) return label // K, R 등은 문자로 표시
            return "[$label]"
        }
        return morseToTextMap[morse] ?: "?"
    }

    /**
     * 모스 부호의 의미(설명)를 반환합니다.
     */
    fun getMeaning(morse: String): String? {
        return prosignMap[morse]
    }
    
    /**
     * 모든 프로사인 맵을 반환합니다.
     */
    fun getAllProsigns(): Map<String, String> = prosignMap

    /**
     * 전체 모스 부호 문장을 텍스트로 변환합니다.
     */
    fun decodeSentence(morseSentence: String): String {
        if (morseSentence.isEmpty()) return ""
        return morseSentence.split(" ").joinToString("") { decodeChar(it) }
    }
}
