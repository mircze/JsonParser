package com.mircze.jsonparser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.playground.customJson.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SPACE = "    "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sampleJson =
            "{\"ssoId\":\"suby0200000259\",\"externalSsoType\":\"UBITRICITY\",\"socketNumber\":\"suby0200000259\",\"deviceNote\":null,\"taken\":null,\"active\":null,\"isPublic\":null,\"addressAdd1\":null,\"addressAdd2\":null,\"addressAdd3\":null,\"state\":null,\"directAccess\":true,\"tariff\":null,\"daTariff\":null,\"flexibleTariff\":{\"id\":\"5da5dd06ea4031000157fe05\",\"vat\":0.2,\"currency\":\"GBP\",\"fees\":[{\"description\":\"Default Plug In Fee for UK\",\"type\":\"PLUG_IN_FEE\",\"userPricePerUnit\":0.0,\"payoutToProviderPerUnit\":0.0,\"billingUnit\":\"EVENT\",\"freeInterval\":0,\"penaltyInterval\":0,\"mobileMeterCharging\":true,\"directAccessCharging\":true},{\"description\":\"Default Work Fee for UK\",\"type\":\"WORK_FEE\",\"userPricePerUnit\":0.24,\"payoutToProviderPerUnit\":0.0,\"billingUnit\":\"KWH\",\"freeInterval\":0,\"penaltyInterval\":0,\"mobileMeterCharging\":true,\"directAccessCharging\":true}],\"createdOn\":\"2019-10-15T14:51:50.650Z\",\"name\":\"DEFAULT_GB\"},\"openingTimes\":null,\"address\":{\"street\":\"Randolph Ave 97\",\"street2\":\"testing\",\"city\":\"London\",\"zipCode\":\"W9 1DL\",\"country\":\"GB\",\"location\":{\"lat\":51.52830999999999761485014460049569606781005859375,\"lng\":-0.1842580000000000051141313406333210878074169158935546875}}}"
        val sampleJsonCut =
            " { \"ssoId\":\"suby0200000259\",\"externalSsoType\":\"UBITRICITY\",\"socketNumber\":\"suby0200000259\",\"deviceNote\":null,\"taken\":null,\"active\":null,\"isPublic\":null,\"addressAdd1\":null,\"addressAdd2\":null,\"addressAdd3\":null,\"state\":null,\"directAccess\":true,\"tariff\":null,\"daTariff\":null,\"flexibleTariff\":{\"id\":\"5da5dd06ea4031000157fe05\",\"vat\":0.2,\"currency\":\"GBP\",\"fees\":null,\"createdOn\":\"2019-10-15T14:51:50.650Z\",\"name\":\"DEFAULT_GB\"},\"openingTimes\":null,\"address\":{\"street\":\"Randolph Ave 97\",\"street2\":\"testing\",\"city\":\"London\",\"zipCode\":\"W9 1DL\",\"country\":\"GB\",\"location\":{\"lat\":51.52830999999999761485014460049569606781005859375,\"lng\":-0.1842580000000000051141313406333210878074169158935546875}}}"
        val sampleJson2 =
            "[{\"description\":\"Default Plug In Fee for UK\",\"type\":\"PLUG_IN_FEE\",\"userPricePerUnit\":0.0,\"payoutToProviderPerUnit\":0.0,\"billingUnit\":\"EVENT\",\"freeInterval\":0,\"penaltyInterval\":0,\"mobileMeterCharging\":true,\"directAccessCharging\":true},{\"description\":\"Default Work Fee for UK\",\"type\":\"WORK_FEE\",\"userPricePerUnit\":0.24,\"payoutToProviderPerUnit\":0.0,\"billingUnit\":\"KWH\",\"freeInterval\":0,\"penaltyInterval\":0,\"mobileMeterCharging\":true,\"directAccessCharging\":true}]"
        val data = parseObject(sampleJson2)
        printJsonObject(jsonOutput, data, 0)
    }

    private fun parseObject(data: String): CustomJsonObject {
        val result = CustomJsonObject()
        var currentData = data.trimStart().trimEnd().removePrefix(",").removeSurrounding("{", "}")
        while (currentData.isNotEmpty()) {
            val row = currentData.split(":", limit = 2)
            val key = row.first().trimStart().trimEnd().removePrefix(",").removeSurrounding("\"")
            val rawValue = row.last().trimStart().trimEnd()
            val value: BaseCustomJson = when {
                rawValue.startsWith("[") -> {
                    val partResult = getNestedObjectFromString(JsonType.JSON_ARRAY, rawValue)
                    currentData = partResult.second
                    parseArray(partResult.first)
                }
                rawValue.startsWith("{") -> {
                    val partResult = getNestedObjectFromString(JsonType.JSON_OBJECT, rawValue)
                    currentData = partResult.second
                    parseObject(partResult.first)
                }
                else -> {
                    val partResult = rawValue.split(",", limit = 2)
                    currentData = if (partResult.size > 1) partResult.last() else ""
                    CustomJsonValue(partResult.first().trimStart().trimEnd().removeSurrounding("\""))
                }
            }
            result.elements.add(CustomJsonElement(key, value))
        }
        return result
    }

    private fun parseArray(data: String): CustomJsonArray {
        val result = CustomJsonArray()
        var currentData = data.trimStart().trimEnd().removeSurrounding("[", "]")
        while (currentData.isNotEmpty()) {
            val partResult = getNestedObjectFromString(JsonType.JSON_OBJECT, currentData)
            currentData = partResult.second
            val value = parseObject(partResult.first)
            result.elements.add(value)
        }
        return result
    }

    //returns 2 strings, object and rest
    private fun getNestedObjectFromString(type: JsonType, data: String): Pair<String, String> {
        var nesting = 0
        var finalIndex = 0
        val beginChar = if (type == JsonType.JSON_ARRAY) '[' else '{'
        val endChar = if (type == JsonType.JSON_ARRAY) ']' else '}'
        for (index in data.indices) {
            if (data[index] == beginChar) {
                nesting++
            } else if (data[index] == endChar) {
                nesting--
                if (nesting == 0) {
                    finalIndex = index
                    break
                }
            }
        }
        return Pair(data.substring(0, finalIndex + 1), data.substring(finalIndex + 1))
    }

    private fun printJsonObject(output: TextView, data: CustomJsonObject, level: Int, skipStartSpace: Boolean = false, skipNewLineAfterStartChar: Boolean = false, skipNewLineAfterEndChar: Boolean = false) {
        if (level == 0) output.text = ""
        if (!skipStartSpace) {
            output.append("     ".repeat(level))
        }
        output.append("{")
        if (!skipNewLineAfterStartChar) {
            output.append("\n")
        }
        for (item in data.elements) {
            output.append(SPACE.repeat(level + 1))
            when (item.value) {
                is CustomJsonObject -> {
                    output.append("${item.key} : ")
                    printJsonObject(output, item.value, level+1, true)
                }
                is CustomJsonArray -> {
                    output.append("${item.key} : [ \n")
                    item.value.elements.forEachIndexed { index, singleObject ->
                        val isLast = index + 1 == item.value.elements.size
                        printJsonObject(output, singleObject, level + 2, false, false, true)
                        if (!isLast) {
                            output.append(",\n")
                        }
                    }
                    output.append("\n")
                    output.append(SPACE.repeat(level + 1))
                    output.append("],\n")
                }
                is CustomJsonValue -> {
                    when (item.value.value) {
                        is CustomJsonBooleanElement -> {
                            output.append("${item.key} : ${(item.value.value as CustomJsonBooleanElement).value} \n")
                        }
                        is CustomJsonFixedPointElement -> {
                            output.append("${item.key} : ${(item.value.value as CustomJsonFixedPointElement).value} \n")
                        }
                        is CustomJsonFloatingPointElement -> {
                            output.append("${item.key} : ${(item.value.value as CustomJsonFloatingPointElement).value} \n")
                        }
                        is CustomJsonStringElement -> {
                            output.append("${item.key} : ${(item.value.value as CustomJsonStringElement).value} \n")
                        }
                        null -> {
                            output.append("${item.key} : ${item.value.value} \n")
                        }
                    }
                }
            }
            Log.d("miro", "Key: ${item.key} \t type: ${item.value?.elementName}")
        }
        output.append("     ".repeat(level))
        output.append("}")
        if (!skipNewLineAfterEndChar) {
            output.append("\n")
        }
    }

    enum class JsonType {
        JSON_OBJECT, JSON_ARRAY
    }
}
