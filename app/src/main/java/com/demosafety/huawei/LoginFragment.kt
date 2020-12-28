package com.demosafety.huawei

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    var username: String = "defValue"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        huaweiIdLogin.setOnClickListener {
            val user = AGConnectAuth.getInstance().currentUser
            val mHuaweiIdAuthParams =
                HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAccessToken()
                    .createParams()
            val mHuaweiIdAuthService =
                HuaweiIdAuthManager.getService(context, mHuaweiIdAuthParams)
            startActivityForResult(mHuaweiIdAuthService.signInIntent, 1001)

            val client = SafetyDetect.getClient(context)
            val appId = "103476189"
            client.userDetection(appId)
                .addOnSuccessListener { userDetectResponse ->
                    val responseToken = userDetectResponse.responseToken
                    if (responseToken.isNotEmpty()) {
                        val bundle = bundleOf("username" to username)
                        view?.findNavController()
                            ?.navigate(R.id.action_loginFragment_to_mainPageFragment22, bundle)
                    }
                }
                .addOnFailureListener {
                    val errorMsg: String? = if (it is ApiException) {
                        (SafetyDetectStatusCodes.getStatusCodeString(it.statusCode) + ": "
                                + it.message)
                    } else {
                        it.message
                    }
                    Log.i(TAG, "User detection fail. Error info: $errorMsg")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiAccount = authHuaweiIdTask.result
                Log.i(TAG, "accessToken:" + huaweiAccount.accessToken)
                val credential = HwIdAuthProvider.credentialWithToken(huaweiAccount!!.accessToken)
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
                    // onSuccess
                    username = it.user.displayName.toString()
                }.addOnFailureListener {
                    // onFail
                }
            }
        }
    }

    private fun initUserDetect() {
        val client = SafetyDetect.getClient(context)
        client.initUserDetect().addOnSuccessListener {
        }.addOnFailureListener {
        }
    }
}