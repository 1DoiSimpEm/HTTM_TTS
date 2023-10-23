package group.nhom14.textospeech.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioFileDao {
    @Query("SELECT * FROM audio_files")
    fun getAll(): Flow<List<AudioFile>>

    @Insert
    fun insert(audioFile: AudioFile)

    @Delete
    fun delete(audioFile: AudioFile)

    //update
    @Query("UPDATE audio_files SET name = :name WHERE id = :id")
    fun update(id: Int, name: String)
}