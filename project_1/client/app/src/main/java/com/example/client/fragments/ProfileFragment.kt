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
import com.example.client.logic.userDB

class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textName = view.findViewById<TextView>(R.id.textName)
        val textNick = view.findViewById<TextView>(R.id.textNick)
        val editNameButton = view.findViewById<ImageButton>(R.id.editNameButton)
        val editNickButton = view.findViewById<ImageButton>(R.id.editNickButton)

        textName.text = userDB.getColumnValue("Name")
        textNick.text = userDB.getColumnValue("Nick")

        editNameButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("Name")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("Name", newText)
                textName.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }

        editNickButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("Nick")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("Nick", newText)
                textNick.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }
    }
}