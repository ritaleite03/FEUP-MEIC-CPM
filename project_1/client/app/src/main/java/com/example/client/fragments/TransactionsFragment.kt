package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity2
import com.example.client.R
import com.example.client.logic.actionChallengeTransactions
import com.example.client.logic.actionGetTransactions
import com.example.client.dialog.ErrorDialogFragment
import com.example.client.logic.Transaction
import com.example.client.logic.TransactionAdapter
import com.example.client.logic.getPrivateKey
import com.example.client.logic.listTransactions
import com.example.client.utils.Crypto
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.Cipher

class TransactionsFragment: Fragment() {

    private lateinit var transactionsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        transactionsListView = view.findViewById<ListView>(R.id.lv_transaction)

        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("uuid", null)

        lifecycleScope.launch {
            var nonce: UUID?

            try {
                val result = JSONObject(actionChallengeTransactions(uuid.toString()))
                nonce = UUID.fromString(result.getString("nonce").toString())
            } catch (_: Exception) {
                if (!isAdded) return@launch
                ErrorDialogFragment.Companion.newInstance("The server was not available. Try again!").show(parentFragmentManager, "error_popup")
                return@launch
            }

            val activity = requireActivity() as MainActivity2
            val entry = activity.fetchEntryRSA()

            val buffer = ByteBuffer.allocate(16).apply {
                putLong(nonce.mostSignificantBits)
                putLong(nonce.leastSignificantBits)
            }

            val encrypted = Cipher.getInstance(Crypto.CRYPTO_RSA_ENC_ALGO).run {
                init(Cipher.ENCRYPT_MODE, getPrivateKey(entry))
                doFinal(buffer.array())
            }

            var transactions: JSONArray? = null
            try {
                val result = JSONObject(actionGetTransactions(uuid.toString(), encrypted))
                Log.d("result", result.toString())
                transactions = result.getJSONArray("transactions")
                Log.d("transactions", transactions.toString())
            } catch (_: Exception) {
                if (!isAdded) return@launch
                ErrorDialogFragment.Companion.newInstance("The server was not available. Try again!").show(parentFragmentManager, "error_popup")
                return@launch
            }

            for (i in 0 until transactions.length()) {
                val transactionObject = transactions.getJSONObject(i)
                val transactionUuid = UUID.fromString(transactionObject.getString("uuid"))
                val transactionPrice = transactionObject.getString("price").toDouble()
                val transactionDate = transactionObject.getString("date")
                listTransactions.add(
                    Transaction(
                        transactionUuid,
                        transactionPrice,
                        transactionDate
                    )
                )
            }

            transactionsListView.run {
                adapter = TransactionAdapter(requireContext(), listTransactions) {}
            }
            (transactionsListView.adapter as TransactionAdapter).notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listTransactions.clear()
    }
}