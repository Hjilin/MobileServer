package com.mobileserver.ui.theme

import androidx.compose.ui.graphics.Color

// ============ 简云风格配色（对齐其 Web 面板设计系统）============
// 主色：靛蓝 → 紫渐变 (#6366f1 → #8b5cf6)
val Primary = Color(0xFF6366F1)
val Primary2 = Color(0xFF8B5CF6)
val PrimaryDark = Color(0xFF4F46E5)
val PrimaryLight = Color(0xFFEEF0FE)

// 沿用旧名以兼容既有引用，值替换为简云主色
val MiuiOrange = Primary
val MiuiOrangeDark = PrimaryDark
val MiuiOrangeLight = PrimaryLight

// 功能色
val MiuiGreen = Color(0xFF22C55E)
val Green = MiuiGreen
val MiuiRed = Color(0xFFEF4444)
val MiuiBlue = Primary
val Amber = Color(0xFFF59E0B)
val Purple = Primary2

// 背景与表面
val MiuiBackground = Color(0xFFF1F5F9)
val MiuiSurface = Color(0xFFFFFFFF)
val MiuiCard = Color(0xFFFFFFFF)

// 文字
val MiuiTextPrimary = Color(0xFF1E293B)
val MiuiTextSecondary = Color(0xFF64748B)
val MiuiTextHint = Color(0xFF94A3B8)

// 分割线 / 边框
val MiuiDivider = Color(0xFFE2E8F0)
val Border = Color(0xFFE2E8F0)

// 终端日志背景（深色）
val TerminalBg = Color(0xFF0F172A)
val TerminalFg = Color(0xFFE2E8F0)
