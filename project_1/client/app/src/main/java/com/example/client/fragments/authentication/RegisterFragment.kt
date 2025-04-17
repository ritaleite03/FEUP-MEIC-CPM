package com.example.client.fragments.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.actionRegistration
import com.example.client.fragments.feedback.ErrorFragment
import com.example.client.fragments.feedback.ProgressFragment
import com.example.client.generateEC
import com.example.client.generateRSA
import com.example.client.getPublicKey
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.PublicKey
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Log
import android.view.MotionEvent
import androidx.core.widget.addTextChangedListener

/**
 * Fragment responsible to deal with the register authentication of the user.
 */
class RegisterFragment : Fragment() {

    private var generated = false;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardDate = view.findViewById<TextInputEditText>(R.id.input_card_date)
        cardDate.setSelection(0)

        cardDate.addTextChangedListener(object: TextWatcher {

            var isEditing = false
            var isDeleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing || s == null) return

                isDeleting = s.split("").count { it == "/" } != 2
                isEditing = true

                val digits = s.replace(Regex("\\D"), "")
                val formatted = StringBuilder("__/__/____")

                var lastDigitPos = 0

                for (i in digits.indices) {
                    if (i < 8) {
                        val pos = if (i < 2) i else if (i < 4) i+1 else i+2

                        if (isDeleting) {
                            lastDigitPos = pos
                        }

                        formatted.setCharAt(pos, digits[i])
                    }
                }

                if (isDeleting) {
                    formatted.setCharAt(lastDigitPos, '_')
                    isDeleting = false
                }

                cardDate.setText(formatted)

                val nextPos = formatted.indexOf('_').takeIf { it != -1 } ?: formatted.length

                cardDate.setSelection(nextPos)

                isEditing = false
            }
        })

        var button = view.findViewById<Button>(R.id.button)

        button.setOnClickListener{
            lifecycleScope.launch {
                // show fragment showing the loading progress
                loadFragment(ProgressFragment())

                var publicEC : PublicKey? = null
                var publicRSA : PublicKey? = null

                // check user input
                var name = view.findViewById<TextInputEditText>(R.id.input_name).text.toString()
                var nick = view.findViewById<TextInputEditText>(R.id.input_nick).text.toString()
                var pass = view.findViewById<TextInputEditText>(R.id.input_pass).text.toString()

                var cardNumber = view.findViewById<TextInputEditText>(R.id.input_card_number).text.toString()
                var cardDate = view.findViewById<TextInputEditText>(R.id.input_card_date).text.toString()

                if (name == "") {
                    loadFragment(ErrorFragment.newInstance("Error - At least the input \"Name\" is missing. Please fill all the inputs!"))
                    return@launch
                }

                if (nick == "") {
                    loadFragment(ErrorFragment.newInstance("Error - At least the input \"Nickname\" is missing. Please fill all the inputs!"))
                    return@launch
                }

                if (pass == "") {
                    loadFragment(ErrorFragment.newInstance("Error - The input \"Password\" is missing. Please fill all the inputs!"))
                    return@launch
                }

                if (cardNumber == "") {
                    loadFragment(ErrorFragment.newInstance("Error - At least the card number is missing. Please fill all the inputs!"))
                    return@launch
                }

                if (cardDate == ""){
                    loadFragment(ErrorFragment.newInstance("Error - At least the card expiration date is missing. Please fill all the inputs!"))
                    return@launch
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                dateFormat.isLenient = false

                var cardDateValid : String? = null

                try {
                    var cardDateValidTemp = dateFormat.parse(cardDate)
                    cardDateValid = dateFormat.format(cardDateValidTemp)
                    Log.d("TEST",cardDateValid)
                }
                catch (_: Exception) {
                    loadFragment(ErrorFragment.newInstance("Error - The card expiration date is not valid. Please correct it!"))
                    return@launch
                }

                val radioGroup = view.findViewById<RadioGroup>(R.id.input_type_card)
                val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                var selectedCardType : String? = null

                if (selectedRadioButtonId != -1) {
                    val selectedRadioButton = view.findViewById<RadioButton>(selectedRadioButtonId)
                    selectedCardType = selectedRadioButton.text.toString()

                } else {
                    loadFragment(ErrorFragment.newInstance("Error - The card type is missing. Please fill all the inputs!"))
                    return@launch
                }

                // generate EC e RSA keys
                try {

                    val activity = requireActivity() as MainActivity

                    if (!generated) {
                        generateEC()
                        generateRSA()
                        generated = true
                    }

                    val entryEC = activity.fetchEntryEC()
                    val entryRSA = activity.fetchEntryRSA()

                    publicEC = getPublicKey(entryEC)
                    publicRSA = getPublicKey(entryRSA)

                    if(publicEC == null || publicRSA == null) {
                        throw Exception("Error")
                    }
                }
                catch(_: Exception) {
                    loadFragment(ErrorFragment.newInstance("Error - A problem occur when generating your keys. Try again!"))
                    return@launch
                }

                // perform registration on the server
                var result : String? = null
                try {
                    result = actionRegistration(publicEC, publicRSA, name, nick, cardNumber, cardDateValid, selectedCardType)
                    if(result.startsWith("Error")) {
                        loadFragment(ErrorFragment.newInstance(result))
                        return@launch
                    }
                }
                catch (_: Exception) {
                    loadFragment(ErrorFragment.newInstance("Error - The server was not available. Try again!"))
                    return@launch
                }

                // save credentials
                try {
                    var uuid: String? = JSONObject(result).optString("Uuid", null)
                    var key: String? = JSONObject(result).optString("key", null)

                    if (uuid != null && key != null) {

                        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                        sharedPreferences.edit {
                            putString("name", name)
                            putString("nick", nick)
                            putString("pass", pass)
                            putString("cardNumber", cardNumber)
                            putString("cardDate", cardDateValid)
                            putString("selectedCardType", selectedCardType)
                            putString("uuid", uuid)
                            putString("key", key)
                        }

                        // redirect to the next activity and close actual
                        withContext(Dispatchers.Main) {
                            val intent = Intent(activity, MainActivity2::class.java)
                            startActivity(intent)
                            activity?.finish()

                        }
                    }
                }
                catch (_: Exception){
                    loadFragment(ErrorFragment.newInstance("Error - A problem occur when saving the credentials. Try again!"))
                    return@launch
                }
            }
        }
    }

    /**
     * Replaces the current fragment in the container with the given fragment.
     * Uses childFragmentManager to manage inner fragments.
     *
     * @param fragment New fragment to be shown.
     */
    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.container_register, fragment)
            .commit()
    }
}