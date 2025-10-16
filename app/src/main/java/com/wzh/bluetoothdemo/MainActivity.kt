package com.wzh.bluetoothdemo

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wzh.bluetoothdemo.ui.screens.BluetoothScreen
import com.wzh.bluetoothdemo.ui.screens.DataTransferScreen
import com.wzh.bluetoothdemo.ui.theme.BlueToothDemoTheme

/**
 * 主Activity - 蓝牙设备管理应用
 * 
 * 这个应用演示了Android蓝牙开发的核心功能：
 * 1. 蓝牙权限管理
 * 2. 蓝牙状态检测和控制
 * 3. 设备扫描和发现
 * 4. 设备连接和数据传输
 * 5. 蓝牙知识学习
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var bluetoothManager: BluetoothManager
    
    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要蓝牙权限才能正常使用", Toast.LENGTH_LONG).show()
        }
    }
    
    // 蓝牙启用请求启动器
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要开启蓝牙才能使用相关功能", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化蓝牙管理器
        bluetoothManager = BluetoothManager(this)
        
        // 检查蓝牙支持
        if (!bluetoothManager.isBluetoothSupported()) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setContent {
            BlueToothDemoTheme {
                BluetoothApp()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BluetoothApp() {
        var currentScreen by remember { mutableStateOf("main") }
        var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
        
        // 收集状态
        val isBluetoothEnabled by bluetoothManager.isBluetoothEnabled.collectAsStateWithLifecycle()
        val isScanning by bluetoothManager.isScanning.collectAsStateWithLifecycle()
        val discoveredDevices by bluetoothManager.discoveredDevices.collectAsStateWithLifecycle()
        val pairedDevices by bluetoothManager.pairedDevices.collectAsStateWithLifecycle()
        val connectionStatus by bluetoothManager.connectionStatus.collectAsStateWithLifecycle()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = when (currentScreen) {
                                "main" -> "蓝牙Demo"
                                "transfer" -> "数据传输"
                                else -> "蓝牙Demo"
                            }
                        ) 
                    },
                    navigationIcon = {
                        if (currentScreen != "main") {
                            IconButton(onClick = { currentScreen = "main" }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        // 权限检查按钮
                        IconButton(
                            onClick = { checkAndRequestPermissions() }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "检查权限",
                                tint = if (bluetoothManager.hasBluetoothPermissions()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (currentScreen == "main") {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Check, contentDescription = null) },
                            label = { Text("蓝牙管理") },
                            selected = true,
                            onClick = { }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Send, contentDescription = null) },
                            label = { Text("数据传输") },
                            selected = false,
                            onClick = { 
                                if (connectionStatus.contains("已连接") || connectionStatus.contains("连接成功")) {
                                    currentScreen = "transfer"
                                } else {
                                    Toast.makeText(this@MainActivity, "请先连接设备", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = connectionStatus.contains("已连接") || connectionStatus.contains("连接成功")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "main" -> BluetoothScreen(
                        isBluetoothEnabled = isBluetoothEnabled,
                        isScanning = isScanning,
                        discoveredDevices = discoveredDevices,
                        pairedDevices = pairedDevices,
                        connectionStatus = connectionStatus,
                        onEnableBluetoothClick = { requestEnableBluetooth() },
                        onStartScanClick = { startDeviceScan() },
                        onStopScanClick = { stopDeviceScan() },
                        onDeviceClick = { device ->
                            selectedDevice = device
                            connectToDevice(device)
                        },
                        onRefreshPairedClick = { bluetoothManager.updatePairedDevices() },
                        getDeviceName = { device -> bluetoothManager.getDeviceName(device) },
                        hasBluetoothPermissions = bluetoothManager.hasBluetoothPermissions()
                    )
                    
                    "transfer" -> DataTransferScreen(
                        connectionStatus = connectionStatus,
                        onSendData = { data -> bluetoothManager.sendData(data) },
                        onReceiveData = { bluetoothManager.receiveData() },
                        onDisconnect = { 
                            bluetoothManager.disconnect()
                            currentScreen = "main"
                        }
                    )
                }
            }
        }
    }
    
    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            requestPermissionLauncher.launch(bluetoothManager.getRequiredPermissions())
        } else {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 请求启用蓝牙
     */
    private fun requestEnableBluetooth() {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            Toast.makeText(this, "请先授予蓝牙权限", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }
        
        bluetoothManager.requestEnableBluetooth()?.let { intent ->
            enableBluetoothLauncher.launch(intent)
        }
    }
    
    /**
     * 开始设备扫描
     */
    private fun startDeviceScan() {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            Toast.makeText(this, "请先授予蓝牙权限", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }
        
        if (!bluetoothManager.isBluetoothEnabled.value) {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_SHORT).show()
            requestEnableBluetooth()
            return
        }
        
        val success = bluetoothManager.startDiscovery()
        if (!success) {
            Toast.makeText(this, "启动扫描失败", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "开始扫描设备...", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 停止设备扫描
     */
    private fun stopDeviceScan() {
        bluetoothManager.stopDiscovery()
        Toast.makeText(this, "已停止扫描", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 连接到设备
     */
    private fun connectToDevice(device: BluetoothDevice) {
        if (!bluetoothManager.hasBluetoothPermissions()) {
            Toast.makeText(this, "请先授予蓝牙权限", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "正在连接到 ${bluetoothManager.getDeviceName(device)}...", Toast.LENGTH_SHORT).show()
        
        // 在后台线程中执行连接操作
        Thread {
            val success = bluetoothManager.connectToDevice(device)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "连接成功！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }
}