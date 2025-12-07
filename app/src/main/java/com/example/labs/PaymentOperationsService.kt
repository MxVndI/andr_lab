package com.example.labs
// PaymentOperationsService.kt
import okhttp3.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PaymentOperationsService {
    private val client = OkHttpClient()
    private val gson = Gson()

    /**
     * Получает историю операций с YooMoney API
     */
    suspend fun getOperationsHistory(): List<PaymentOperation> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://yoomoney.ru/api/operation-history"

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${Config.ACCESS_TOKEN}")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    parseOperationsResponse(jsonResponse ?: "")
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Ищет операции по метке (label)
     */
    suspend fun findOperationsByLabel(label: String = Config.DEFAULT_LABEL): List<PaymentOperation> {
        val allOperations = getOperationsHistory()
        return allOperations.filter { it.label == label }
    }

    /**
     * Парсит ответ от API YooMoney
     */
    private fun parseOperationsResponse(json: String): List<PaymentOperation> {
        val operations = mutableListOf<PaymentOperation>()

        try {
            val jsonObject = gson.fromJson(json, JsonObject::class.java)
            val operationsArray = jsonObject.getAsJsonArray("operations")

            operationsArray?.forEach { operationElement ->
                val operation = operationElement.asJsonObject

                val op = PaymentOperation(
                    operationId = operation.get("operation_id")?.asString ?: "",
                    status = operation.get("status")?.asString ?: "",
                    datetime = parseDateTime(operation.get("datetime")?.asString),
                    title = operation.get("title")?.asString,
                    patternId = operation.get("pattern_id")?.asString,
                    direction = operation.get("direction")?.asString ?: "",
                    amount = operation.get("amount")?.asDouble ?: 0.0,
                    label = operation.get("label")?.asString,
                    type = operation.get("type")?.asString
                )

                operations.add(op)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return operations
    }

    private fun parseDateTime(dateTimeStr: String?): LocalDateTime {
        return try {
            dateTimeStr?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } ?: LocalDateTime.now()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}