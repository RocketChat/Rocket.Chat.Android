import android.content.Context
import chat.rocket.android.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

object DateTimeHelper {
    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)
    private val lastWeek = today.minus(1, ChronoUnit.WEEKS)

    /**
     * Returns a date from a LocalDateTime or the textual representation if the LocalDateTime has a max period of a week from the current date.
     *
     * @param localDateTime The LocalDateTime.
     * @param context The context.
     * @return The date or the textual representation from a LocalDateTime.
     */
    fun getDate(localDateTime: LocalDateTime, context: Context): String {
        val localDate = localDateTime.toLocalDate()
        return when (localDate) {
            today -> localDateTime.toLocalTime().toString()
            yesterday -> context.getString(R.string.msg_yesterday)
            else -> {
                if (Period.between(lastWeek, localDate).days <= 0) {
                    formatDate(localDate)
                } else {
                    localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
            }
        }
    }

    private fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return localDate.format(formatter).toString()
    }
}