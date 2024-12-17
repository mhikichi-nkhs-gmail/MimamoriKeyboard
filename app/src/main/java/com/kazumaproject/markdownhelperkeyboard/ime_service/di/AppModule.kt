package com.kazumaproject.markdownhelperkeyboard.ime_service.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.google.android.flexbox.*
import com.kazumaproject.markdownhelperkeyboard.R
import com.kazumaproject.markdownhelperkeyboard.ime_service.components.TenKeyMap
import com.kazumaproject.markdownhelperkeyboard.ime_service.components.TenKeyMapHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.co.omronsoft.openwnn.ComposingText
import jp.co.omronsoft.openwnn.JAJP.OpenWnnEngineJAJP
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@SuppressLint("ServiceCast")
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @InputBackGroundDispatcher
    @Provides
    fun providesInputBackgroundDispatcher(): CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @KeyInputDispatcher
    @Provides
    fun providesIKeyInputDispatcher(): CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @SuggestionDispatcher
    @Provides
    fun providesSuggestionDispatcher(): CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @CursorMoveDispatcher
    @Provides
    fun providesCursorMoveDispatcher(): CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @DeleteLongDispatcher
    @Provides
    fun providesDeleteLongDispatcher(): CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

    @Singleton
    @Provides
    fun providesStringBuilder(): StringBuilder = StringBuilder()

    @Singleton
    @Provides
    fun providesSupervisorJob(): CompletableJob = SupervisorJob()

    @Singleton
    @Provides
    @Named("main_ime_scope")
    fun providesIMEScope(
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
        supervisorJob: CompletableJob
    ): CoroutineScope = CoroutineScope(supervisorJob + mainDispatcher)

    @Singleton
    @Provides
    fun providesOpenWnnEngineJP(
        @ApplicationContext context: Context
    ): OpenWnnEngineJAJP = OpenWnnEngineJAJP(
        "${context.filesDir.path}/writableJAJP.dic"
    ).apply {
        init()
        setDictionary(0)
        setKeyboardType(1)
    }

    @Singleton
    @Provides
    fun providesComposingText(): ComposingText = ComposingText()

    @Singleton
    @Provides
    fun providesTenKeyMap(): TenKeyMapHolder = TenKeyMap()

    @Singleton
    @Provides
    fun providesMutex(): Mutex = Mutex()



    @Singleton
    @Provides
    fun providesInputManager(@ApplicationContext context: Context) : InputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

    @DrawableReturn
    @Provides
    fun providesDrawableReturn(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.baseline_keyboard_return_24)!!

    @DrawableKanaSmall
    @Provides
    fun providesDrawableKanaSmall(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.kana_small)!!

    @DrawableEnglishSmall
    @Provides
    fun providesDrawableEnglishSmall(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.english_small)!!

    @DrawableHenkan
    @Provides
    fun providesDrawableHenkan(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.henkan)!!

    @DrawableSpaceBar
    @Provides
    fun providesDrawableSpaceBar(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.baseline_space_bar_24)!!

    @DrawableRightArrow
    @Provides
    fun providesDrawableRightArrow(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.baseline_arrow_right_alt_24)!!

    @DrawableLanguage
    @Provides
    fun providesDrawableLanguage(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.baseline_language_24)!!

    @DrawableNumberSmall
    @Provides
    fun providesDrawableNumberSmall(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.number_small)!!

    @DrawableOpenBracket
    @Provides
    fun providesDrawableOpenBracket(@ApplicationContext context: Context): Drawable = ContextCompat.getDrawable(context,
        com.kazumaproject.markdownhelperkeyboard.R.drawable.open_bracket)!!

    @SuppressLint("InflateParams")
    @PopUpTextActive
    @Provides
    fun providesPopUpWindowActive(@ApplicationContext context: Context): PopupWindow{
        val mPopupWindow = PopupWindow(context)
        val popupView = LayoutInflater
            .from(context)
            .inflate(R.layout.popup_layout,null)
        mPopupWindow.contentView = popupView
        return mPopupWindow
    }

    @SuppressLint("InflateParams")
    @PopUpWindowTop
    @Provides
    fun providesPopUpWindowTop(@ApplicationContext context: Context): PopupWindow{
        val mPopupWindow = PopupWindow(context)
        val popupView = LayoutInflater
            .from(context)
            .inflate(R.layout.popup_layout_top,null)
        mPopupWindow.contentView = popupView
        return mPopupWindow
    }
    @SuppressLint("InflateParams")
    @PopUpWindowLeft
    @Provides
    fun providesPopUpWindowLeft(@ApplicationContext context: Context): PopupWindow{
        val mPopupWindow = PopupWindow(context)
        val popupView = LayoutInflater
            .from(context)
            .inflate(R.layout.popup_window_left,null)
        mPopupWindow.contentView = popupView
        return mPopupWindow
    }

    @SuppressLint("InflateParams")
    @PopUpWindowBottom
    @Provides
    fun providesPopUpWindowBottom(@ApplicationContext context: Context): PopupWindow{
        val mPopupWindow = PopupWindow(context)
        val popupView = LayoutInflater
            .from(context)
            .inflate(R.layout.popup_layout_bottom,null)
        mPopupWindow.contentView = popupView
        return mPopupWindow
    }

    @SuppressLint("InflateParams")
    @PopUpWindowRight
    @Provides
    fun providesPopUpWindowRight(@ApplicationContext context: Context): PopupWindow{
        val mPopupWindow = PopupWindow(context)
        val popupView = LayoutInflater
            .from(context)
            .inflate(R.layout.popup_layout_right,null)
        mPopupWindow.contentView = popupView
        return mPopupWindow
    }

}