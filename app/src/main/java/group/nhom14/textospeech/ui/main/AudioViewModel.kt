package group.nhom14.textospeech.ui.main

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import group.nhom14.textospeech.App
import group.nhom14.textospeech.R
import group.nhom14.textospeech.model.AudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import group.nhom14.textospeech.BuildConfig

class AudioViewModel : ViewModel() {
    private val audioFileDao = App.database.audioFileDao()
    private val _audioList: MutableLiveData<List<AudioFile>> = MutableLiveData<List<AudioFile>>()
    var audioList = _audioList

    init {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.getAll().collect {
                _audioList.postValue(it)
            }
        }
    }

    fun initTestData() {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.insert(AudioFile(0, "test1", "/test1"))
            audioFileDao.insert(AudioFile(0, "test2", "/test2"))
            audioFileDao.insert(AudioFile(0, "test3", "/test3"))
        }
    }

    fun insert(audioFile: AudioFile) {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.insert(audioFile)
        }
    }

    fun delete(audioFile: AudioFile) {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.delete(audioFile)
        }
    }

    fun update(id: Int, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.update(id, name)
        }
    }


    fun setRingtone(context: Context, filePath: String, fileName: String,callBack:() ->Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val file = File(
                saveRingTone(
                    fileDir = filePath,
                    newName = fileName
                )
            )
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            values.put(MediaStore.MediaColumns.TITLE, file.name)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            values.put(MediaStore.Audio.Media.ARTIST, "Group-14")
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            if (uri != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                context.contentResolver.delete(
                    uri,
                    MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                    null
                )
            }
            val newUri: Uri? = uri?.let { context.contentResolver.insert(it, values) }
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
            callBack.invoke()
        }


    }


    private fun saveRingTone(fileDir: String, newName: String): String {
        val targetDir = "${
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_RINGTONES
            )
        }"
        val file = File(fileDir)
        file.copyTo(
            target = File(targetDir, newName),
            overwrite = true
        )
        return File(targetDir, newName).absolutePath
    }


    fun shareFile(context: Context, audioFile: AudioFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(audioFile.filePath)
            val uri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file.absoluteFile
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "audio/mp3"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.share_text_to_speech_audio)
                )
            )
        }

    }


}