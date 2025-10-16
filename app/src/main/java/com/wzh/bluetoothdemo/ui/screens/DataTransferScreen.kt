package com.wzh.bluetoothdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 数据传输界面
 * 用于发送和接收蓝牙数据，学习蓝牙通信协议
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTransferScreen(
    connectionStatus: String,
    onSendData: (String) -> Boolean,
    onReceiveData: () -> String?,
    onDisconnect: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var messageHistory by remember { mutableStateOf(listOf<ChatMessage>()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 预设的测试命令
    val testCommands = listOf(
        "AT" to "基本AT命令测试",
        "AT+VERSION" to "查询版本信息",
        "Hello World" to "发送问候消息",
        "LED_ON" to "LED开启命令",
        "LED_OFF" to "LED关闭命令",
        "SENSOR_READ" to "读取传感器数据"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 连接状态卡片
        ConnectionStatusCard(
            connectionStatus = connectionStatus,
            onDisconnect = onDisconnect
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 消息历史
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "通信记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        // 接收数据按钮
                        TextButton(
                            onClick = {
                                val receivedData = onReceiveData()
                                if (!receivedData.isNullOrEmpty()) {
                                    messageHistory = messageHistory + ChatMessage(
                                        content = receivedData,
                                        isOutgoing = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    scope.launch {
                                        listState.animateScrollToItem(messageHistory.size - 1)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("接收")
                        }
                        
                        // 清空记录按钮
                        TextButton(
                            onClick = { messageHistory = emptyList() }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(messageHistory) { message ->
                        MessageItem(message = message)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 快速命令区域
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "快速命令",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(120.dp)
                ) {
                    items(testCommands) { (command, description) ->
                        OutlinedCard(
                            onClick = {
                                val success = onSendData(command)
                                messageHistory = messageHistory + ChatMessage(
                                    content = command,
                                    isOutgoing = true,
                                    timestamp = System.currentTimeMillis(),
                                    success = success
                                )
                                scope.launch {
                                    listState.animateScrollToItem(messageHistory.size - 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = command,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 自定义输入区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("输入要发送的数据") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        val success = onSendData(inputText)
                        messageHistory = messageHistory + ChatMessage(
                            content = inputText,
                            isOutgoing = true,
                            timestamp = System.currentTimeMillis(),
                            success = success
                        )
                        inputText = ""
                        scope.launch {
                            listState.animateScrollToItem(messageHistory.size - 1)
                        }
                    }
                },
                enabled = inputText.isNotEmpty(),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    connectionStatus: String,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (connectionStatus.contains("已连接")) 
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
                    text = "连接状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = connectionStatus,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (connectionStatus.contains("已连接")) {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("断开连接")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val backgroundColor = if (message.isOutgoing) {
        if (message.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.secondary
    }
    
    val textColor = if (message.isOutgoing) {
        if (message.success) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onSecondary
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (message.isOutgoing) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (message.isOutgoing) "发送" else "接收",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                    if (message.isOutgoing && !message.success) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "发送失败",
                            tint = textColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontFamily = FontFamily.Monospace
                )
                
                Text(
                    text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class ChatMessage(
    val content: String,
    val isOutgoing: Boolean,
    val timestamp: Long,
    val success: Boolean = true
)