package com.example.morsedecoder.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.morsedecoder.domain.model.MorseMessage
import com.example.morsedecoder.domain.repository.ChatRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class WifiChatRepositoryImpl(private val context: Context) : ChatRepository {
    private val _messages = MutableStateFlow<List<MorseMessage>>(emptyList())
    override fun getMessages(): Flow<List<MorseMessage>> = _messages.asStateFlow()

    private val _peers = MutableStateFlow<Set<Pair<String, Int>>>(emptySet())
    val peers = _peers.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var isServerRunning = false
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_morsechat._tcp."
    private var serviceName: String = "MorseUser_${Random().nextInt(1000)}"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startServer() {
        if (isServerRunning) return
        isServerRunning = true
        
        scope.launch {
            try {
                serverSocket = ServerSocket(0)
                val port = serverSocket!!.localPort
                registerService(port)
                
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

    private fun handleClient(socket: Socket) {
        scope.launch {
            try {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val line = input.readLine()
                if (line != null) {
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
        
        _peers.value.forEach { (ip, port) ->
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

    override fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }

    override fun discoverPeers() {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType == serviceType && service.serviceName != serviceName) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            _peers.value = _peers.value + (serviceInfo.host.hostAddress!! to serviceInfo.port)
                        }
                    })
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                // Technically should remove here
            }
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun connectToPeer(ipAddress: String) {
        // Not needed for this auto-broadcast model
    }
}

