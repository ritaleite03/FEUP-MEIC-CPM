package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.actionChallengeVouchers
import com.example.client.actionGetVouchers
import com.example.client.data.VouchersDB
import com.example.client.domain.Voucher
import com.example.client.domain.VoucherAdapter
import com.example.client.domain.listVouchers
import com.example.client.domain.vouchersDB
import com.example.client.fragments.feedback.ErrorFragment
import com.example.client.getPrivateKey
import com.example.client.utils.Crypto.CRYPTO_RSA_ENC_ALGO
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.Cipher

/**
 * Fragment used to show the user's vouchers.
 */
class VouchersFragment : Fragment() {

    private lateinit var vouchersListView: ListView
    private lateinit var empty: TextView
    private lateinit var buttonUpdate : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vouchers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vouchersListView = view.findViewById<ListView>(R.id.lv_voucher)
        empty = view.findViewById(R.id.empty2)
        buttonUpdate = view.findViewById<Button>(R.id.bottom_button_update)
        vouchersDB = VouchersDB(requireActivity().applicationContext)

        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences",
            Context.MODE_PRIVATE
        )
        val uuid = sharedPreferences.getString("uuid", null)

        buttonUpdate.setOnClickListener {
            if (uuid != null) {
                updateVouchers(uuid)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vouchersDB.getVouchers()
        vouchersListView.run {
            emptyView = empty
            adapter = VoucherAdapter(requireContext(), listVouchers) {
            }
        }
        registerForContextMenu(vouchersListView)
    }

    private fun updateVouchers(uuid : String){
        lifecycleScope.launch {

            var nonce: UUID? = null

            // get nonce challenge
            try {
                val result = JSONObject(actionChallengeVouchers(uuid.toString()))
                nonce = UUID.fromString(result.getString("nonce").toString())
            } catch (_: Exception) {
                if (!isAdded) return@launch
                ErrorFragment.newInstance("The server was not available. Try again!").show(parentFragmentManager, "error_popup")
                return@launch
            }

            val activity = requireActivity() as MainActivity2
            val entry = activity.fetchEntryRSA()

            val buffer = ByteBuffer.allocate(16).apply {
                putLong(nonce.mostSignificantBits)
                putLong(nonce.leastSignificantBits)
            }
            val encrypted = Cipher.getInstance(CRYPTO_RSA_ENC_ALGO).run {
                init(Cipher.ENCRYPT_MODE, getPrivateKey(entry))
                doFinal(buffer.array())
            }

            var vouchers: JSONArray? = null
            try {
                val result = JSONObject(actionGetVouchers(uuid.toString(), encrypted))
                vouchers = result.getJSONArray("vouchers")

            } catch (_: Exception) {
                if (!isAdded) return@launch
                ErrorFragment.newInstance("Error - The server was not available. Try again!").show(parentFragmentManager, "error_popup")
                return@launch
            }

            listVouchers.clear()
            vouchersDB.deleteAll()

            for (i in 0 until vouchers.length()) {
                val voucherObject = vouchers.getJSONObject(i)
                val voucherUuid = UUID.fromString(voucherObject.getString("uuid"))
                val voucherValue = 15
                val voucher = Voucher(voucherUuid, voucherValue)
                vouchersDB.insert(voucher)
                listVouchers.add(voucher)
            }
            vouchersListView.run {
                adapter = VoucherAdapter(requireContext(), listVouchers) {
                }
            }
            (vouchersListView.adapter as VoucherAdapter).notifyDataSetChanged()
        }
    }
}