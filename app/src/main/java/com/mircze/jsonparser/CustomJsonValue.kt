package com.example.playground.customJson

import android.util.Log

/**
 * Created by Mirosław Juda on 06.11.2019.
 */
class CustomJsonValue(value: Any): BaseCustomJson() {
    var value: Any?
    init {
        when (value) {
            is CustomJsonObject -> {
                Log.d("miro", "$value is Object")
                this.value = value
            }
            is CustomJsonArray -> {
                Log.d("miro", "$value is Array")
                this.value = value
            }
            is String -> {
                if (value == "null") {
                    Log.d("miro", "$value is null")
                    this.value = null
                } else {
                    val doubleValue = value.toDoubleOrNull()
                    val longValue = value.toLongOrNull()
                    if (doubleValue != null && longValue != null) {
                        if (doubleValue == longValue.toDouble()) {
                            Log.d("miro", "$value is Long")
                            this.value = CustomJsonFixedPointElement(longValue)
                        } else {
                            Log.d("miro", "$value is Double")
                            this.value = CustomJsonFloatingPointElement(doubleValue)
                        }
                    } else {
                        if (doubleValue != null) {
                            Log.d("miro", "$value is Double")
                            this.value = CustomJsonFloatingPointElement(doubleValue)
                        } else if (longValue != null) {
                            Log.d("miro", "$value is Long")
                            this.value = CustomJsonFixedPointElement(longValue)
                        } else if (value.toLowerCase() == "true" || value.toLowerCase() == "false"){
                            Log.d("miro", "$value is Boolean")
                            this.value = CustomJsonBooleanElement(value.toBoolean())
                        } else {
                            Log.d("miro", "$value is String")
                            this.value = CustomJsonStringElement(value)
                        }
                    }
                }
            }
            else -> {
                Log.d("miro", "$value is other")
                this.value = value
            }
        }
    }
}
