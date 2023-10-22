package group.nhom14.textospeech

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import group.nhom14.textospeech.databinding.ItemAudioBinding
import java.io.File

class Adapter(val itemClick: (AudioFile) -> Unit) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    private var files: MutableList<AudioFile> = mutableListOf()


    inner class ViewHolder(private val mBinding: ItemAudioBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun bind(audioFile: AudioFile) {
            mBinding.explorerItemName.text = audioFile.name
//            mBinding.explorerItemAudioLength.text = getAudioDuration(audioFile.filePath).toString()
            itemView.setOnClickListener {
                itemClick(audioFile)
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

//    fun getAudioDuration(filePath: String): Int {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(filePath)
//        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        val duration = durationStr?.toInt() ?: 0
//        retriever.release()
//        return duration
//    }
}