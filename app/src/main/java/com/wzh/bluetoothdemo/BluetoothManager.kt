package com.wzh.bluetoothdemo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * 蓝牙管理器 - 封装所有蓝牙相关操作
 * 这个类演示了Android蓝牙API的核心用法
 */
class BluetoothManager(private val context: Context) {
    
    // 蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    // 状态流
    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()
    
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow<String>("未连接")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    
    // 蓝牙Socket用于数据传输
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // SPP服务UUID (Serial Port Profile)
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // 广播接收器 - 监听蓝牙状态变化
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = state == BluetoothAdapter.STATE_ON
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { addDiscoveredDevice(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }
    
    init {
        // 注册广播接收器
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
        
        // 初始化状态
        updateBluetoothState()
        updatePairedDevices()
    }
    
    /**
     * 检查蓝牙是否可用
     */
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null
    
    /**
     * 更新蓝牙状态
     */
    private fun updateBluetoothState() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * 请求启用蓝牙
     */
    fun requestEnableBluetooth(): Intent? {
        return if (bluetoothAdapter?.isEnabled == false) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        } else null
    }
    
    /**
     * 检查权限
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取需要请求的权限
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
    
    /**
     * 开始扫描设备
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery(): Boolean {
        if (!hasBluetoothPermissions()) return false
        
        bluetoothAdapter?.let { adapter ->
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            _discoveredDevices.value = emptyList() // 清空之前的结果
            return adapter.startDiscovery()
        }
        return false
    }
    
    /**
     * 停止扫描设备
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery(): Boolean {
        if (!hasBluetoothPermissions()) return false
        return bluetoothAdapter?.cancelDiscovery() == true
    }
    
    /**
     * 添加发现的设备
     */
    @SuppressLint("MissingPermission")
    private fun addDiscoveredDevice(device: BluetoothDevice) {
        // 过滤掉没有名称的设备（减少未知设备显示）
        if (!hasBluetoothPermissions()) return
        
        val deviceName = try {
            device.name
        } catch (e: SecurityException) {
            null
        }
        
        // 只添加有名称的设备，过滤掉"未知设备"
        if (!deviceName.isNullOrBlank() && deviceName.trim().isNotEmpty()) {
            val currentDevices = _discoveredDevices.value.toMutableList()
            if (!currentDevices.any { it.address == device.address }) {
                currentDevices.add(device)
                _discoveredDevices.value = currentDevices
            }
        }
    }
    
    /**
     * 更新已配对设备列表
     */
    @SuppressLint("MissingPermission")
    fun updatePairedDevices() {
        if (!hasBluetoothPermissions()) return
        
        bluetoothAdapter?.bondedDevices?.let { devices ->
            _pairedDevices.value = devices.toList()
        }
    }
    
    /**
     * 连接到设备
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice): Boolean {
        if (!hasBluetoothPermissions()) return false
        
        try {
            // 停止扫描以提高连接成功率
            bluetoothAdapter?.cancelDiscovery()
            
            // 创建Socket
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            _connectionStatus.value = "正在连接..."
            
            // 连接
            bluetoothSocket?.connect()
            
            // 获取输入输出流
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream
            
            _connectionStatus.value = "已连接 - ${getDeviceName(device)}"
            return true
            
        } catch (e: IOException) {
            _connectionStatus.value = "连接失败: ${e.message}"
            disconnect()
            return false
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            // 忽略关闭时的异常
        }
        
        inputStream = null
        outputStream = null
        bluetoothSocket = null
        _connectionStatus.value = "未连接"
    }
    
    /**
     * 发送数据
     */
    fun sendData(data: String): Boolean {
        return try {
            outputStream?.write(data.toByteArray())
            true
        } catch (e: IOException) {
            _connectionStatus.value = "发送失败: ${e.message}"
            false
        }
    }
    
    /**
     * 接收数据 (简单实现，实际应用中应该在后台线程中持续监听)
     */
    fun receiveData(): String? {
        return try {
            inputStream?.let { stream ->
                val buffer = ByteArray(1024)
                val bytes = stream.read(buffer)
                String(buffer, 0, bytes)
            }
        } catch (e: IOException) {
            _connectionStatus.value = "接收失败: ${e.message}"
            null
        }
    }
    
    /**
     * 获取设备名称（处理权限问题）
     */
    @SuppressLint("MissingPermission")
    fun getDeviceName(device: BluetoothDevice): String {
        return if (hasBluetoothPermissions()) {
            device.name ?: "未知设备"
        } else {
            "未知设备 (需要权限)"
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            // 忽略取消注册时的异常
        }
        disconnect()
        stopDiscovery()
    }
}