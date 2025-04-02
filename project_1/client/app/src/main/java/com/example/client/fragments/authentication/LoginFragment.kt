package com.example.client.fragments.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.fragments.feedback.ErrorFragment
import com.example.client.fragments.feedback.ProgressFragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var button  = view.findViewById<Button>(R.id.login_button)

        button.setOnClickListener{
            loadFragment(ProgressFragment())

            var name = view.findViewById<TextInputEditText>(R.id.input_name).text.toString()
            var nick = view.findViewById<TextInputEditText>(R.id.input_nick).text.toString()
            var pass = view.findViewById<TextInputEditText>(R.id.input_pass).text.toString()

            val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences",
                Context.MODE_PRIVATE
            )
            val realName = sharedPreferences.getString("name", null)
            val realNick = sharedPreferences.getString("nick", null)
            val realPass = sharedPreferences.getString("pass", null)

            if(name == realName && nick == realNick && pass == realPass) {
                val activity = requireActivity() as MainActivity
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(activity, MainActivity2::class.java)
                        startActivity(intent)
                        activity.finish()
                    }
                }
            }
            else {
                loadFragment(ErrorFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.container_login, fragment)
            .commit()
    }

}