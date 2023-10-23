package group.nhom14.textospeech.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import group.nhom14.textospeech.R
import group.nhom14.textospeech.databinding.FragmentRenameDialogBinding


class RenameDialogFragment : DialogFragment() {
    private lateinit var mBinding: FragmentRenameDialogBinding
    private var name  =""
    private var onRenameListener: ((String) -> Unit?)? = null

    companion object {
        fun newInstance(name : String, onRenameListener:(String) ->Unit): RenameDialogFragment {
            val args = Bundle()
            val fragment = RenameDialogFragment()
            fragment.arguments = args
            fragment.name   =name
            fragment.onRenameListener = onRenameListener
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRenameDialogBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActions()
    }

    private fun initActions() {
        mBinding.permDialogDeny.setOnClickListener {
            dismiss()
        }
        mBinding.permDialogAllow.setOnClickListener {
            onRenameListener?.invoke(mBinding.dialogRenameEditTxt.text.toString())
            dismiss()
        }
    }

}