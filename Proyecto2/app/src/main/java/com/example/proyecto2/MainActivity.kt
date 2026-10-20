package com.example.proyecto2

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {

  private lateinit var tvRpm: TextView
  private lateinit var graficaView: GraficaView
  private lateinit var btnConectar: Button

  private var clienteWebSocket: WebSocketClient? = null
  private var estaConectado = false

  // Dirección fija del Access Point del ESP32 en el puerto 81
  private val servidorUri = URI("ws://192.168.4.1:81")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    tvRpm = findViewById(R.id.tvRpm)
    graficaView = findViewById(R.id.graficaView)
    btnConectar = findViewById(R.id.btnConectar)

    btnConectar.setOnClickListener {
      if (!estaConectado) {
        conectarAlServidorWiFi()
      } else {
        desconectarServidor()
      }
    }
  }

  private fun conectarAlServidorWiFi() {
    btnConectar.text = "Conectando..."
    btnConectar.isEnabled = false

    clienteWebSocket = object : WebSocketClient(servidorUri) {
      override fun onOpen(handshakedata: ServerHandshake?) {
        estaConectado = true
        runOnUiThread {
          btnConectar.isEnabled = true
          btnConectar.text = "Desconectar"
          btnConectar.setBackgroundColor(Color.parseColor("#FF5252"))
          Toast.makeText(this@MainActivity, "¡Conectado al Tacómetro por Wi-Fi!", Toast.LENGTH_SHORT).show()
        }
      }

      override fun onMessage(message: String?) {
        val rpmFloat = message?.trim()?.toFloatOrNull()
        if (rpmFloat != null) {
          runOnUiThread {
            tvRpm.text = rpmFloat.toInt().toString()
            graficaView.agregarDato(rpmFloat)
          }
        }
      }

      override fun onClose(code: Int, reason: String?, remote: Boolean) {
        runOnUiThread {
          gestionarDesconexion("Conexión Wi-Fi cerrada o perdida")
        }
      }

      override fun onError(ex: Exception?) {
        ex?.printStackTrace()
        runOnUiThread {
          gestionarDesconexion("Error de red en el canal de datos")
        }
      }
    }

    clienteWebSocket?.connect()
  }

  private fun desconectarServidor() {
    estaConectado = false
    try {
      clienteWebSocket?.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    gestionarDesconexion("Desconectado por el usuario")
  }

  private fun gestionarDesconexion(mensaje: String?) {
    estaConectado = false
    btnConectar.isEnabled = true
    btnConectar.text = "Conectar"
    btnConectar.setBackgroundColor(Color.parseColor("#00FFCC"))
    tvRpm.text = "0"
    if (mensaje != null) {
      Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    desconectarServidor()
  }
}