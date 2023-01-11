package com.example.kakaologin5

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.kakaologin5.databinding.ActivityMainBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import okhttp3.Response
import javax.security.auth.callback.Callback

//코드 이동시 LoginActivity 리네임 플리즈
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그인 정보 확인
        UserApiClient.instance.accessTokenInfo{ tokenInfo, error ->
            if(error != null){
                Toast.makeText(this, "토큰 정보 보기 실패", Toast.LENGTH_SHORT).show()
            }
            else if(tokenInfo != null) {
                Toast.makeText(this, "토큰 정보 보기 성공", Toast.LENGTH_SHORT).show()
            }
        }
        /* Click_listener */
        binding.btnLogin.setOnClickListener {
            kakaoLogin() //로그인
        }
        UserApiClient.instance.me{user, error ->
            binding.tvNickname.text = "닉네임: ${user?.kakaoAccount?.profile?.nickname}"
            binding.tvId.text = "닉네임: ${user?.kakaoAccount?.email}"
        }
        binding.btnLogout.setOnClickListener {
            kakaoLogout() //로그아웃
        }
    }
    private fun kakaoLogin() {

        // 카카오톡으로 로그인
        UserApiClient.instance.loginWithKakaoTalk(this@MainActivity) { token, error ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
            }
            else if (token != null) {
                Log.i(TAG, "로그인 성공 ${token.accessToken}")
            }
        }
        
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@MainActivity)) {
            UserApiClient.instance.loginWithKakaoTalk(this@MainActivity) { token, error ->

                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    //binding.tvId.text = ${id.}
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
        }
    }
    private fun kakaoLogout(){
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Toast.makeText(this, "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
            }
        }
    }
}