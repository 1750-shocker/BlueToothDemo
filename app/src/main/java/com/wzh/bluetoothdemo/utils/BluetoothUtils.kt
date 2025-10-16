package com.wzh.bluetoothdemo.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build

/**
 * 蓝牙工具类
 * 提供一些实用的蓝牙相关工具方法
 */
object BluetoothUtils {
    
    /**
     * 获取蓝牙设备类型描述
     */
    @SuppressLint("MissingPermission")
    fun getDeviceTypeDescription(device: BluetoothDevice): String {
        return try {
            when (device.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "经典蓝牙"
                BluetoothDevice.DEVICE_TYPE_LE -> "低功耗蓝牙(BLE)"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "双模蓝牙"
                else -> "未知类型"
            }
        } catch (e: SecurityException) {
            "权限不足"
        }
    }
    
    /**
     * 获取设备绑定状态描述
     */
    fun getBondStateDescription(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_NONE -> "未配对"
            BluetoothDevice.BOND_BONDING -> "配对中"
            BluetoothDevice.BOND_BONDED -> "已配对"
            else -> "未知状态"
        }
    }
    
    /**
     * 获取设备类别信息
     */ 
    @SuppressLint("MissingPermission")
    fun getDeviceClassDescription(device: BluetoothDevice): String {
        return try {
            val deviceClass = device.bluetoothClass ?: return "未知设备类别"
            
            when (deviceClass.majorDeviceClass) {
                android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO -> "音频/视频设备"
                android.bluetooth.BluetoothClass.Device.Major.COMPUTER -> "计算机"
                android.bluetooth.BluetoothClass.Device.Major.HEALTH -> "健康设备"
                android.bluetooth.BluetoothClass.Device.Major.IMAGING -> "图像设备"
                android.bluetooth.BluetoothClass.Device.Major.MISC -> "其他设备"
                android.bluetooth.BluetoothClass.Device.Major.NETWORKING -> "网络设备"
                android.bluetooth.BluetoothClass.Device.Major.PERIPHERAL -> "外围设备"
                android.bluetooth.BluetoothClass.Device.Major.PHONE -> "电话"
                android.bluetooth.BluetoothClass.Device.Major.TOY -> "玩具"
                android.bluetooth.BluetoothClass.Device.Major.UNCATEGORIZED -> "未分类"
                android.bluetooth.BluetoothClass.Device.Major.WEARABLE -> "可穿戴设备"
                else -> "未知设备类别"
            }
        } catch (e: SecurityException) {
            "权限不足"
        }
    }
    
    /**
     * 检查设备是否支持指定的UUID
     */
    @SuppressLint("MissingPermission")
    fun deviceSupportsUuid(device: BluetoothDevice, uuid: String): Boolean {
        return try {
            val deviceUuids = device.uuids
            deviceUuids?.any { it.toString().equals(uuid, ignoreCase = true) } == true
        } catch (e: SecurityException) {
            false
        }
    }
    
    /**
     * 格式化MAC地址
     */
    fun formatMacAddress(address: String): String {
        return address.uppercase().chunked(2).joinToString(":")
    }
    
    /**
     * 验证MAC地址格式
     */
    fun isValidMacAddress(address: String): Boolean {
        val macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
        return address.matches(macPattern.toRegex())
    }
    
    /**
     * 获取信号强度描述
     */
    fun getSignalStrengthDescription(rssi: Int): String {
        return when {
            rssi >= -30 -> "极强"
            rssi >= -50 -> "很强"
            rssi >= -60 -> "强"
            rssi >= -70 -> "中等"
            rssi >= -80 -> "弱"
            else -> "极弱"
        }
    }
    
    /**
     * 计算距离估算（基于RSSI，仅供参考）
     */
    fun estimateDistance(rssi: Int, txPower: Int = -59): Double {
        if (rssi == 0) return -1.0
        
        val ratio = txPower * 1.0 / rssi
        return if (ratio < 1.0) {
            Math.pow(ratio, 10.0)
        } else {
            val accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111
            accuracy
        }
    }
    
    /**
     * 获取Android版本对应的蓝牙功能描述
     */
    fun getBluetoothFeaturesForAndroidVersion(): List<String> {
        val features = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            features.add("蓝牙4.0 LE支持")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            features.add("蓝牙LE广播")
            features.add("蓝牙LE扫描过滤")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            features.add("蓝牙LE扫描权限管理")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            features.add("蓝牙5.0支持")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            features.add("新的蓝牙权限模型")
            features.add("蓝牙LE音频")
        }
        
        return features
    }
    
    /**
     * 常用的蓝牙服务UUID
     */
    object CommonUUIDs {
        const val SPP = "00001101-0000-1000-8000-00805F9B34FB"              // 串口服务
        const val A2DP = "0000110D-0000-1000-8000-00805F9B34FB"              // 高级音频分发
        const val HID = "00001124-0000-1000-8000-00805F9B34FB"               // 人机接口设备
        const val HSP = "00001108-0000-1000-8000-00805F9B34FB"               // 耳机
        const val HFP = "0000111E-0000-1000-8000-00805F9B34FB"               // 免提
        const val OBEX = "00001105-0000-1000-8000-00805F9B34FB"              // 对象交换
        const val HEART_RATE = "0000180D-0000-1000-8000-00805F9B34FB"        // 心率服务
        const val BATTERY = "0000180F-0000-1000-8000-00805F9B34FB"           // 电池服务
        const val DEVICE_INFO = "0000180A-0000-1000-8000-00805F9B34FB"       // 设备信息
    }
    
    /**
     * 获取UUID对应的服务名称
     */
    fun getServiceNameByUuid(uuid: String): String {
        return when (uuid.uppercase()) {
            CommonUUIDs.SPP -> "串口服务 (SPP)"
            CommonUUIDs.A2DP -> "高级音频分发 (A2DP)"
            CommonUUIDs.HID -> "人机接口设备 (HID)"
            CommonUUIDs.HSP -> "耳机 (HSP)"
            CommonUUIDs.HFP -> "免提 (HFP)"
            CommonUUIDs.OBEX -> "对象交换 (OBEX)"
            CommonUUIDs.HEART_RATE -> "心率服务"
            CommonUUIDs.BATTERY -> "电池服务"
            CommonUUIDs.DEVICE_INFO -> "设备信息服务"
            else -> "未知服务"
        }
    }
}