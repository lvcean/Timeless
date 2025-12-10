package com.example.myapplication.model

import java.util.UUID

enum class AttributeType(val displayName: String) {
    NUMBER("数值类型"),
    SINGLE_SELECT("单选类型"),
    MULTI_SELECT("多选类型"),
    TEXT("文本类型"), // Short Text default
    LONG_TEXT("长文本"),
    SWITCH("开关类型"),
    RATING("评分类型")
}

data class AttributeOption(
    val label: String,
    val color: Int // ARGB Color
)

data class AttributeDefinition(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AttributeType,
    val options: List<AttributeOption> = emptyList(),
    val description: String = "",
    val isRequired: Boolean = false,
    val unit: String? = null, // Custom unit for display, e.g. "ml"
    val defaultValue: String? = null
)

data class AttributeValue(
    val attributeId: String,
    val value: String
)
