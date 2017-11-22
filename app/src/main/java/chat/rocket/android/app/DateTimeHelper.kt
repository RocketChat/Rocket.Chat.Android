import android.content.Context
import chat.rocket.android.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit

object DateTimeHelper {
    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)
    private val lastWeek = today.minus(1, ChronoUnit.WEEKS)

    /**
     * Returns a date or the correlated expression from a LocalDateTime.
     * REMARK: this will return "today", "yesterday", the day of the week or the localDate if the LocalDateTime is from a week ago.
     *
     * @param localDateTime The LocalDateTime.
     * @param context The context.
     * @return The date or the correlated expression from a LocalDateTime.
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
                    when (localDateTime.dayOfWeek) {
                        DayOfWeek.MONDAY -> context.getString(R.string.msg_monday)
                        DayOfWeek.TUESDAY -> context.getString(R.string.msg_tuesday)
                        DayOfWeek.WEDNESDAY -> context.getString(R.string.msg_wednesday)
                        DayOfWeek.THURSDAY -> context.getString(R.string.msg_thursday)
                        DayOfWeek.FRIDAY -> context.getString(R.string.msg_friday)
                        DayOfWeek.SATURDAY -> context.getString(R.string.msg_saturday)
                        else -> context.getString(R.string.msg_sunday)
                    }
                }
            }
        }
    }

    private fun formatDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return localDate.format(formatter).toString()
    }
}