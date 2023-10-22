package group.nhom14.textospeech

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
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

    fun initTestData(){
        viewModelScope.launch(Dispatchers.IO) {
            audioFileDao.insert(AudioFile(0,"test1","/test1"))
            audioFileDao.insert(AudioFile(0,"test2","/test2"))
            audioFileDao.insert(AudioFile(0,"test3","/test3"))
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


}