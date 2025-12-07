import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.labs.CrosswordCell
import com.example.labs.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CoinManager(context: Context) {

    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _userCoins = MutableLiveData<Int>()
    val userCoins: MutableLiveData<Int> get() = _userCoins

    // ===== ПОЛУЧЕНИЕ МОНЕТ (БЕЗ FLOW!) =====
    private suspend fun getCoinsDirect(email: String): Int {
        return userDao.getCoins(email).firstOrNull() ?: 0
    }

    // ===== ДОБАВЛЕНИЕ =====
    suspend fun addCoins(email: String, amount: Int) {
        withContext(Dispatchers.IO) {
            userDao.changeCoins(email, amount)
            val newValue = getCoinsDirect(email)
            _userCoins.postValue(newValue)
        }
    }

    // ===== ТРАТА =====
    suspend fun spendCoins(email: String, amount: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val currentCoins = getCoinsDirect(email)

            if (currentCoins >= amount) {
                userDao.changeCoins(email, -amount)
                val newValue = getCoinsDirect(email)
                _userCoins.postValue(newValue)
                true
            } else {
                false
            }
        }
    }

    // ===== УСТАНОВКА ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ =====
    fun setCurrentUserEmail(email: String) {
        scope.launch {
            val coins = getCoinsDirect(email)
            _userCoins.postValue(coins)
        }
    }

    // ===== ЛОГИКА ПОДСКАЗКИ ОСТАВЛЯЕМ =====
    fun getRandomUnfilledLetter(
        grid: Array<Array<CrosswordCell>>,
        userGrid: Array<Array<Char?>>
    ): Pair<Int, Int>? {
        val unfilledCells = mutableListOf<Pair<Int, Int>>()

        for (i in grid.indices) {
            for (j in grid[i].indices) {
                val cell = grid[i][j]
                if (!cell.isBlack && userGrid[i][j] == null) {
                    unfilledCells.add(Pair(i, j))
                }
            }
        }

        return unfilledCells.randomOrNull()
    }
}
