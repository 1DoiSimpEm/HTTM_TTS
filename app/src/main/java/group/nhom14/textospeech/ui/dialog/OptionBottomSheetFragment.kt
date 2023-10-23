package group.nhom14.textospeech.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import group.nhom14.textospeech.R
import group.nhom14.textospeech.databinding.FragmentOptionBottomSheetBinding


class OptionBottomSheetFragment(
    private val listener: OptionBottomSheetListener
) : BottomSheetDialogFragment() {
    private lateinit var mBinding: FragmentOptionBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setOnShowListener {
            val frameLayout: FrameLayout? =
                dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            frameLayout?.setBackgroundResource(android.R.color.transparent)
        }
        mBinding = FragmentOptionBottomSheetBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActions()
    }

    private fun initActions() {
        mBinding.bottomSheetCancel.setOnClickListener {
            dismiss()
        }
        mBinding.bottomSheetRename.setOnClickListener {
            listener.rename()
            dismiss()
        }
        mBinding.bottomSheetShare.setOnClickListener {
            listener.share()
            dismiss()
        }
        mBinding.bottomSheetSetAsRingTone.setOnClickListener {
            listener.setRingtone()
            dismiss()
        }
        mBinding.bottomSheetDelete.setOnClickListener {
            listener.delete()
            dismiss()
        }
    }


    interface OptionBottomSheetListener {
        fun rename()
        fun share()
        fun setRingtone()
        fun delete()
    }

}