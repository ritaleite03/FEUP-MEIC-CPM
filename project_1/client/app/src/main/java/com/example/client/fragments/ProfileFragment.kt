package com.example.client.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.dialog.ChangePassDialogFragment
import com.example.client.dialog.ChangeRadioDialogFragment
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
        (this.requireActivity() as MainActivity2).toolbar.title = "Profile"

        val textName = view.findViewById<TextView>(R.id.textName)
        val textNick = view.findViewById<TextView>(R.id.textNick)
        val textPass = view.findViewById<TextView>(R.id.textPass)
        val textType = view.findViewById<TextView>(R.id.textType)
        val textNumber = view.findViewById<TextView>(R.id.textNumber)
        val textDate = view.findViewById<TextView>(R.id.textDate)

        val editNameButton = view.findViewById<ImageButton>(R.id.editNameButton)
        val editNickButton = view.findViewById<ImageButton>(R.id.editNickButton)
        val editPassButton = view.findViewById<ImageButton>(R.id.editPassButton)
        val editTypeButton = view.findViewById<ImageButton>(R.id.editTypeButton)
        val editNumberButton = view.findViewById<ImageButton>(R.id.editNumberButton)
        val editDateButton = view.findViewById<ImageButton>(R.id.editDateButton)

        textName.text = userDB.getColumnValue("Name")
        textNick.text = userDB.getColumnValue("Nick")
        textPass.text = userDB.getColumnValue("Pass")
        textType.text = userDB.getColumnValue("SelectedCardType")
        textNumber.text = userDB.getColumnValue("CardNumber")
        textDate.text = userDB.getColumnValue("CardDate")

        editNameButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("Name", "text")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("Name", newText)
                textName.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }

        editNickButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("Nick", "text")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("Nick", newText)
                textNick.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }

        editPassButton.setOnClickListener {
            var dialog = ChangePassDialogFragment()
            dialog.onPassChanged = { newPass ->
                userDB.updateColumn("Pass", newPass)
                textPass.text = newPass
            }
            dialog.show(parentFragmentManager, "change_pass_dialog")
        }

        editTypeButton.setOnClickListener {
            var dialog = ChangeRadioDialogFragment()
            dialog.onRadioChanged = { newText ->
                userDB.updateColumn("SelectedCardType", newText)
                textType.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }


        editNumberButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("CardNumber", "number")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("CardNumber", newText)
                textNumber.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }

        editDateButton.setOnClickListener {
            var dialog = ChangeTextDialogFragment.newInstance("CardDate", "date")
            dialog.onTextChanged = { newText ->
                userDB.updateColumn("CardDate", newText)
                textDate.text = newText
            }
            dialog.show(parentFragmentManager, "change_text_dialog")
        }
    }
}