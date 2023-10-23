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
        private const val BASE_URL = "https://3bdf-14-231-130-155.ngrok-free.app"
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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val audioId =
                    withContext(Dispatchers.Default) {
                        getAudioId("$BASE_URL/tts?text=${mBinding.ttsEditText.text}")
                    }
                if (audioId != null) {
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "Audio ID: $audioId")
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource("$BASE_URL/audiogen?id=$audioId")
//                            setDataSource("https://doc-0s-7g-docs.googleusercontent.com/docs/securesc/pf90nneoelfe9erk5nq9uatm55ijlnvt/qr1nuv73fjmvl1gt7a5updoof74cc8a7/1698078975000/12097368366067485510/12097368366067485510/1fZiZjeeXRF9_HP5GpeuKm2v2GhcoFEIm?e=open&ax=AI0foUqKjPFR1kij9maUeQF4Xl4vdxEdJhusWaLhnKglU66gWwDqXSmFx1ReVZ_vioIcwdDtPQzRAAeGgvsOAF9QUEHLOzMLxNcS0cFV-sJX_PU0Cfb_-CfqWft63qcUm0uW61BDMrtUSV3xHlLg980v80bil55RuA8MCBGX2SyFRgmDrazp4dlE3IsvgVSA6daNPn54ULafHq2EVDK_CYxRk_Ry8FcN-lTchZnbx1w-B2kAOUhEt3JG1xnru7KBPHVI3gzRB7olN8NEHNUZ8OS5NFYpCtY6i4ni6dv6ovhdRZ30KKknbcDT-lx2keN3q7Ke8dBWHjKFCsR0WBAQDcGVC-uk642MwoPTeNXfOYYTGyxTK6V2Vu9CzIrQlVcmseIOUMh5Yq4QQetkHZl-5JQ1TLlzG18DjjFsi62CEJryQ9xV-JeKAk5jme8XSG6oztRmKa7LZcHrOEd9zKdSzcop6he8-1ZxnLYf9Ds_P48dkHiyAGLRtUdZ1SJ6njC7I1YkgpwaCp1Z-wswPbgk415turrna6ToFTiegSdQr8HtxEYTOFyztGSczLCeYqvkIGb7CcKsUI0TdfIK9_s-FYEvJzb79np4xj1gMm5pKXevM-WEVfhI4Vql2Z3b_JmW4ACCbRhtzZMpN19Pvv_QyIBgE0LIafXIhjPT7GD5a-D9RMXiWNbguIHjeiRY_n6Me8B1xoCyvTAzJM0X84c3L1gGcTpmXMGFxU644Azyrq7pHTxvrerLDy2ednLrVwpCkBjovMSq8Pel8_pqqYxWKGsD0n7_cjKNK-gWxUN0bivc-XnngtYYguvfOn1DezWlWV084IimTkxSkvLJ9IQJyHjCJRpWmaLuEynbcjVkHHuRNWPDo3o8XfljKx3wVlrlBYo6-jfK9e03S4gX7IY6wevoMHgXk_lLyG-v7U11sS5L_vf8nNhgAUfGf19Z-BVPyB12CzzyYTpSM0fEXEm4VtMi9ZAcTFkejuTzLK9iTCuDzCX4QTpdsd2sNHUjEj2AFZvdtLqK9kcWIZjLXO-wKWZiPCgpyQzGT0Ukm78d5H-oTLSctw&uuid=3beb7788-ad64-4876-8985-d51922a6418d&authuser=0")
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

