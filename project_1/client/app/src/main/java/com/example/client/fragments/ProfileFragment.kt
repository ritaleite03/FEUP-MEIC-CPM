package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.client.R
import com.example.client.dialog.ChangeTextDialogFragment

class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val name: String? = sharedPreferences.getString("name", null)
        val nick: String? = sharedPreferences.getString("nick", null)

        val textName = view.findViewById<TextView>(R.id.textName)
        val textNick = view.findViewById<TextView>(R.id.textNick)
        val editNameButton = view.findViewById<ImageButton>(R.id.editNameButton)
        val editNickButton = view.findViewById<ImageButton>(R.id.editNickButton)

        textName.text = name
        textNick.text = nick

        editNameButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("name")
            dialog.onTextChanged = { newText ->
                textName.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }

        editNickButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("nick")
            dialog.onTextChanged = { newText ->
                textNick.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }
    }
}