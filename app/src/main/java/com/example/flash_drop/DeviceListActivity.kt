package com.example.flash_drop

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class DeviceListActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var listView: ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var txtStatus: TextView
    private val deviceList = mutableListOf<String>()
    private val bluetoothDevices = mutableMapOf<String, BluetoothDevice>() // Mapa para armazenar os dispositivos

    // Receiver para dispositivos encontrados
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                device?.let {
                    // Filtra apenas dispositivos móveis
                    if (it.bluetoothClass.deviceClass == BluetoothClass.Device.PHONE_SMART ||
                        it.bluetoothClass.deviceClass == BluetoothClass.Device.PHONE_CELLULAR ||
                        it.bluetoothClass.deviceClass == BluetoothClass.Device.PHONE_UNCATEGORIZED
                    ) {
                        val name = it.name ?: "Dispositivo Desconhecido"
                        val address = it.address
                        val display = "$name\n$address"

                        if (!deviceList.contains(display)) {
                            deviceList.add(display)
                            bluetoothDevices[address] = it // Armazena o dispositivo
                            arrayAdapter.notifyDataSetChanged()
                            txtStatus.text = "${deviceList.size} dispositivos encontrados"
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                txtStatus.text = "Busca finalizada - ${deviceList.size} dispositivos"
            }
        }
    }

    // Receiver para estado do pareamento
    private val bondReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                val bondState = intent?.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR) ?: BluetoothDevice.ERROR

                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        device?.let {
                            Toast.makeText(this@DeviceListActivity, "Pareado com ${it.name ?: it.address}", Toast.LENGTH_SHORT).show()
                            txtStatus.text = "Pareado com ${it.name ?: it.address}"

                            // Aguardar um momento antes de ir para a próxima tela
                            Handler(mainLooper).postDelayed({
                                goToAnexoActivity(it)
                            }, 1000)
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        txtStatus.text = "Pareando..."
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Toast.makeText(this@DeviceListActivity, "Pareamento falhou", Toast.LENGTH_SHORT).show()
                        txtStatus.text = "Pareamento falhou"
                    }
                }
            }
        }
    }

    // Handler para operações na thread principal
    private lateinit var handler: android.os.Handler

    // Launcher para ativar Bluetooth
    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startDiscovery()
            } else {
                Toast.makeText(this, "Bluetooth necessário para continuar", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device_list)

        handler = android.os.Handler(mainLooper)

        // Configurar botão voltar
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Inicializar componentes
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        listView = findViewById(R.id.listaDevices)
        txtStatus = findViewById(R.id.txtStatus)
        val btnScan = findViewById<Button>(R.id.btnScan)

        // Verificar suporte ao Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar adapter da lista
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = arrayAdapter

        // Botão scan
        btnScan.setOnClickListener {
            checkPermissionAndScan()
        }

        // Clique na lista
        listView.setOnItemClickListener { _, _, position, _ ->
            val deviceInfo = deviceList[position]
            val address = deviceInfo.split("\n")[1]
            val device = bluetoothDevices[address]

            device?.let {
                checkDevicePairingStatus(it)
            }
        }

        // Registrar receivers
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)

        val bondFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondReceiver, bondFilter)

        // Iniciar busca automática se já tiver permissões
        if (bluetoothAdapter.isEnabled) {
            handler.postDelayed({
                checkPermissionAndScan()
            }, 500)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDevicePairingStatus(device: BluetoothDevice) {
        // Verificar se tem permissão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão necessária para verificar pareamento", Toast.LENGTH_SHORT).show()
                return
            }
        }

        when (device.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                // Já está pareado, pode ir para a próxima tela
                goToAnexoActivity(device)
            }
            BluetoothDevice.BOND_BONDING -> {
                Toast.makeText(this, "Pareamento em andamento...", Toast.LENGTH_SHORT).show()
                txtStatus.text = "Pareamento em andamento..."
            }
            BluetoothDevice.BOND_NONE -> {
                // Não está pareado, iniciar processo de pareamento
                Toast.makeText(this, "Iniciando pareamento...", Toast.LENGTH_SHORT).show()
                txtStatus.text = "Iniciando pareamento..."

                // Tentar parear
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Permissão necessária para parear", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }

                    // Método para parear
                    val method = device.javaClass.getMethod("createBond")
                    method.invoke(device)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Erro ao iniciar pareamento: ${e.message}", Toast.LENGTH_SHORT).show()
                    txtStatus.text = "Erro ao parear"

                    // Mostrar instruções para parear manualmente
                    showPairingInstructions(device)
                }
            }
        }
    }

    private fun showPairingInstructions(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Pareamento necessário")
            .setMessage("Para continuar, é necessário parear com o dispositivo ${device.name ?: device.address}.\n\n" +
                    "Por favor, vá nas configurações de Bluetooth do seu dispositivo e aceite a solicitação de pareamento.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun goToAnexoActivity(device: BluetoothDevice) {
        val intent = Intent(this, AnexoActivity::class.java)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        intent.putExtra("deviceName", device.name ?: "Dispositivo")
        intent.putExtra("deviceAddress", device.address)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun checkPermissionAndScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            if (bluetoothAdapter.isEnabled) {
                startDiscovery()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        // Cancelar descoberta anterior se estiver acontecendo
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        deviceList.clear()
        bluetoothDevices.clear()
        arrayAdapter.notifyDataSetChanged()
        txtStatus.text = "Buscando dispositivos..."

        val started = bluetoothAdapter.startDiscovery()
        if (!started) {
            txtStatus.text = "Falha ao iniciar busca"
            Toast.makeText(this, "Não foi possível iniciar a busca por dispositivos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            checkPermissionAndScan()
        } else {
            Toast.makeText(this, "Permissões necessárias para buscar dispositivos", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
            unregisterReceiver(bondReceiver)

            // Verificar permissão antes de cancelar descoberta
            if (bluetoothAdapter.isDiscovering) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                } else {
                    bluetoothAdapter.cancelDiscovery()
                }
            }
        } catch (e: Exception) {
            // Já estava desregistrado
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}