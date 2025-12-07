package com.example.labs
// YooMoneyService.kt
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class YooMoneyService(private val context: Context) {

    private val client = OkHttpClient()
    private val authManager = AuthManager(context)

    /**
     * Создает ссылку для оплаты через YooMoney
     */
    fun createPaymentLink(
        sum: Int,
        label: String = generatePaymentLabel()
    ): String {
        val receiver = Config.RECEIVER_WALLET
        val paymentType = "PC" // Можно изменить на PC, AC

        return "https://yoomoney.ru/quickpay/confirm.xml?" +
                "receiver=$receiver&" +
                "quickpay-form=shop&" +
                "targets=Покупка монет для кроссвордов&" +
                "paymentType=$paymentType&" +
                "sum=$sum&" +
                "label=$label"
    }

    /**
     * Генерирует уникальную метку для платежа
     */
    fun generatePaymentLabel(): String {
        val email = authManager.getCurrentEmail() ?: "unknown"
        val timestamp = System.currentTimeMillis()
        return "${email.hashCode()}_${timestamp}_${(1000..9999).random()}"
    }

    /**
     * Проверяет платежи по метке
     */
    suspend fun checkPayment(label: String): PaymentStatus {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://yoomoney.ru/api/operation-history"

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${Config.ACCESS_TOKEN}")
                    .post(RequestBody.create(null, ""))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val jsonObject = JSONObject(jsonString ?: "{}")
                    val operations = jsonObject.optJSONArray("operations")

                    if (operations != null) {
                        for (i in 0 until operations.length()) {
                            val operation = operations.getJSONObject(i)
                            val operationLabel = operation.optString("label", "")
                            val status = operation.optString("status", "")
                            val amount = operation.optDouble("amount", 0.0)

                            if (operationLabel == label && status == "success") {
                                return@withContext PaymentStatus(
                                    success = true,
                                    amount = amount,
                                    message = "Платеж подтвержден"
                                )
                            }
                        }
                    }
                }

                PaymentStatus(
                    success = false,
                    amount = 0.0,
                    message = "Платеж не найден или не подтвержден"
                )

            } catch (e: IOException) {
                PaymentStatus(
                    success = false,
                    amount = 0.0,
                    message = "Ошибка сети: ${e.message}"
                )
            } catch (e: Exception) {
                PaymentStatus(
                    success = false,
                    amount = 0.0,
                    message = "Ошибка: ${e.message}"
                )
            }
        }
    }

    /**
     * Ищет последние платежи пользователя
     */
    suspend fun getUserPayments(): List<PaymentInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://yoomoney.ru/api/operation-history"
                val email = authManager.getCurrentEmail() ?: return@withContext emptyList()

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${Config.ACCESS_TOKEN}")
                    .post(RequestBody.create(null, ""))
                    .build()

                val response = client.newCall(request).execute()
                val payments = mutableListOf<PaymentInfo>()

                if (response.isSuccessful) {
                    val jsonString = response.body?.string()
                    val jsonObject = JSONObject(jsonString ?: "{}")
                    val operations = jsonObject.optJSONArray("operations")

                    if (operations != null) {
                        for (i in 0 until operations.length()) {
                            val operation = operations.getJSONObject(i)
                            val label = operation.optString("label", "")
                            val status = operation.optString("status", "")
                            val amount = operation.optDouble("amount", 0.0)
                            val datetime = operation.optString("datetime", "")
                            val title = operation.optString("title", "")

                            // Проверяем, содержит ли метка email пользователя
                            if (label.contains(email.hashCode().toString()) && status == "success") {
                                payments.add(PaymentInfo(
                                    amount = amount,
                                    datetime = datetime,
                                    title = title,
                                    status = status
                                ))
                            }
                        }
                    }
                }

                payments
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

data class PaymentStatus(
    val success: Boolean,
    val amount: Double,
    val message: String
)

data class PaymentInfo(
    val amount: Double,
    val datetime: String,
    val title: String,
    val status: String
)