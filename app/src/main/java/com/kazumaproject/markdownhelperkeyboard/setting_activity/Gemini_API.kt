package com.kazumaproject.markdownhelperkeyboard.setting_activity

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



@RequiresApi(Build.VERSION_CODES.S)
@AndroidEntryPoint

class Gemini_API: InputMethodService() {
    fun btnCurrentClick(v: View) {
        CoroutineScope(Dispatchers.IO).launch {
            textGenTextOnlyPrompt() // suspend関数を呼び出す
            delay(5000)
            // suspend関数の結果を処理する
            withContext(Dispatchers.Main) {
                // UIスレッドで結果を表示
                //textView.text = "結果:" + response.text
                Toast.makeText(this@Gemini_API, "処理が完了しました", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //Gemini APIにリクエストを送信
    suspend fun textGenTextOnlyPrompt() {
        // [START text_gen_text_only_prompt]
        try {
            val generativeModel =
                GenerativeModel(
                    // Specify a Gemini model appropriate for your use case
                    modelName = "gemini-1.5-flash",
                    // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                    //以下API Keyを直接貼り付ける
                    apiKey = "AIzaSyDAXlRJ8fNjN2dufhssDh8_WCb4Tmoyjcs")
            //Gemini APIに質問を投げる。
            //↓不適切判断を行う(Gemini API 1.5だといい感じに返してくれた)
            // val prompt = "　この文章が不適切(卑猥、暴言、コンプライアンス)なら1、問題ないなら0と数字のみで答えてください。解説はいりません。"
            val prompt = "Write a story about a magic backpack."
            val response = generativeModel.generateContent(prompt)
            print(response.text)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@Gemini_API,
                    response.text,
                    Toast.LENGTH_SHORT
                ).show()
            }
            // [END text_gen_text_only_prompt]
        } catch (e: Exception) {
        // エラー処理
        Log.e("GeminiAPI", "Error: ${e.message}")
        // 例えば、エラーメッセージをダイアログで表示する
        // showErrorDialog(e.message)
        }
    }
}