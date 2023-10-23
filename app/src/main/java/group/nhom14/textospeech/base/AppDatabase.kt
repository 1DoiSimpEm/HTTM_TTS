package group.nhom14.textospeech.base

import androidx.room.Database
import androidx.room.RoomDatabase
import group.nhom14.textospeech.model.AudioFile
import group.nhom14.textospeech.model.AudioFileDao

@Database(entities = [AudioFile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
}