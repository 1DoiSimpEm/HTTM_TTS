package group.nhom14.textospeech

import android.app.Application
import androidx.room.Room
import group.nhom14.textospeech.base.AppDatabase

class App : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "my-db"
        ).build()
    }
}