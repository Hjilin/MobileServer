package com.mobileserver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileserver.core.Paths
import com.mobileserver.engine.EngineController
import com.mobileserver.engine.EngineController.ServiceState
import com.mobileserver.engine.EngineController.ServiceType
import com.mobileserver.util.PortChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 服务状态管理：驱动MIUI风格UI的服务卡片、启停按钮、端口状态
 */
class ServerViewModel : ViewModel() {

    private val _services = MutableStateFlow<List<ServiceState>>(emptyList())
    val services: StateFlow<List<ServiceState>> = _services.asStateFlow()

    private val _isAllRunning = MutableStateFlow(false)
    val isAllRunning: StateFlow<Boolean> = _isAllRunning.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        // 启动时拉取引擎状态并开启端口轮询
        refresh()
        viewModelScope.launch {
            while (true) {
                delay(3000)
                refresh()
            }
        }
    }

    fun refresh() {
        _services.value = EngineController.services.value
        _isAllRunning.value = _services.value.isNotEmpty() && _services.value.all { it.isRunning }
    }

    fun toggle(type: ServiceType) {
        viewModelScope.launch {
            val svc = _services.value.first { it.type == type }
            if (svc.isRunning) {
                EngineController.stop(type)
            } else {
                _loading.value = true
                runCatching { EngineController.start(type) }
                    .onFailure { com.mobileserver.util.LogManager.append("ui", "[error] 启动失败: ${it.message}") }
                _loading.value = false
            }
            refresh()
        }
    }

    fun startAll() {
        viewModelScope.launch {
            _loading.value = true
            runCatching { EngineController.startAll() }
                .onFailure { com.mobileserver.util.LogManager.append("ui", "[error] 一键启动失败: ${it.message}") }
            delay(500)
            _loading.value = false
            refresh()
        }
    }

    fun stopAll() {
        EngineController.stopAll()
        refresh()
    }

    /** 端口占用状态（用于服务卡片显示） */
    fun isPortBusy(port: Int): Boolean = PortChecker.isPortInUse(port)
}
