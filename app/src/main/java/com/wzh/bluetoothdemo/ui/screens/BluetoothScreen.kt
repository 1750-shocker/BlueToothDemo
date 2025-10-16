package com.wzh.bluetoothdemo.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wzh.bluetoothdemo.ui.components.DeviceDetailDialog

/**
 * 蓝牙主界面
 * 展示蓝牙状态、设备列表和控制按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    isBluetoothEnabled: Boolean,
    isScanning: Boolean,
    discoveredDevices: List<BluetoothDevice>,
    pairedDevices: List<BluetoothDevice>,
    connectionStatus: String,
    onEnableBluetoothClick: () -> Unit,
    onStartScanClick: () -> Unit,
    onStopScanClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onRefreshPairedClick: () -> Unit,
    getDeviceName: (BluetoothDevice) -> String,
    hasBluetoothPermissions: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val tabs = listOf("扫描设备", "已配对设备")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 蓝牙状态卡片
        BluetoothStatusCard(
            isBluetoothEnabled = isBluetoothEnabled,
            connectionStatus = connectionStatus,
            onEnableBluetoothClick = onEnableBluetoothClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab选择器
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab内容
        when (selectedTab) {
            0 -> DeviceScanTab(
                isScanning = isScanning,
                discoveredDevices = discoveredDevices,
                onStartScanClick = onStartScanClick,
                onStopScanClick = onStopScanClick,
                onDeviceClick = { device -> selectedDevice = device },
                getDeviceName = getDeviceName
            )
            1 -> PairedDevicesTab(
                pairedDevices = pairedDevices,
                onDeviceClick = { device -> selectedDevice = device },
                onRefreshClick = onRefreshPairedClick,
                getDeviceName = getDeviceName
            )

        }
        
        // 设备详情对话框
        selectedDevice?.let { device ->
            DeviceDetailDialog(
                device = device,
                onDismiss = { selectedDevice = null },
                onConnect = { 
                    onDeviceClick(device)
                    selectedDevice = null
                },
                hasPermissions = hasBluetoothPermissions
            )
        }
    }
}

@Composable
fun BluetoothStatusCard(
    isBluetoothEnabled: Boolean,
    connectionStatus: String,
    onEnableBluetoothClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBluetoothEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "蓝牙状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBluetoothEnabled) "已开启" else "未开启",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = connectionStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isBluetoothEnabled) {
                Button(
                    onClick = onEnableBluetoothClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开启蓝牙")
                }
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceScanTab(
    isScanning: Boolean,
    discoveredDevices: List<BluetoothDevice>,
    onStartScanClick: () -> Unit,
    onStopScanClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit,
    getDeviceName: (BluetoothDevice) -> String
) {
    Column {
        // 扫描控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onStartScanClick,
                enabled = !isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始扫描")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = onStopScanClick,
                enabled = isScanning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止扫描")
            }
        }
        
        if (isScanning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("正在扫描设备...")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 设备列表
        Text(
            text = "发现的设备 (${discoveredDevices.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn {
            items(discoveredDevices) { device ->
                DeviceItem(
                    device = device,
                    deviceName = getDeviceName(device),
                    onClick = { onDeviceClick(device) },
                    isPaired = false
                )
            }
        }
    }
}

@Composable
fun PairedDevicesTab(
    pairedDevices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onRefreshClick: () -> Unit,
    getDeviceName: (BluetoothDevice) -> String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已配对设备 (${pairedDevices.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onRefreshClick) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn {
            items(pairedDevices) { device ->
                DeviceItem(
                    device = device,
                    deviceName = getDeviceName(device),
                    onClick = { onDeviceClick(device) },
                    isPaired = true
                )
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: BluetoothDevice,
    deviceName: String,
    onClick: () -> Unit,
    isPaired: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPaired) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "已配对",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

