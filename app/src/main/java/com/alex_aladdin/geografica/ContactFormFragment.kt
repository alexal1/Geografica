package com.alex_aladdin.geografica

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.alex_aladdin.geografica.di.inject
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.Companion.PREFS_CONTACT_FORM_WAS_SENT
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.fragment_contact_from.*
import java.io.DataOutputStream
import java.lang.RuntimeException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class ContactFormFragment : Fragment() {

    var onCloseClick: Runnable? = null

    private val analytics: Analytics by inject()
    private val sharedPrefsHelper: SharedPreferencesHelper by inject()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val textWatcherFormatter = PhoneNumberFormattingTextWatcher()
    private val textWatcherValidator = PhoneNumberValidatingTextWatcher()
    private val phoneUtil = PhoneNumberUtil.getInstance()

    private var currentState = State.INPUT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytics.showContactForm()
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_from, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crossImageView.setOnClickListener {
            analytics.skipContactForm()
            onCloseClick?.run()
        }

        phoneEditText.addTextChangedListener(textWatcherFormatter)
        phoneEditText.addTextChangedListener(textWatcherValidator)

        applyState(currentState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        phoneEditText.removeTextChangedListener(textWatcherFormatter)
        phoneEditText.removeTextChangedListener(textWatcherValidator)
    }

    private fun sendNumber(number: String) {
        currentState = State.SENDING
        applyState(currentState)

        thread {
            try {
                val url = URL("https://formspree.io/xeqrqydl")
                val connection = url.openConnection() as HttpsURLConnection
                connection.apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val params = mapOf("number" to number)
                val out = DataOutputStream(connection.outputStream)
                out.writeBytes(getParamsString(params))
                out.flush()
                out.close()

                val status = connection.responseCode
                if (status == 200) {
                    mainHandler.post {
                        setRequestResult(true)
                    }
                } else {
                    throw RuntimeException("Response code $status")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sending number failed", e)
                mainHandler.post {
                    setRequestResult(false, e)
                }
            }
        }
    }

    private fun setRequestResult(succeed: Boolean, exception: Exception? = null) {
        if (succeed) {
            currentState = State.THANKS
            applyState(currentState)
            sharedPrefsHelper.setBool(PREFS_CONTACT_FORM_WAS_SENT, true)
            analytics.sendContactForm()
        } else {
            currentState = State.INPUT
            applyState(currentState)
            exception?.message?.let { failReason ->
                Toast.makeText(requireContext(), failReason, Toast.LENGTH_LONG).show()
            }
            analytics.contactFormError(exception)
        }
    }

    private inner class PhoneNumberValidatingTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            sendButton.isEnabled = try {
                val phoneNumber = phoneUtil.parse(s, "RU")
                phoneUtil.isValidNumberForRegion(phoneNumber, "RU")
            } catch (e: NumberParseException) {
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    private fun getParamsString(params: Map<String, String>): String {
        val result = StringBuilder()
        for ((key, value) in params) {
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))
            result.append("&")
        }
        val resultString = result.toString()
        return if (resultString.isNotEmpty()) {
            resultString.substring(0, resultString.length - 1)
        } else {
            resultString
        }
    }

    private fun applyState(state: State) {
        when (state) {
            State.INPUT -> {
                sendButton.setText(R.string.contact_form_send)
                sendButton.isEnabled = false
                sendButton.setOnClickListener {
                    val number = phoneEditText.text.toString()
                    sendNumber(number)
                }
                progressBar.isVisible = false
                thanksView.isVisible = false
                titleView.isVisible = true
                textView.isVisible = true
                phoneEditText.isVisible = true
                phoneEditText.isEnabled = true
            }

            State.SENDING -> {
                sendButton.text = ""
                sendButton.isEnabled = false
                progressBar.isVisible = true
                thanksView.isVisible = false
                titleView.isVisible = true
                textView.isVisible = true
                phoneEditText.isVisible = true
                phoneEditText.isEnabled = false
            }

            State.THANKS -> {
                sendButton.setText(R.string.contact_form_close)
                sendButton.isEnabled = true
                sendButton.setOnClickListener {
                    onCloseClick?.run()
                }
                progressBar.isVisible = false
                thanksView.isVisible = true
                titleView.isVisible = false
                textView.isVisible = false
                phoneEditText.isVisible = false
            }
        }
    }

    enum class State { INPUT, SENDING, THANKS }

    companion object {
        const val TAG = "GeoContactForm"

        fun create(): ContactFormFragment = ContactFormFragment()
    }
}