package group.nhom14.textospeech.ui.main

import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import group.nhom14.textospeech.model.AudioFile
import group.nhom14.textospeech.util.DownloadUtil
import group.nhom14.textospeech.R
import group.nhom14.textospeech.databinding.FragmentAudioBinding
import group.nhom14.textospeech.ui.dialog.OptionBottomSheetFragment
import group.nhom14.textospeech.ui.dialog.RenameDialogFragment
import group.nhom14.textospeech.ui.play_audio.PlayAudioFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AudioFragment : Fragment() {
    private lateinit var mBinding: FragmentAudioBinding
    private val viewModel: AudioViewModel by activityViewModels()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioAdapter: AudioAdapter

    companion object {
        private const val BASE_URL = "https://1229-14-231-130-155.ngrok-free.app"
        private const val MAX_CHUNK_LENGTH = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAudioBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
        initActions()
    }


    private fun initView() {
        audioAdapter = AudioAdapter(
            itemClick = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_container, PlayAudioFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()
            },
            itemLongClick = {
                OptionBottomSheetFragment(object : OptionBottomSheetFragment.OptionBottomSheetListener {
                    override fun rename() {
                        RenameDialogFragment.newInstance(it.name){name ->
                            viewModel.update(it.id, name)
                        }.show(childFragmentManager, RenameDialogFragment::class.java.name)
                    }

                    override fun share() {
                        viewModel.shareFile(context ?: return, it)
                    }

                    override fun setRingtone() {
                        viewModel.setRingtone(context ?: return, it.filePath, it.name){
                            Toast.makeText(context, "Set ringtone successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun delete() {
                        viewModel.delete(it)
                    }
                }).show(childFragmentManager, OptionBottomSheetFragment::class.java.name)


            })
        mBinding.ttsRecyclerView.adapter = audioAdapter
    }

    private fun initData() {
        viewModel.audioList.observe(viewLifecycleOwner) {
            audioAdapter.updateList(it)
        }
    }

    private fun initActions() {
        mBinding.ttsEditText.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            setRawInputType(InputType.TYPE_CLASS_TEXT)
        }


        mBinding.ttsEditText.doAfterTextChanged { s ->
            if (!s.isNullOrEmpty()) {
                mBinding.apply {
                    ttsDownloadButton.setBackgroundResource(R.drawable.bg_tts_next_btn)
                    ttsDownloadButton.isEnabled = true
                    ttsClearButton.visibility = View.VISIBLE
                    ttsPlayButton.setImageResource(R.drawable.ic_tts_playable)
                    mBinding.ttsEditText.setBackgroundResource(R.drawable.bg_langpick_notempty_layout)
                }
            } else {
                mBinding.apply {
                    ttsDownloadButton.setBackgroundResource(R.drawable.bg_tts_unplayed_button)
                    ttsDownloadButton.isEnabled = false
                    ttsClearButton.visibility = View.GONE
                    ttsPlayButton.setImageResource(R.drawable.ic_tts_play)
                    mBinding.ttsEditText.setBackgroundResource(R.drawable.bg_langpick_empty_layout)
                }
            }
        }

//        mBinding.ttsPlayButton.setOnClickListener {
//            mBinding.ttsProgressBar.visibility = View.VISIBLE
//            val text = mBinding.ttsEditText.text.toString()
//            val chunks = splitTextIntoChunks(text, MAX_CHUNK_LENGTH)
//            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//                for (chunk in chunks) {
//                    val audioId = withContext(Dispatchers.Default) {
//                        getAudioId("$BASE_URL/tts?text=$chunk")
//                    }
//                    if (audioId != null) {
//                        withContext(Dispatchers.Main) {
//                            Log.d("MainActivity", "Audio ID: $audioId")
//                            mediaPlayer = MediaPlayer().apply {
//                                setDataSource("$BASE_URL/audiogen?id=$audioId")
//                                prepare()
//                                start()
//                                setOnCompletionListener {
//                                    mediaPlayer.release()
//                                    mBinding.ttsPlayButton.setImageResource(R.drawable.ic_tts_playable)
//                                }
//                                setOnPreparedListener {
//                                    mBinding.ttsPlayButton.setImageResource(R.drawable.ic_tts_pause)
//                                    mBinding.ttsProgressBar.visibility = View.GONE
//                                }
//                            }
//                        }
//                    } else {
//                        Log.d("MainActivity", "Error getting audio ID")
//                    }
//                }
//            }
//        }


        mBinding.ttsClearButton.setOnClickListener {
            mBinding.ttsEditText.text.clear()
            mBinding.ttsPlayButton.isEnabled = true
            try {
                mediaPlayer.release()
            } catch (_: Exception) {
            }
        }

        mBinding.ttsPlayButton.setOnClickListener {
            try{
                mBinding.ttsProgressBar.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val text = mBinding.ttsEditText.text.toString()
                    val sentences = text.split("[.!?]".toRegex())
                    playSentences(sentences, 0)
                }
            }
            catch (_:Exception){
            }
        }

        mBinding.ttsDownloadButton.setOnClickListener {
            mBinding.ttsProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                val audioId =
                    withContext(Dispatchers.Default) {
                        getAudioId("$BASE_URL/tts?text=${mBinding.ttsEditText.text}")
                    }
                if (audioId != null) {
                    DownloadUtil.downloadAudioFile(
                        "$BASE_URL/audiogen?id=$audioId",
//                        "https://35.84.154.59:4000/file/AUGHHHHH.mp3",
                        context ?: return@launch,
                    ) { success, filePath ->
                        if (success) {
                            Log.d("MainActivity", "Downloaded file: $filePath")
                            val audioFile = AudioFile(
                                name = mBinding.ttsEditText.text.toString(),
                                filePath = filePath
                            )
                            viewModel.insert(audioFile)
                            mBinding.ttsProgressBar.visibility = View.GONE

                        } else {
                            Log.d("MainActivity", "Error downloading file")
                        }
                    }

                } else {
                    Log.d("MainActivity", "Error getting audio ID")
                }
            }

        }
    }

    private suspend fun playSentences(sentences: List<String>, index: Int) {
        if (index < sentences.size && sentences[index].isNotEmpty()) {
            val audioId = withContext(Dispatchers.Default) {
                getAudioId("$BASE_URL/tts?text=${sentences[index]}")
            }
            if (audioId != null) {
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "$BASE_URL/audiogen?id=$audioId")
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource("$BASE_URL/audiogen?id=$audioId")
                        prepare()
                        start()
                        setOnCompletionListener {
                            if(index == sentences.size - 1){
                                mediaPlayer.release()
                                mBinding.ttsPlayButton.setImageResource(R.drawable.ic_tts_playable)
                                mBinding.ttsPlayButton.isEnabled = true
                            }
                            lifecycleScope.launch(Dispatchers.Main) {
                                playSentences(sentences, index + 1)
                            }
                        }
                        setOnPreparedListener {
                            mBinding.ttsPlayButton.isEnabled = false
                            mBinding.ttsPlayButton.setImageResource(R.drawable.ic_tts_pause)
                            mBinding.ttsProgressBar.visibility = View.GONE
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "Error getting audio ID")
            }
        }
    }

//    private fun splitTextIntoChunks(text: String, maxLength: Int): List<String> {
//        val chunks = mutableListOf<String>()
//        var startIndex = 0
//        while (startIndex < text.length) {
//            val endIndex = startIndex + maxLength
//            val chunk = if (endIndex < text.length) {
//                text.substring(startIndex, endIndex)
//            } else {
//                text.substring(startIndex)
//            }
//            chunks.add(chunk)
//            startIndex = endIndex
//        }
//        return chunks
//    }

    private suspend fun getAudioId(url: String): String? = suspendCoroutine { continuation ->
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("MainActivity", "onFailure: ${e.message}")
                continuation.resume(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val jsonObject = json?.let { JSONObject(it) }
                val audioId = jsonObject?.getString("audio_id")
                continuation.resume(audioId)
            }
        })
    }
}

