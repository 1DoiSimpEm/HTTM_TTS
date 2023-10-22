package group.nhom14.textospeech

import android.app.Application
import androidx.room.Room

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