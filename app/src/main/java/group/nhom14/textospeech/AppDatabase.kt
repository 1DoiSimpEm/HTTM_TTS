package group.nhom14.textospeech

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AudioFile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
}