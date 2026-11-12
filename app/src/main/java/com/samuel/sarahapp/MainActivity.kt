package com.samuel.sarahapp // Asegúrate de que este paquete sea el tuyo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // TODO: PEGA AQUÍ TU TOKEN DE BLYNK (El mismo que pusiste en la ESP32)
    private val BLYNK_TOKEN = "F8nSKXPKkdLpRrAhN2WYEKpNd1Jqejyr"

    private val client = OkHttpClient()

    // Variables de la interfaz
    private lateinit var tvSoilMoisture: TextView
    private lateinit var tvWaterLevel: TextView
    private lateinit var tvSunlight: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val switchAutoToldo = findViewById<SwitchMaterial>(R.id.switchAutoToldo)
        val layoutManualToldoControls = findViewById<LinearLayout>(R.id.layoutManualToldoControls)
        val btnOpenToldo = findViewById<Button>(R.id.btnOpenToldo)
        val btnCloseToldo = findViewById<Button>(R.id.btnCloseToldo)

        tvSoilMoisture = findViewById(R.id.tvSoilMoisture)
        tvWaterLevel = findViewById(R.id.tvWaterLevel)
        tvSunlight = findViewById(R.id.tvSunlight)

        // Estado inicial de la interfaz
        if (switchAutoToldo.isChecked) layoutManualToldoControls.visibility = View.GONE

        // 1. Enviar el cambio de modo Automático/Manual (Pin V3)
        switchAutoToldo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutManualToldoControls.visibility = View.GONE
                enviarComandoBlynk("v3", "1") // 1 = Automático
                Toast.makeText(this, "Modo Automático Activado", Toast.LENGTH_SHORT).show()
            } else {
                layoutManualToldoControls.visibility = View.VISIBLE
                enviarComandoBlynk("v3", "0") // 0 = Manual
                Toast.makeText(this, "Modo Manual Activado", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Enviar comandos manuales al Motor (Pin V4)
        btnOpenToldo.setOnClickListener {
            enviarComandoBlynk("v4", "1") // 1 = Extender
            Toast.makeText(this, "Extendiendo Toldo...", Toast.LENGTH_SHORT).show()
        }

        btnCloseToldo.setOnClickListener {
            enviarComandoBlynk("v4", "-1") // -1 = Retraer
            Toast.makeText(this, "Retrayendo Toldo...", Toast.LENGTH_SHORT).show()
        }

        // 3. Iniciar el monitoreo constante de los sensores
        iniciarLecturaDeSensores()
    }

    // --- FUNCIONES DE COMUNICACIÓN CON BLYNK ---

    // Función para enviar órdenes (Switch, Botones, Válvulas) en segundo plano
    private fun enviarComandoBlynk(pin: String, valor: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Estructura de la API REST de Blynk para Escribir datos
            val url = "https://blynk.cloud/external/api/update?token=$BLYNK_TOKEN&$pin=$valor"
            val request = Request.Builder().url(url).build()
            try {
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para leer los sensores cada 2 segundos sin congelar la app
    private fun iniciarLecturaDeSensores() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) { // Mientras la pantalla esté abierta
                // Petición a la API REST para Leer múltiples pines a la vez
                val url = "https://blynk.cloud/external/api/get?token=$BLYNK_TOKEN&v0&v1&v2"
                val request = Request.Builder().url(url).build()
                try {
                    val response = client.newCall(request).execute()
                    val jsonResponse = response.body?.string()

                    // Si la nube responde correctamente, extraemos los datos
                    if (jsonResponse != null && jsonResponse.startsWith("{")) {
                        val jsonObject = JSONObject(jsonResponse)
                        val humedad = jsonObject.optString("v0", "--")
                        val agua = jsonObject.optString("v1", "--")
                        val luz = jsonObject.optString("v2", "--")

                        // Actualizamos la interfaz gráfica (esto debe hacerse en el hilo principal)
                        withContext(Dispatchers.Main) {
                            tvSoilMoisture.text = "Humedad del suelo: $humedad"
                            tvWaterLevel.text = "Nivel de Tanque: $agua"
                            tvSunlight.text = "Radiación / Luz: $luz %"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000) // Espera de 2 segundos antes de la siguiente lectura
            }
        }
    }
}