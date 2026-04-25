package com.example.morsedecoder.domain.repository

import com.example.morsedecoder.domain.model.MorseMessage
import kotlinx.coroutines.flow.Flow

/**
 * 채팅 리포지토리 인터페이스: 로컬 네트워크 기반 채팅 기능을 정의합니다.
 */
interface ChatRepository {
    /**
     * 전체 메시지 목록을 관찰 가능한 Flow로 반환합니다.
     */
    fun getMessages(): Flow<List<MorseMessage>>

    /**
     * 텍스트와 모스 부호를 다른 피어(Peer)들에게 전송합니다.
     */
    suspend fun sendMessage(text: String, morse: String)

    /**
     * 로컬 서버를 가동하고 주변 기기의 접속을 대기합니다.
     */
    fun startServer()

    /**
     * 가동 중인 서버를 중단하고 자원을 해제합니다.
     */
    fun stopServer()

    /**
     * 같은 네트워크 상의 다른 모스 채팅 서비스들을 탐색합니다.
     */
    fun discoverPeers()

    /**
     * 특정 IP 주소의 피어와 연결을 시도합니다 (현재 구현에서는 자동 탐색 기반).
     */
    fun connectToPeer(ipAddress: String)
}
