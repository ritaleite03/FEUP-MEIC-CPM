package com.example.client.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.client.R
import com.example.client.logic.userDB

class ChangeTextDialogFragment : DialogFragment() {

    private var param1: String? = null

    var onTextChanged: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_change_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var changeTextParameter = view.findViewById<TextView>(R.id.changeTextParameter)
        var changeTextInput = view.findViewById<EditText>(R.id.changeTextInput)
        var changeTextButton = view.findViewById<Button>(R.id.changeTextButton)

        val parameter: String? = userDB.getColumnValue(param1.toString())
        if (param1 != null) {
            changeTextParameter.text = param1!!.uppercase()
            changeTextInput.setText(parameter)
        }

        changeTextButton.setOnClickListener {
            val newValue = changeTextInput.text.toString()
            onTextChanged?.invoke(newValue)
            dismiss()
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"

        fun newInstance(param1: String): ChangeTextDialogFragment {
            val fragment = ChangeTextDialogFragment()
            val args = Bundle().apply {
                putString(ARG_PARAM1, param1)
            }
            fragment.arguments = args
            return fragment
        }
    }
}