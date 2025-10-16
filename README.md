# 蓝牙学习助手 (Bluetooth Learning Assistant)

一个用于学习Android蓝牙开发的完整示例应用，涵盖了蓝牙开发的核心概念和实际应用。

## 🎯 应用功能

### 核心功能
- **蓝牙状态管理** - 检查蓝牙支持、开启/关闭状态
- **权限管理** - 处理Android 12+的新权限模型
- **设备扫描** - 扫描附近的蓝牙设备
- **设备管理** - 查看已配对设备列表
- **设备连接** - 连接到蓝牙设备
- **数据传输** - 发送和接收数据
- **蓝牙知识** - 内置学习资料

### 学习特性
- **详细注释** - 代码中包含详细的中文注释
- **知识卡片** - 内置蓝牙协议和概念说明
- **实用工具** - 蓝牙工具类和常用UUID
- **错误处理** - 完整的错误处理和用户提示

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + State Flow
- **蓝牙API**: Android Bluetooth API
- **最低SDK**: API 29 (Android 10)
- **目标SDK**: API 35

## 📱 界面预览

### 主界面
- 蓝牙状态显示
- 设备扫描控制
- 设备列表展示
- 蓝牙知识学习

### 数据传输界面
- 实时通信记录
- 快速命令发送
- 自定义数据输入
- 连接状态监控

## 🔧 核心类说明

### BluetoothManager.kt
封装所有蓝牙操作的管理类：
- 蓝牙适配器管理
- 设备扫描和发现
- 设备连接和数据传输
- 状态监听和更新

### MainActivity.kt
主Activity，处理：
- 权限请求
- 界面导航
- 用户交互
- 生命周期管理

### BluetoothScreen.kt
主界面组件：
- 设备列表显示
- 扫描控制
- 蓝牙知识展示

### DataTransferScreen.kt
数据传输界面：
- 消息历史记录
- 数据发送接收
- 快速命令

## 🚀 开始使用

### 1. 克隆项目
```bash
git clone <your-repo-url>
cd BlueToothDemo
```

### 2. 导入Android Studio
- 打开Android Studio
- 选择 "Open an Existing Project"
- 选择项目文件夹

### 3. 运行应用
- 连接Android设备或启动模拟器
- 点击 "Run" 按钮

### 4. 权限授予
首次运行时需要授予以下权限：
- 蓝牙扫描权限
- 蓝牙连接权限
- 位置权限（用于蓝牙扫描）

## 📚 学习要点

### 1. 蓝牙权限管理
```kotlin
// Android 12+ 新权限
BLUETOOTH_SCAN
BLUETOOTH_CONNECT
BLUETOOTH_ADVERTISE

// 传统权限
BLUETOOTH
BLUETOOTH_ADMIN
ACCESS_FINE_LOCATION
```

### 2. 蓝牙开发流程
```
检查蓝牙支持 → 请求权限 → 启用蓝牙 → 扫描设备 → 配对连接 → 数据传输
```

### 3. 核心API使用
- `BluetoothAdapter` - 蓝牙适配器
- `BluetoothDevice` - 蓝牙设备
- `BluetoothSocket` - 数据传输通道
- `BroadcastReceiver` - 状态监听

### 4. 常用UUID
- SPP: `00001101-0000-1000-8000-00805F9B34FB`
- A2DP: `0000110D-0000-1000-8000-00805F9B34FB`
- HID: `00001124-0000-1000-8000-00805F9B34FB`

## 🔍 代码结构

```
app/src/main/java/com/wzh/bluetoothdemo/
├── BluetoothManager.kt          # 蓝牙管理核心类
├── MainActivity.kt              # 主Activity
├── ui/
│   ├── screens/
│   │   ├── BluetoothScreen.kt   # 主界面
│   │   └── DataTransferScreen.kt # 数据传输界面
│   ├── components/
│   │   └── DeviceDetailDialog.kt # 设备详情对话框
│   └── theme/                   # UI主题
├── utils/
│   └── BluetoothUtils.kt        # 蓝牙工具类
```

## 🐛 常见问题

### Q: 扫描不到设备？
A: 检查以下几点：
1. 是否授予了位置权限
2. 目标设备是否处于可发现状态
3. 是否在Android 12+设备上授予了新的蓝牙权限

### Q: 连接失败？
A: 可能原因：
1. 设备未配对
2. 目标设备不支持SPP协议
3. 距离过远或信号干扰

### Q: 数据传输失败？
A: 检查：
1. 连接是否正常
2. 数据格式是否正确
3. 目标设备是否正确处理数据

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个学习项目。

## 📄 许可证

本项目仅用于学习目的，请遵循相关开源协议。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- Issue: 在GitHub上创建Issue
- Email: your-email@example.com

---

**注意**: 这是一个学习项目，主要用于理解Android蓝牙开发的基本概念和实现方法。在生产环境中使用时，请根据具体需求进行相应的优化和安全加固。