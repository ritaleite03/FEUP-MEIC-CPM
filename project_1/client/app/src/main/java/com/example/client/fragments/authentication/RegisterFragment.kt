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
import com.example.client.fragments.feedback.ErrorFragment
import com.example.client.fragments.feedback.ProgressFragment
import com.example.client.generateEC
import com.example.client.generateRSA
import com.example.client.getPublicKey
import com.example.client.register
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Fragment responsible to deal with the register authentication of the user.
 */
class RegisterFragment : Fragment() {

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

                try {
                    // generate EC e RSA keys for save communication
                    generateEC()
                    generateRSA()

                    val activity = requireActivity() as MainActivity
                    val entryEC = activity.fetchEntryEC()
                    val entryRSA = activity.fetchEntryRSA()

                    // get public keys for EC and RSA
                    var publicEC = getPublicKey(entryEC)
                    var publicRSA = getPublicKey(entryRSA)

                    // register on the server
                    val result = register(publicEC, publicRSA)

                    // attempts to extract the UUID and key from the server response
                    try {
                        var uuid: String? = JSONObject(result).optString("Uuid", null)
                        var key: String? = JSONObject(result).optString("key", null)

                        // if UUID and key are valid
                        if (uuid != null && key != null) {

                            var name  = view.findViewById<TextInputEditText>(R.id.input_name).text.toString()
                            var nick  = view.findViewById<TextInputEditText>(R.id.input_nick).text.toString()
                            var pass  = view.findViewById<TextInputEditText>(R.id.input_pass).text.toString()

                            // checks if all required fields have been filled in
                            if(name != "" && nick != "" && pass != ""){

                                // saves user information to SharedPreferences
                                val sharedPreferences =  requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
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
                                    activity.finish()
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // if there is an error processing the response, it displays an error fragment
                        loadFragment(ErrorFragment())
                    }
                }
                catch (_: Exception){
                    // if there is an error during registration, it displays an error fragment
                    loadFragment(ErrorFragment())
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