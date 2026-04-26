package com.example.morsedecoder.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.morsedecoder.domain.model.MorseMessage
import com.example.morsedecoder.domain.repository.ChatRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * 로컬 Wi-Fi 기반 채팅 기능을 구현한 리포지토리 클래스입니다.
 * NSD(Network Service Discovery)와 소켓 통신을 기반으로 동작합니다.
 */
class WifiChatRepositoryImpl(private val context: Context) : ChatRepository {
    /** 수신 및 발신된 모든 메시지를 관리하는 내부 상태 */
    private val _messages = MutableStateFlow<List<MorseMessage>>(emptyList())
    
    /** 전체 메시지 목록을 스트림 형태로 제공하는 Flow */
    override fun getMessages(): Flow<List<MorseMessage>> = _messages.asStateFlow()

    /** 탐색된 피어(상대방 기기)들의 서비스명, IP 및 포트 정보 맵 */
    private val _peers = MutableStateFlow<Map<String, Pair<String, Int>>>(emptyMap())
    
    /** 탐색된 피어 목록을 스트림 형태로 제공 (UI 등에서 활용 가능) */
    override fun getPeers(): Flow<Set<Pair<String, Int>>> = _peers.map { it.values.toSet() }

    override fun getWifiName(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid
            if (ssid == "<unknown ssid>" || ssid == "0x") "Connected Network" else ssid.removeSurrounding("\"")
        } catch (e: Exception) {
            "Local Network"
        }
    }

    /** 메시지 수신을 위한 서버 소켓 */
    private var serverSocket: ServerSocket? = null
    
    /** 서버 실행 상태 플래그 */
    private var isServerRunning = false
    
    /** 안드로이드 네트워크 서비스 탐색 관리자 */
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    /** 탐색할 서비스 유형 (DNS-SD) */
    private val serviceType = "_morsechat._tcp."
    
    /** 현재 기기가 네트워크 상에서 사용할 이름 */
    private var serviceName: String = "MorseUser_${Random().nextInt(1000)}"

    /** 백그라운드 작업을 위한 코루틴 스코프 (I/O 스케줄러 사용) */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 서버를 시작하고 로컬 포트에 바인딩합니다. 동시에 자신의 서비스를 네트워크에 등록합니다.
     */
    override fun startServer() {
        if (isServerRunning) return
        isServerRunning = true
        
        scope.launch {
            try {
                // 시스템에서 할당 가능한 로컬 포트로 서버 소켓 생성
                serverSocket = ServerSocket(0)
                val port = serverSocket!!.localPort
                registerService(port)
                
                // 루프를 돌며 클라이언트 접속을 대기
                while (isServerRunning) {
                    val socket = serverSocket?.accept()
                    socket?.let { handleClient(it) }
                }
            } catch (e: Exception) {
                Log.e("WifiChat", "Server Error", e)
            }
        }
        discoverPeers()
    }

    /**
     * 접속한 클라이언트 소켓으로부터 데이터를 수신하여 처리합니다.
     * @param socket 접속한 클라이언트 소켓
     */
    private fun handleClient(socket: Socket) {
        scope.launch {
            try {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val line = input.readLine()
                if (line != null) {
                    // 데이터 포맷: "발신자|텍스트|모스부호"
                    val parts = line.split("|")
                    if (parts.size >= 3) {
                        val newMessage = MorseMessage(
                            id = UUID.randomUUID().toString(),
                            sender = parts[0],
                            text = parts[1],
                            morse = parts[2],
                            timestamp = System.currentTimeMillis(),
                            isFromMe = false
                        )
                        _messages.value = _messages.value + newMessage
                    }
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("WifiChat", "Handle Client Error", e)
            }
        }
    }

    /**
     * 텍스트와 모스 부호를 로컬 메시지 목록에 추가하고, 탐색된 모든 피어에게 전송합니다.
     * @param text 일반 텍스트 메시지
     * @param morse 변환된 모스 부호 제이터
     */
    override suspend fun sendMessage(text: String, morse: String) {
        val newMessage = MorseMessage(
            id = UUID.randomUUID().toString(),
            sender = "Me",
            text = text,
            morse = morse,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        _messages.value = _messages.value + newMessage
        
        val messageToSend = "$serviceName|$text|$morse"
        
        // 탐색된 모든 피어에게 메시지 브로드캐스팅
        _peers.value.values.forEach { (ip, port) ->
            scope.launch {
                try {
                    val socket = Socket(ip, port)
                    val out = PrintWriter(socket.getOutputStream(), true)
                    out.println(messageToSend)
                    socket.close()
                } catch (e: Exception) {
                    Log.e("WifiChat", "Send Error to $ip:$port", e)
                }
            }
        }
    }

    /**
     * 자신의 서비스를 네트워크에 공시(Advertise)하여 다른 기기가 찾을 수 있도록 합니다.
     * @param port 서버 소켓이 열려 있는 포트 번호
     */
    private fun registerService(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = this@WifiChatRepositoryImpl.serviceName
            serviceType = this@WifiChatRepositoryImpl.serviceType
            setPort(port)
        }
        
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(regInfo: NsdServiceInfo) {
                serviceName = regInfo.serviceName
            }
            override fun onRegistrationFailed(arg0: NsdServiceInfo, arg1: Int) {}
            override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
            override fun onUnregistrationFailed(arg0: NsdServiceInfo, arg1: Int) {}
        })
    }

    /**
     * 서버 작동을 중지하고 리소스를 정리합니다.
     */
    override fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }

    /**
     * 네트워크 상에서 동작 중인 다른 모스 채팅 서비스들을 지속적으로 탐색합니다.
     */
    override fun discoverPeers() {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onServiceFound(service: NsdServiceInfo) {
                // 동일한 서비스 타입이면서 본인이 아닌 경우만 확인
                if (service.serviceType == serviceType && service.serviceName != serviceName) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            // 피어 IP와 포트 정보를 목록에 추가
                            _peers.value = _peers.value + (serviceInfo.serviceName to (serviceInfo.host.hostAddress!! to serviceInfo.port))
                        }
                    })
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                // 서비스 손실 시 목록에서 제거
                _peers.value = _peers.value - service.serviceName
            }
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    /**
     * 수동 연결 기능 (현재는 자동 탐색을 사용하여 구현하지 않음)
     */
    override fun connectToPeer(ipAddress: String) {
        // Not needed for this auto-broadcast model
    }
}

