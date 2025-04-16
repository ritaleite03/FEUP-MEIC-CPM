package com.example.client.fragments.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

/**
 * Fragment responsible to deal with the register authentication of the user.
 */
class RegisterFragment : Fragment() {

    private var generated = false;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var button  = view.findViewById<Button>(R.id.button)

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
                    result = actionRegistration(publicEC, publicRSA)
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