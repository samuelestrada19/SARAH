package com.samuel.sarahapp
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Enlazar la interfaz con el código
        val switchAutoToldo = findViewById<SwitchMaterial>(R.id.switchAutoToldo)
        val layoutManualToldoControls = findViewById<LinearLayout>(R.id.layoutManualToldoControls)
        val btnOpenToldo = findViewById<Button>(R.id.btnOpenToldo)
        val btnCloseToldo = findViewById<Button>(R.id.btnCloseToldo)
        val btnManagePlants = findViewById<Button>(R.id.btnManagePlants)

        // Estado inicial: Si está automático, ocultamos los botones manuales
        if (switchAutoToldo.isChecked) {
            layoutManualToldoControls.visibility = View.GONE
        }

        // 2. Lógica del interruptor (Switch) del Toldo
        switchAutoToldo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Modo automático: ocultar botones
                layoutManualToldoControls.visibility = View.GONE
                Toast.makeText(this, "Toldo en modo Automático", Toast.LENGTH_SHORT).show()
            } else {
                // Modo manual: mostrar botones
                layoutManualToldoControls.visibility = View.VISIBLE
                Toast.makeText(this, "Toldo en modo Manual", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Lógica de los botones manuales del toldo
        btnOpenToldo.setOnClickListener {
            // Aquí luego enviaremos la señal a la ESP32
            Toast.makeText(this, "Enviando orden: Desplegar Toldo...", Toast.LENGTH_SHORT).show()
        }

        btnCloseToldo.setOnClickListener {
            // Aquí luego enviaremos la señal a la ESP32
            Toast.makeText(this, "Enviando orden: Retraer Toldo...", Toast.LENGTH_SHORT).show()
        }

        // 4. Lógica para ir a la pantalla de plantas
        btnManagePlants.setOnClickListener {
            Toast.makeText(this, "Próximamente: Pantalla de Plantas", Toast.LENGTH_SHORT).show()
            // En el siguiente paso abriremos la nueva Activity aquí
        }
    }
}