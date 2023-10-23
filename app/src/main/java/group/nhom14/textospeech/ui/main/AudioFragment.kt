package group.nhom14.textospeech.ui.main

import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
        private const val BASE_URL = "https://7282-14-231-130-155.ngrok-free.app"
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
                    .addToBackStack(PlayAudioFragment::class.java.name)
                    .commit()
            },
            itemLongClick = {
                OptionBottomSheetFragment(object :
                    OptionBottomSheetFragment.OptionBottomSheetListener {
                    override fun rename() {
                        RenameDialogFragment.newInstance(it.name){name ->
                            viewModel.update(it.id, name)
                        }.show(parentFragmentManager, RenameDialogFragment::class.java.name)
                    }

                    override fun share() {
                        viewModel.shareFile(context ?: return, it)
                    }

                    override fun setRingtone() {
                        viewModel.setRingtone(context ?: return, it.filePath, it.name)
                    }
                    override fun delete() {
                        viewModel.delete(it)
                    }
                })


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
                    ttsPlayButton.setImageResource(R.drawable.ic_tts_playable)
                    mBinding.ttsEditText.setBackgroundResource(R.drawable.bg_langpick_notempty_layout)
                }
            } else {
                mBinding.apply {
                    ttsDownloadButton.setBackgroundResource(R.drawable.bg_tts_unplayed_button)
                    ttsDownloadButton.isEnabled = false
                    ttsPlayButton.setImageResource(R.drawable.ic_tts_play)
                    mBinding.ttsEditText.setBackgroundResource(R.drawable.bg_langpick_empty_layout)
                }
            }
        }

        mBinding.ttsPlayButton.setOnClickListener {
            mBinding.ttsProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                val audioId =
                    withContext(Dispatchers.Default) {
                        getAudioId("$BASE_URL/tts?text=${mBinding.ttsEditText.text}")
                    }
                if (audioId != null) {
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "Audio ID: $audioId")
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource("$BASE_URL/audio?id=$audioId")
                            prepare()
                            start()
                            setOnCompletionListener {
                                mediaPlayer.release()
                                mBinding.ttsPlayButton.setImageResource(R.drawable.ic_tts_playable)
                            }
                            setOnPreparedListener {
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

        mBinding.ttsDownloadButton.setOnClickListener {
            mBinding.ttsProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                val audioId =
                    withContext(Dispatchers.Default) {
                        getAudioId("$BASE_URL/tts?text=${mBinding.ttsEditText.text}")
                    }
                if (audioId != null) {
                    DownloadUtil.downloadAudioFile(
                        "$BASE_URL/tts?text=${mBinding.ttsEditText.text}",
                        context ?: return@launch,
                    ) { success, filePath ->
                        if (success) {
                            Log.d("MainActivity", "Downloaded file: $filePath")
                            val audioFile = AudioFile(
                                name = mBinding.ttsEditText.text.toString(),
                                filePath = filePath
                            )
                            viewModel.insert(audioFile)
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


    private suspend
    fun getAudioId(url: String): String? = suspendCoroutine { continuation ->
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

