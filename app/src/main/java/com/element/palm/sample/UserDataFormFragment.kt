package com.element.palm.sample

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.element.camera.ProviderUtil
import com.element.camera.UserInfo
import java.util.*

class UserDataFormFragment : DialogFragment() {
    private var mainActivity: MainActivity? = null
    private var handler: Handler? = null
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var enroll: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_user_data_form, container, false)
        firstName = rootView.findViewById(R.id.firstName)
        lastName = rootView.findViewById(R.id.lastName)
        val onEditorActionListener = OnEditorActionListener { view, actionId, event ->
            if (view === firstName) {
                lastName.requestFocus()
            } else if (view === lastName) {
                onClickEnroll()
            }
            true
        }
        firstName.setImeActionLabel(getString(R.string.signUp), EditorInfo.IME_ACTION_DONE)
        firstName.setOnEditorActionListener(onEditorActionListener)
        lastName.setImeActionLabel(getString(R.string.signUp), EditorInfo.IME_ACTION_DONE)
        lastName.setOnEditorActionListener(onEditorActionListener)
        enroll = rootView.findViewById(R.id.enroll)
        enroll.setOnClickListener(View.OnClickListener { onClickEnroll() })
        return rootView
    }

    fun setMainActivity(mainActivity: MainActivity?) {
        this.mainActivity = mainActivity
        handler = Handler()
    }

    @Synchronized
    private fun onClickEnroll() {
        enroll!!.isEnabled = false
        handler!!.postDelayed({ enroll!!.isEnabled = true }, 500)
        val fStr = firstName!!.text.toString()
        val lStr = lastName!!.text.toString()
        if (TextUtils.isEmpty(fStr)) {
            Toast.makeText(mainActivity, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
            return
        }
        ProviderUtil.deleteAllUsers(mainActivity!!.baseContext, BuildConfig.APPLICATION_ID)
        val userInfo = UserInfo.enrollNewUser(
                mainActivity!!.baseContext,
                BuildConfig.APPLICATION_ID,
                fStr,
                lStr,
                HashMap())
        mainActivity!!.startEnroll(userInfo.userId)
        dismiss()
    }
}