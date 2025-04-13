package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.actionChallengeVouchers
import com.example.client.actionGetVouchers
import com.example.client.getPrivateKey
import com.example.client.utils.Crypto.CRYPTO_RSA_ENC_ALGO
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.Cipher

class VouchersFragment : Fragment() {


    private lateinit var vouchersListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vouchers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vouchersListView = view.findViewById<ListView>(R.id.lv_voucher)

        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences",
            Context.MODE_PRIVATE
        )
        val uuid = sharedPreferences.getString("uuid", null)

        lifecycleScope.launch {

            val result1 = JSONObject(actionChallengeVouchers(uuid.toString()))
            val nonce = UUID.fromString(result1.getString("nonce").toString())

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

            val result2 = JSONObject(actionGetVouchers(uuid.toString(), encrypted))
            val vouchers = result2.getJSONArray("vouchers")

            for(i in 0 until vouchers.length()) {
                val voucherObject = vouchers.getJSONObject(i)
                val voucherUuid = UUID.fromString(voucherObject.getString("uuid"))
                val voucherValue = 15
                listVouchers.add(Voucher(voucherUuid, voucherValue))
            }
            vouchersListView.run {
                adapter = VoucherAdapter(requireContext(), listVouchers) {
                }
            }
            (vouchersListView.adapter as VoucherAdapter).notifyDataSetChanged()
        }
    }
}