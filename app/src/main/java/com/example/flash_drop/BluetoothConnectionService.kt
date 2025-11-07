package com.example.flash_drop

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothConnectionService(
    private val handler: Handler
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connectedThread: ConnectedThread? = null
    private var serverThread: ServerThread? = null

    companion object {
        private const val TAG = "BluetoothConnectionService"
        val MY_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66") // UUID aleatório, mas deve ser o mesmo no cliente e servidor
        val NAME = "FlashDrop"
    }

    @SuppressLint("MissingPermission")
    inner class ServerThread : Thread() {
        private val serverSocket: BluetoothServerSocket? by lazy {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Falha ao aceitar conexão do servidor", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    connectedThread = ConnectedThread(it).also { thread ->
                        thread.start()
                    }
                    shouldLoop = false
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Falha ao fechar o server socket", e)
            }
        }
    }

    inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream? by lazy { socket.inputStream }
        private val outputStream: OutputStream? by lazy { socket.outputStream }

        override fun run() {
            // Aqui podemos ler dados que chegam (para receber arquivos)
            // Por enquanto, não estamos lendo, apenas enviando.
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream?.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Falha ao escrever no output stream", e)
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Falha ao fechar o socket", e)
            }
        }
    }

    fun startServer() {
        serverThread = ServerThread().also { it.start() }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        try {
            val socket = device?.createRfcommSocketToServiceRecord(MY_UUID)
            socket?.let {
                connectedThread = ConnectedThread(it).also { thread ->
                    thread.start()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Falha ao conectar ao dispositivo", e)
        }
    }

    fun sendFile(bytes: ByteArray) {
        connectedThread?.write(bytes)
    }

    fun stop() {
        serverThread?.cancel()
        connectedThread?.cancel()
    }
}