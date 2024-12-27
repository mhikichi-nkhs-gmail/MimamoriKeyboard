package com.kazumaproject.markdownhelperkeyboard.ime_service

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.*
import android.view.textservice.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.daasuu.bl.BubbleLayout
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.flexbox.*
import com.google.android.material.textview.MaterialTextView
import com.kazumaproject.markdownhelperkeyboard.R
import com.kazumaproject.markdownhelperkeyboard.databinding.MainLayoutBinding
import com.kazumaproject.markdownhelperkeyboard.ime_service.IMEService.Companion
import com.kazumaproject.markdownhelperkeyboard.ime_service.IMEService.Companion.DISPLAY_LEFT_STRING_TIME
import com.kazumaproject.markdownhelperkeyboard.ime_service.adapters.EmojiKigouAdapter
import com.kazumaproject.markdownhelperkeyboard.ime_service.adapters.KigouAdapter
import com.kazumaproject.markdownhelperkeyboard.ime_service.adapters.SuggestionAdapter
import com.kazumaproject.markdownhelperkeyboard.ime_service.components.InputModeSwitch
import com.kazumaproject.markdownhelperkeyboard.ime_service.components.TenKeyInfo
import com.kazumaproject.markdownhelperkeyboard.ime_service.components.TenKeyMapHolder
import com.kazumaproject.markdownhelperkeyboard.ime_service.di.*
import com.kazumaproject.markdownhelperkeyboard.ime_service.extensions.*
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_ACTIVITY
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_LIST_ANIMALS_NATURE
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_LIST_FOOD_DRINK
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_LIST_SMILEYS_PEOPLE
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_OBJECT
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.EMOJI_TRAVEL
import com.kazumaproject.markdownhelperkeyboard.ime_service.other.Constants.KAOMOJI
import com.kazumaproject.markdownhelperkeyboard.ime_service.state.*
import dagger.hilt.android.AndroidEntryPoint
import jp.co.omronsoft.openwnn.ComposingText
import jp.co.omronsoft.openwnn.JAJP.OpenWnnEngineJAJP
import jp.co.omronsoft.openwnn.StrSegment
import jp.co.omronsoft.openwnn.WnnWord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.S)
@AndroidEntryPoint

class Gemini_API: InputMethodService() {
    @Inject
    @Named("main_ime_scope")
    lateinit var scope : CoroutineScope
    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher
    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher
    @Inject
    @InputBackGroundDispatcher
    lateinit var imeIoDispatcher: CoroutineDispatcher
    @Inject
    lateinit var openWnnEngineJAJP: OpenWnnEngineJAJP
    @Inject
    lateinit var composingText: ComposingText
    @Inject
    lateinit var tenKeyMap: TenKeyMapHolder
    @Inject
    lateinit var inputMethodManager: InputMethodManager
    @Inject
    @DrawableReturn
    lateinit var drawableReturn: Drawable
    @Inject
    @DrawableKanaSmall
    lateinit var drawableKanaSmall: Drawable
    @Inject
    @DrawableHenkan
    lateinit var drawableHenkan: Drawable
    @Inject
    @DrawableEnglishSmall
    lateinit var drawableEnglishSmall: Drawable
    @Inject
    @DrawableSpaceBar
    lateinit var drawableSpaceBar: Drawable
    @Inject
    @DrawableRightArrow
    lateinit var drawableRightArrow: Drawable
    @Inject
    @DrawableLanguage
    lateinit var drawableLanguage: Drawable
    @Inject
    @DrawableNumberSmall
    lateinit var drawableNumberSmall : Drawable
    @Inject
    @DrawableOpenBracket
    lateinit var drawableOpenBracket : Drawable
    @Inject
    @PopUpTextActive
    lateinit var mPopupWindowActive: PopupWindow
    private lateinit var bubbleViewActive: BubbleLayout
    private lateinit var popTextActive: MaterialTextView
    @Inject
    @PopUpWindowTop
    lateinit var mPopupWindowTop: PopupWindow
    private lateinit var bubbleViewTop: BubbleLayout
    private lateinit var popTextTop: MaterialTextView
    @Inject
    @PopUpWindowLeft
    lateinit var mPopupWindowLeft: PopupWindow
    private lateinit var bubbleViewLeft: BubbleLayout
    private lateinit var popTextLeft: MaterialTextView
    @Inject
    @PopUpWindowBottom
    lateinit var mPopupWindowBottom: PopupWindow
    private lateinit var bubbleViewBottom: BubbleLayout
    private lateinit var popTextBottom: MaterialTextView
    @Inject
    @PopUpWindowRight
    lateinit var mPopupWindowRight: PopupWindow
    private lateinit var bubbleViewRight: BubbleLayout
    private lateinit var popTextRight: MaterialTextView

    private var mainLayoutBinding: MainLayoutBinding? = null

    private var suggestionAdapter: SuggestionAdapter?= null
    private var emojiKigouAdapter: EmojiKigouAdapter?= null
    private var kigouApdater: KigouAdapter?= null

    private val _currentInputMode = MutableStateFlow<InputMode>(InputMode.ModeJapanese)
    private val _inputString = MutableStateFlow(Companion.EMPTY_STRING)
    private var stringInTail = ""
    private val _currentKeyboardMode = MutableStateFlow<KeyboardMode>(KeyboardMode.ModeTenKeyboard)
    private val _currentModeInKigou = MutableStateFlow<ModeInKigou>(ModeInKigou.Null)
    private val _dakutenPressed = MutableStateFlow(false)
    private val _suggestionList = MutableStateFlow<List<String>>(emptyList())
    private val _suggestionFlag = MutableStateFlow(false)

    private var currentInputType: InputTypeForIME = InputTypeForIME.Text
    private var currentTenKeyId = 0
    private var lastFlickConvertedNextHiragana = false
    private var isContinuousTapInputEnabled = false
    private var englishSpaceKeyPressed = false

    private var firstXPoint = 0.0f
    private var firstYPoint = 0.0f
    private var suggestionClickNum = 0
    private var isHenkan = false
    private var onLeftKeyLongPressUp = false
    private var onRightKeyLongPressUp = false

    private var onDeleteLongPressUp = false
    private var deleteKeyLongKeyPressed = false
    private var NGword = "登別"
    var text = currentInputConnection.getExtractedText(ExtractedTextRequest(),0).text
    //textGenTextOnlyPrompt()
    private fun setEnterKey(imageButton: AppCompatImageButton) = imageButton.apply {
        setOnClickListener {
            if (_inputString.value.isNotEmpty()){
                when(_currentInputMode.value){
                    InputMode.ModeJapanese ->{




                        if (isHenkan){




                            if (suggestionClickNum > _suggestionList.value.size) suggestionClickNum = 0
                            val listIterator = if (suggestionClickNum > 0) _suggestionList.value.listIterator(suggestionClickNum - 1) else {
                                _suggestionList.value.listIterator(suggestionClickNum)
                            }
                            setEnterKeyAction(listIterator)
                        }else {
                            currentInputConnection?.finishComposingText()
                            _inputString.value = EMPTY_STRING
                        }
                        resetFlagsKeyEnter()
                    }
                    else ->{




                        currentInputConnection?.finishComposingText()
                        _inputString.value = EMPTY_STRING
                        resetFlagsKeyEnter()
                    }
                }
            }else{




                if (stringInTail.isNotEmpty()){
                    currentInputConnection?.finishComposingText()
                    stringInTail = EMPTY_STRING
                    return@setOnClickListener
                }

                setEnterKeyPress()
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
                //textGenTextOnlyPrompt()
                isHenkan = false
                suggestionClickNum = 0
            }
        }

    }
    private fun setEnterKeyAction(listIterator: ListIterator<String>) = CoroutineScope(ioDispatcher).launch {
        _suggestionList.update { emptyList() }
        val nextSuggestion = listIterator.next()
        currentInputConnection?.commitText(nextSuggestion,1)
        if (stringInTail.isNotEmpty()){
            delay(DISPLAY_LEFT_STRING_TIME)
            _inputString.update { stringInTail }
            stringInTail = Companion.EMPTY_STRING
        }else{
            _inputString.update { Companion.EMPTY_STRING }
        }
        _suggestionFlag.update { flag -> !flag }
    }
    private fun resetFlagsKeyEnter(){
        isHenkan = false
        suggestionClickNum = 0
        englishSpaceKeyPressed = false
        onDeleteLongPressUp = false
        _dakutenPressed.value = false
        lastFlickConvertedNextHiragana = true
        isContinuousTapInputEnabled = true
    }
    private fun setEnterKeyPress(){




        var text = currentInputConnection.getExtractedText(ExtractedTextRequest(),0).text
        when(currentInputType){

            InputTypeForIME.TextMultiLine,
            InputTypeForIME.TextImeMultiLine ->{
                currentInputConnection?.commitText("\n",1)
            }
            InputTypeForIME.None,
            InputTypeForIME.Text,
            InputTypeForIME.TextAutoComplete,
            InputTypeForIME.TextAutoCorrect,
            InputTypeForIME.TextCapCharacters,
            InputTypeForIME.TextCapSentences,
            InputTypeForIME.TextCapWords,
            InputTypeForIME.TextEmailSubject,
            InputTypeForIME.TextFilter,
            InputTypeForIME.TextShortMessage,
            InputTypeForIME.TextLongMessage,
            InputTypeForIME.TextNoSuggestion,
            InputTypeForIME.TextPersonName,
            InputTypeForIME.TextPhonetic,
            InputTypeForIME.TextWebEditText,
            InputTypeForIME.TextUri,
            InputTypeForIME.TextPostalAddress,
            InputTypeForIME.TextEmailAddress,
            InputTypeForIME.TextWebEmailAddress,
            InputTypeForIME.TextPassword,
            InputTypeForIME.TextVisiblePassword,
            InputTypeForIME.TextWebPassword,
            InputTypeForIME.TextWebSearchView,
            InputTypeForIME.TextNotCursorUpdate,
            InputTypeForIME.TextWebSearchViewFireFox,
            InputTypeForIME.TextEditTextInBookingTDBank
            -> {
                currentInputConnection?.apply {
                    Timber.d("Enter key: called 3\n" )
                    if (text == NGword)
                    {
                        val toast = Toast.makeText(this@Gemini_API, "禁止ワード", Toast.LENGTH_LONG)
                        toast.show()
                        currentInputConnection.deleteSurroundingText(text.length,0)
                    }
                    else
                    {
                        //textGenTextOnlyPrompt()
                        sendKeyEvent(
                            KeyEvent(
                                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER
                            )
                        )
                        sendKeyEvent(
                            KeyEvent(
                                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER
                            )
                        )
                    }
                }
            }

            InputTypeForIME.Number,
            InputTypeForIME.NumberDecimal,
            InputTypeForIME.NumberPassword,
            InputTypeForIME.NumberSigned,
            InputTypeForIME.Phone,
            InputTypeForIME.Date,
            InputTypeForIME.Datetime,
            InputTypeForIME.Time,
            -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
            }

            InputTypeForIME.TextSearchView ->{
                currentInputConnection?.apply {
                    Timber.d("enter key search: ${EditorInfo.IME_ACTION_SEARCH}" +
                            "\n${currentInputEditorInfo.inputType}" +
                            "\n${currentInputEditorInfo.imeOptions}" +
                            "\n${currentInputEditorInfo.actionId}" +
                            "\n${currentInputEditorInfo.privateImeOptions}")
                    if (text == NGword)
                    {
                        currentInputConnection.deleteSurroundingText(text.length,0)
                    }
                    else
                    {
                        performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                    }
                }
            }

        }
    }
    private suspend fun textGenTextOnlyPrompt() {
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