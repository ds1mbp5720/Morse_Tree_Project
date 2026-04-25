package com.example.morsedecoder.domain.model

/**
 * 도메인 모델: 모스 부호 메시지 정보를 담는 데이터 클래스
 */
data class MorseMessage(
    /** 메시지 고유 식별자 */
    val id: String,
    /** 해독된 텍스트 내용 */
    val text: String,
    /** 메시지에 해당하는 모스 부호 */
    val morse: String,
    /** 메시지 발신자 이름 */
    val sender: String,
    /** 메시지 생성 시각 (밀리초) */
    val timestamp: Long,
    /** 사용자가 직접 보낸 메시지인지 여부 */
    val isFromMe: Boolean
)
