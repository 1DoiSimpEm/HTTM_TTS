package group.nhom14.textospeech.util

import java.util.concurrent.TimeUnit

object Util {
     fun Int.formatMinSec(): String {
        return if (this == 0) {
            "00:00"
        } else {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(this.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(this.toLong()) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(this.toLong())
                        )
            )
        }
    }

}