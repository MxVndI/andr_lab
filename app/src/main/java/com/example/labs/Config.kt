package com.example.labs

// Config.kt
object Config {
    const val ACCESS_TOKEN = "4100119426251860.7B22557EAA91873A0AFC442E2742396D1CE053845E30B7553180116B623B1DEF9B5E9232E636D0500975793EF48C8EAAC817E4EC8E83A05C72B51CA40BD7CBB0A15E50DABAB0B288FF5C7D14817782E1E50F98583CA718148B1E71E16E85B7F74E416D1504939F43DBD38AA540C40F838E5A2C1CEE131E93BB2E2905EE62D4F1" // Ваш токен
    const val RECEIVER_WALLET = "4100119426251860"
    const val CLIENT_ID = "8584212C295A38E0347C2D9A8B6B35CA8BBE450EB016079A45100BFE7BAAB149"
    // Номер кошелька
    const val SUCCESS_URL = "crosswordapp://payment/success"
    const val FAIL_URL = "crosswordapp://payment/fail"
    const val DEFAULT_SUM = 50.0                      // Сумма по умолчанию
    const val DEFAULT_LABEL = "Platejic"
    val COIN_PACKAGES = mapOf(
        50 to 50,    // 50 рублей = 10 монет
        100 to 110,   // 100 рублей = 25 монет
        200 to 210,   // 200 рублей = 60 монет (скидка)
        500 to 1000   // 500 рублей = 160 монет (максимальная скидка)
    )
}