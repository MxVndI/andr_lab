package com.example.labs
// QuickPayService.kt
class QuickPayService {

    /**
     * Создает ссылку для оплаты через YooMoney
     * @param sum Сумма оплаты
     * @param label Метка платежа (для идентификации)
     * @return Ссылка для оплаты
     */
    fun createPaymentLink(
        sum: Double = Config.DEFAULT_SUM,
        label: String = Config.DEFAULT_LABEL,
        paymentType: String = "SB" // SB - Сбербанк, PC - Яндекс.Деньги, AC - банковская карта
    ): String {
        // Формируем URL для оплаты через YooMoney
        return "https://yoomoney.ru/quickpay/confirm.xml?" +
                "receiver=${Config.RECEIVER_WALLET}&" +
                "quickpay-form=shop&" +
                "targets=Sponsor this project&" +
                "paymentType=$paymentType&" +
                "sum=$sum&" +
                "label=$label"
    }

    /**
     * Создает объект PaymentLink с разными форматами URL
     */
    fun createPaymentLinkObject(
        sum: Double = Config.DEFAULT_SUM,
        label: String = Config.DEFAULT_LABEL
    ): PaymentLink {
        val baseUrl = "https://yoomoney.ru/quickpay/confirm.xml"
        val redirectedUrl = "https://yoomoney.ru/quickpay/confirm.xml"
        val paymentUrl = createPaymentLink(sum, label)

        return PaymentLink(baseUrl, redirectedUrl, paymentUrl)
    }
}