package com.example.labs

import CoinManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.labs.model.YooMoneyPayment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class YooMoneyPaymentManager(
    private val context: Context,
    private val coinManager: CoinManager,
    private val authManager: AuthManager
) {

    // Ваш номер кошелька ЮMoney (4100...)
    private val yooMoneyWalletNumber = "4100119426251860" // Замените на ваш

    // Список доступных платежей
    val paymentItems = listOf(
        YooMoneyPayment(
            id = "payment_100",
            name = "100 монет",
            description = "Малый набор",
            coinsAmount = 100,
            amount = 99.0,
            walletNumber = yooMoneyWalletNumber,
            comment = "coins_100"
        ),
        YooMoneyPayment(
            id = "payment_500",
            name = "500 монет",
            description = "Средний набор",
            coinsAmount = 500,
            amount = 399.0,
            walletNumber = yooMoneyWalletNumber,
            comment = "coins_500"
        ),
        YooMoneyPayment(
            id = "payment_1000",
            name = "1000 монет",
            description = "Большой набор",
            coinsAmount = 1000,
            amount = 699.0,
            walletNumber = yooMoneyWalletNumber,
            comment = "coins_1000"
        ),
        YooMoneyPayment(
            id = "payment_custom",
            name = "Произвольная сумма",
            description = "Любая сумма на ваше усмотрение",
            coinsAmount = 0, // Рассчитывается на сервере
            amount = 0.0,
            walletNumber = yooMoneyWalletNumber,
            comment = "coins_custom"
        )
    )

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var webViewLauncher: ActivityResultLauncher<Intent>

    // Callbacks
    var onPaymentProcessed: ((YooMoneyPayment, Int) -> Unit)? = null
    var onPaymentError: ((String) -> Unit)? = null

    // Для отслеживания ожидающих платежей
    private val pendingPayments = mutableMapOf<String, PendingPayment>()

    data class PendingPayment(
        val paymentId: String,
        val userId: String,
        val amount: Double,
        val coinsAmount: Int,
        val timestamp: Long,
        val status: PaymentStatus
    )

    enum class PaymentStatus {
        PENDING, VERIFIED, EXPIRED
    }

    fun initialize(activity: ComponentActivity) {
        webViewLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // После возврата из приложения ЮMoney
            handlePaymentReturn()
        }
    }

    // Инициировать платеж через приложение ЮMoney
    fun initiatePayment(paymentItem: YooMoneyPayment) {
        val userEmail = authManager.getCurrentEmail() ?: "unknown@user.com"
        val userName = authManager.getCurrentEmail()?.substringBefore("@") ?: "user"

        // Генерируем уникальный комментарий для идентификации платежа
        val uniqueComment = generateUniqueComment(paymentItem, userName)

        // Формируем intent для приложения ЮMoney
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(buildYooMoneyUri(paymentItem, uniqueComment))
            `package` = "ru.yoomoney.sdk.kassa" // Пакет приложения ЮMoney
        }

        // Проверяем, установлено ли приложение
        val packageManager = context.packageManager
        val resolveInfo = packageManager.resolveActivity(intent, 0)

        if (resolveInfo != null) {
            // Приложение ЮMoney установлено
            webViewLauncher.launch(intent)

            // Сохраняем информацию об ожидающем платеже
            savePendingPayment(paymentItem, userEmail, uniqueComment)

            // Показываем инструкции
            showPaymentInstructions(paymentItem, uniqueComment)
        } else {
            // Приложение не установлено - открываем в браузере
            openInBrowser(paymentItem, uniqueComment)
        }
    }

    // Открыть оплату в браузере
    private fun openInBrowser(paymentItem: YooMoneyPayment, comment: String) {
        val uri = buildYooMoneyUri(paymentItem, comment)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)

        showPaymentInstructions(paymentItem, comment)
    }

    // Построить URI для ЮMoney
    private fun buildYooMoneyUri(paymentItem: YooMoneyPayment, comment: String): String {
        return Uri.parse("https://yoomoney.ru/quickpay/confirm")
            .buildUpon()
            .appendQueryParameter("receiver", paymentItem.walletNumber)
            .appendQueryParameter("sum", paymentItem.amount.toString())
            .appendQueryParameter("formcomment", paymentItem.description)
            .appendQueryParameter("short-dest", paymentItem.name)
            .appendQueryParameter("label", comment)
            .appendQueryParameter("quickpay-form", "donate")
            .appendQueryParameter("targets", "Пополнение монеток в Crossword Puzzle")
            .appendQueryParameter("comment", comment)
            .appendQueryParameter("successURL", "yoomoney://success")
            .build()
            .toString()
    }

    // Генерация уникального комментария
    private fun generateUniqueComment(paymentItem: YooMoneyPayment, userName: String): String {
        val timestamp = System.currentTimeMillis()
        val date = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date(timestamp))
        val random = (1000..9999).random()

        return "${paymentItem.comment}_${userName}_${date}_$random"
    }

    // Сохранить ожидающий платеж
    private fun savePendingPayment(
        paymentItem: YooMoneyPayment,
        userId: String,
        comment: String
    ) {
        val pendingPayment = PendingPayment(
            paymentId = comment,
            userId = userId,
            amount = paymentItem.amount,
            coinsAmount = paymentItem.coinsAmount,
            timestamp = System.currentTimeMillis(),
            status = PaymentStatus.PENDING
        )

        pendingPayments[comment] = pendingPayment

        // Сохраняем в SharedPreferences для восстановления
        saveToPreferences(comment, pendingPayment)

        // Запускаем таймер на истечение (например, 1 час)
        startExpirationTimer(comment)
    }

    // Обработка возврата из ЮMoney
    private fun handlePaymentReturn() {
        // Здесь пользователь вернулся из приложения ЮMoney
        // Мы не знаем, совершил ли он платеж, поэтому:

        // 1. Показываем сообщение с инструкцией
        showVerificationInstructions()

        // 2. Предлагаем проверить платеж вручную
        // или ждем уведомления от нашего сервера/telegram бота
    }

    // Проверить платеж вручную (вы будете проверять в приложении ЮMoney)
    fun verifyPaymentManually(paymentId: String) {
        scope.launch {
            val pendingPayment = pendingPayments[paymentId]
            if (pendingPayment != null && pendingPayment.status == PaymentStatus.PENDING) {
                // Обновляем статус
                pendingPayments[paymentId] = pendingPayment.copy(status = PaymentStatus.VERIFIED)

                // Начисляем монетки
                val coins = if (pendingPayment.coinsAmount == 0) {
                    calculateCoinsFromAmount(pendingPayment.amount)
                } else {
                    pendingPayment.coinsAmount
                }

                coinManager.addCoins(pendingPayment.userId, coins)

                // Уведомляем UI
                paymentItems.find { it.comment == paymentId }?.let { paymentItem ->
                    onPaymentProcessed?.invoke(paymentItem, coins)
                }

                // Удаляем из ожидания
                removeFromPreferences(paymentId)
            }
        }
    }

    // Рассчитать монетки из суммы (для произвольных платежей)
    private fun calculateCoinsFromAmount(amount: Double): Int {
        // 1 рубль = 1 монетка (можно изменить логику)
        return amount.toInt()
    }

    // Показать инструкции
    private fun showPaymentInstructions(paymentItem: YooMoneyPayment, comment: String) {
        // Можно показать AlertDialog или Snackbar с инструкциями
        // Например: "После оплаты пришлите скриншот в поддержку"
    }

    private fun showVerificationInstructions() {
        // Инструкции после возврата из ЮMoney
    }

    // Сохранить в SharedPreferences
    private fun saveToPreferences(paymentId: String, payment: PendingPayment) {
        val prefs = context.getSharedPreferences("pending_payments", Context.MODE_PRIVATE)
        prefs.edit()
            .putString(paymentId, "${payment.userId}|${payment.amount}|${payment.timestamp}")
            .apply()
    }

    // Загрузить из SharedPreferences
    fun loadPendingPayments() {
        val prefs = context.getSharedPreferences("pending_payments", Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            val parts = (value as String).split("|")
            if (parts.size == 3) {
                val payment = PendingPayment(
                    paymentId = key,
                    userId = parts[0],
                    amount = parts[1].toDouble(),
                    coinsAmount = 0,
                    timestamp = parts[2].toLong(),
                    status = PaymentStatus.PENDING
                )
                pendingPayments[key] = payment
            }
        }
    }

    private fun removeFromPreferences(paymentId: String) {
        val prefs = context.getSharedPreferences("pending_payments", Context.MODE_PRIVATE)
        prefs.edit().remove(paymentId).apply()
    }

    private fun startExpirationTimer(paymentId: String) {
        // Через 1 час помечаем как истекший
        scope.launch {
            kotlinx.coroutines.delay(60 * 60 * 1000L) // 1 час

            pendingPayments[paymentId]?.let { payment ->
                if (payment.status == PaymentStatus.PENDING) {
                    pendingPayments[paymentId] = payment.copy(status = PaymentStatus.EXPIRED)
                    removeFromPreferences(paymentId)
                }
            }
        }
    }

    // Получить список ожидающих платежей
    fun getPendingPayments(): List<PendingPayment> {
        return pendingPayments.values.toList()
    }
}