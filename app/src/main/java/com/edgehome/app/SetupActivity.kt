package com.edgehome.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * First-run screen: asks for the hub's gateway address and stores it.
 * Also reachable later from the error page and the launcher shortcut.
 */
class SetupActivity : ComponentActivity() {

    companion object {
        const val EXTRA_EDIT = "edit"
    }

    private lateinit var input: EditText
    private lateinit var error: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        input = findViewById(R.id.gateway_input)
        error = findViewById(R.id.gateway_error)
        val connect = findViewById<Button>(R.id.connect)

        if (intent.getBooleanExtra(EXTRA_EDIT, false) || Gateway.saved(this) != null) {
            findViewById<TextView>(R.id.setup_title).setText(R.string.setup_title_edit)
        }

        if (savedInstanceState == null) {
            input.setText(Gateway.saved(this) ?: BuildConfig.DEFAULT_GATEWAY_URL)
            input.setSelection(input.text.length)
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (error.visibility == View.VISIBLE) {
                    error.visibility = View.GONE
                    input.isActivated = false
                }
            }
        })
        input.setOnEditorActionListener { _, actionId, event ->
            val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_GO || enter) {
                submit(); true
            } else false
        }
        connect.setOnClickListener { submit() }
    }

    private fun submit() {
        val url = Gateway.normalize(input.text.toString())
        if (url == null) {
            error.visibility = View.VISIBLE
            input.isActivated = true
            input.requestFocus()
            return
        }
        Gateway.save(this, url)
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
