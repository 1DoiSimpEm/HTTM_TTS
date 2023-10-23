package group.nhom14.textospeech.ui.play_audio

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import group.nhom14.textospeech.R
import group.nhom14.textospeech.databinding.FragmentPlayAudioBinding
import group.nhom14.textospeech.model.AudioFile
import group.nhom14.textospeech.ui.main.AudioViewModel
import group.nhom14.textospeech.util.Util.formatMinSec
import kotlinx.coroutines.launch
import java.io.File


class PlayAudioFragment : Fragment() {
    private lateinit var mBinding: FragmentPlayAudioBinding
    private val viewModel: AudioViewModel by activityViewModels()
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var anim: ObjectAnimator
    private lateinit var audioList: MutableList<AudioFile>
    private lateinit var audioFile: AudioFile
    private var seekBarUpdater: Handler? = null

    companion object {
        fun newInstance(file: AudioFile): PlayAudioFragment {
            val args = Bundle()
            val fragment = PlayAudioFragment()
            fragment.arguments = args
            fragment.audioFile = file
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPlayAudioBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initActions()
    }

    private fun initActions() {
        mBinding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        mBinding.btnShare.setOnClickListener {
            viewModel.shareFile(audioFile)
        }
        mBinding.playMyWorkAudioPlayBtn.setOnClickListener {
            if (mMediaPlayer.isPlaying)
                pauseAudio()
            else
                resumeAudio()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                mBinding.playMyWorkAudioSeekBar?.onProgressChanged =
                    object : SeekBarOnProgressChanged {
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
                            if (fromUser)
                                mMediaPlayer.seekTo(progress.toInt())
                        }

                    }
            }
        }

    }

    private fun initViews() {
        viewModel.audioList.observe(viewLifecycleOwner){
            audioList = it as MutableList<AudioFile>
        }
        mMediaPlayer = MediaPlayer.create(context, Uri.fromFile(File(audioFile.filePath)))
        mMediaPlayer.isLooping = true
        mMediaPlayer.start()

        anim =
            ObjectAnimator.ofFloat(mBinding?.playMyWorkAudioImgDisk, "rotation", 0f, 360f).apply {
                duration = 3000
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        mBinding?.playMyWorkAudioProgressTxt?.text = mMediaPlayer.currentPosition.formatMinSec()
        mBinding?.playMyWorkAudioDurationTxt?.text = mMediaPlayer.duration.formatMinSec()

        seekBarUpdater = Handler(Looper.getMainLooper())
        mBinding?.playMyWorkAudioSeekBar?.maxProgress = mMediaPlayer.duration.toFloat()
        seekBarUpdater?.post(object : Runnable {
            override fun run() {
                try {
                    mBinding?.playMyWorkAudioSeekBar?.progress =
                        mMediaPlayer.currentPosition.toFloat()
                    mBinding?.playMyWorkAudioProgressTxt?.text =
                        mMediaPlayer.currentPosition.formatMinSec()
                } catch (_: Exception) {
                }
                seekBarUpdater?.postDelayed(this, 50)
            }
        })

    }


    private fun resumeAudio() {
        mMediaPlayer.start()
        anim.resume()
        mBinding.playMyWorkAudioPlayBtn.setImageResource(R.drawable.ic_pause_mywork_audio)
    }

    private fun pauseAudio() {
        mMediaPlayer.pause()
        anim.pause()
        mBinding.playMyWorkAudioPlayBtn.setImageResource(R.drawable.ic_play_mywork_audio)
    }

}