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
import com.example.client.generateAndStoreKeysEC
import com.example.client.generateAndStoreKeysRSA
import com.example.client.getPublicKeyEC
import com.example.client.getPublicKeyRSA
import com.example.client.register
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Fragment with the register task logic
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
                loadFragment(ProgressFragment())

                try {
                    // generate keys
                    generateAndStoreKeysEC()
                    generateAndStoreKeysRSA()

                    val activity = requireActivity() as MainActivity
                    val entryEC = activity.fetchEntryEC()
                    val entryRSA = activity.fetchEntryRSA()

                    // public keys
                    var publicEC = getPublicKeyEC(entryEC).toString()
                    var publicRSA = getPublicKeyRSA(entryRSA).toString()

                    val result = register(publicEC, publicRSA)

                    // check uuid
                    try {
                        var uuid: String? = JSONObject(result).optString("Uuid", null)
                        if (uuid != null) {

                            var name  = view.findViewById<TextInputEditText>(R.id.input_name).text.toString()
                            var nick  = view.findViewById<TextInputEditText>(R.id.input_nick).text.toString()
                            var pass  = view.findViewById<TextInputEditText>(R.id.input_pass).text.toString()

                            if(name != "" && nick != "" && pass != ""){

                                // save information
                                val sharedPreferences =  requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                                sharedPreferences.edit {
                                    putString("name", name)
                                    putString("nick", nick)
                                    putString("pass", pass)
                                    putString("uuid", uuid)
                                }

                                withContext(Dispatchers.Main) {
                                    val intent = Intent(activity, MainActivity2::class.java)
                                    startActivity(intent)
                                    activity.finish()
                                }
                            }
                        }
                    } catch (_: Exception) {
                        loadFragment(ErrorFragment())
                    }
                }
                catch (_: Exception){
                    loadFragment(ErrorFragment())
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.container_register, fragment)
            .commit()
    }
}