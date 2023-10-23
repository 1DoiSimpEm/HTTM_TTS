package group.nhom14.textospeech.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_files")
data class AudioFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val filePath: String
)