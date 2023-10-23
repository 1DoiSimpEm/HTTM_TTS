package group.nhom14.textospeech.ui.main

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import group.nhom14.textospeech.model.AudioFile
import group.nhom14.textospeech.databinding.ItemAudioBinding
import kotlin.math.abs

class AudioAdapter(
    val itemClick: (AudioFile) -> Unit,
    val itemLongClick:(AudioFile) -> Unit
)
    : RecyclerView.Adapter<AudioAdapter.ViewHolder>() {
    private var files: MutableList<AudioFile> = mutableListOf()


    inner class ViewHolder(private val mBinding: ItemAudioBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun bind(audioFile: AudioFile) {
            mBinding.explorerItemName.text = audioFile.name
            mBinding.explorerItemAudioLength.text = humanReadableByteCountSI(getAudioDuration(audioFile.filePath).toLong())
            mBinding.myWorkAudioItemThreedots.setOnClickListener {
                itemLongClick(audioFile)
            }
            itemView.setOnClickListener {
                itemClick(audioFile)
            }
            itemView.setOnLongClickListener {
                itemLongClick(audioFile)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemAudioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }


    fun updateList(newList: List<AudioFile>) {
        files.clear()
        files.addAll(newList)
        notifyDataSetChanged()
    }
    //human readable app size
    private fun humanReadableByteCountSI(bytes: Long): String? {
        val s = if (bytes == Long.MIN_VALUE) Long.MIN_VALUE else abs(bytes)
        return if (s < 1000L) {
            String.format("%dB", bytes)
        } else if (s < 999_950L) {
            String.format("%.1fKB", bytes / 1e3)
        } else if (s < 999_950_000L) {
            String.format("%.1fMB", bytes / 1e6)
        } else {
            String.format("%.1fGB", bytes / 1e9)
        }
    }


    fun getAudioDuration(filePath: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr?.toInt() ?: 0
        retriever.release()
        return duration
    }
}