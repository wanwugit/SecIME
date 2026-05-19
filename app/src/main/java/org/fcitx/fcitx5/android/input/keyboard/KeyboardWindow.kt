/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.text.InputType
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.input.SecLogger
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.transition.Slide
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.CapabilityFlags
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.bar.KawaiiBarComponent
import org.fcitx.fcitx5.android.input.broadcast.InputBroadcastReceiver
import org.fcitx.fcitx5.android.input.broadcast.ReturnKeyDrawableComponent
import org.fcitx.fcitx5.android.input.dependency.fcitx
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.fcitx.fcitx5.android.input.dependency.theme
import org.fcitx.fcitx5.android.input.bus.InputDecisionBus
import org.fcitx.fcitx5.android.input.bus.LanguageAdapterComponent
import org.fcitx.fcitx5.android.input.picker.PickerWindow
import org.fcitx.fcitx5.android.input.popup.PopupActionListener
import org.fcitx.fcitx5.android.input.popup.PopupComponent
import org.fcitx.fcitx5.android.input.wm.EssentialWindow
import org.fcitx.fcitx5.android.input.wm.InputWindow
import org.fcitx.fcitx5.android.input.wm.InputWindowManager
import org.mechdancer.dependency.manager.must
import splitties.views.dsl.core.add
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent

class KeyboardWindow : InputWindow.SimpleInputWindow<KeyboardWindow>(), EssentialWindow,
    InputBroadcastReceiver {

    private val service by manager.inputMethodService()
    private val fcitx by manager.fcitx()
    private val theme by manager.theme()
    private val commonKeyActionListener: CommonKeyActionListener by manager.must()
    private val inputDecisionBus: InputDecisionBus by manager.must()
    private val languageAdapter: LanguageAdapterComponent by manager.must()
    private val windowManager: InputWindowManager by manager.must()
    private val popup: PopupComponent by manager.must()
    private val bar: KawaiiBarComponent by manager.must()
    private val returnKeyDrawable: ReturnKeyDrawableComponent by manager.must()

    companion object : EssentialWindow.Key

    enum class KeyboardMode { T9, QWERTY }

    /** Locked keyboard mode — only changed via toolbar picker */
    var keyboardMode: KeyboardMode = KeyboardMode.QWERTY
        private set

    /**
     * Gate: allows switchLayout to reach TextKeyboard while in T9 mode.
     * Only set to true by switchToQwertyEnglish (中/英 key) and setKeyboardMode.
     * All other paths (ABC buttons, onImeUpdate, onStartInput) are blocked.
     */
    private var allowQwertyInT9 = false

    override val key: EssentialWindow.Key
        get() = KeyboardWindow

    override fun enterAnimation(lastWindow: InputWindow) = Slide().apply {
        slideEdge = Gravity.BOTTOM
    }.takeIf {
        lastWindow !is PickerWindow
    }

    override fun exitAnimation(nextWindow: InputWindow) =
        super.exitAnimation(nextWindow).takeIf {
            nextWindow !is PickerWindow
        }

    private lateinit var keyboardView: FrameLayout

    private val keyboards: HashMap<String, BaseKeyboard> by lazy {
        hashMapOf(
            TextKeyboard.Name to TextKeyboard(context, theme),
            NumberKeyboard.Name to NumberKeyboard(context, theme),
            T9Keyboard.Name to T9Keyboard(context, theme)
        )
    }
    private var currentKeyboardName = ""
    private var lastSymbolType: String by AppPrefs.getInstance().internal.lastSymbolLayout

    /** Locked mode — for toolbar icon and gate logic */
    fun isT9Active(): Boolean = keyboardMode == KeyboardMode.T9

    /** Current keyboard is actually T9 layout — for InputView valve */
    fun isT9KeyboardShowing(): Boolean = currentKeyboardName == T9Keyboard.Name

    private val currentKeyboard: BaseKeyboard? get() = keyboards[currentKeyboardName]

    private val keyActionListener = KeyActionListener { it, source ->
        val detail = when (it) {
            is KeyAction.FcitxKeyAction -> "FcitxKeyAction(act='${it.act}', states=${it.states.states}, code=${it.code})"
            is KeyAction.SymAction -> "SymAction(sym=${it.sym.sym}, states=${it.states.states})"
            else -> it.toString()
        }
        SecLogger.d("KBWin", "KeyboardWindow.keyAction: $detail, currentKeyboard='$currentKeyboardName', mode=$keyboardMode")
        if (it is KeyAction.LayoutSwitchAction) {
            switchLayout(it.act)
        } else if (it is KeyAction.T9DigitAction) {
            inputDecisionBus.onDigitPress(it.digit[0])
        } else if (it is KeyAction.T9ResetAction) {
            inputDecisionBus.onT9Reset()
        } else if (it is KeyAction.T9CandidateSelectAction) {
            inputDecisionBus.onUiCandidateSelect(it.index)
        } else if (it is KeyAction.T9CommitAction) {
            inputDecisionBus.onCommitText(it.text)
        } else if (it is KeyAction.T9SegmentAction) {
            inputDecisionBus.onSegment()
        } else if (it is KeyAction.T9ZeroAction) {
            inputDecisionBus.onDigitPress('0')
        } else if (it is KeyAction.T9BackspaceAction) {
            inputDecisionBus.onT9Backspace()
        } else if (it is KeyAction.T9SpaceAction) {
            inputDecisionBus.onQwertyKey("space", 0, 0x20)
        } else if (it is KeyAction.T9EnterAction) {
            inputDecisionBus.onQwertyKey("Return", 0, 0xFF0D)
        } else if (it is KeyAction.CommitAction) {
            inputDecisionBus.onCommitTextToApp(it.text)
        } else if (it is KeyAction.FcitxKeyAction) {
            inputDecisionBus.onQwertyKey(it.act, it.states.states.toInt(), it.code)
        } else if (it is KeyAction.SymAction) {
            inputDecisionBus.onQwertyKeySym(it.sym.sym, it.states.states.toInt())
        } else if (it is KeyAction.LangSwitchAction && keyboardMode == KeyboardMode.T9) {
            switchToQwertyEnglish()
        } else {
            commonKeyActionListener.listener.onKeyAction(it, source)
        }
    }

    private val popupActionListener: PopupActionListener by lazy {
        popup.listener
    }

    override fun onCreateView(): View {
        keyboardView = context.frameLayout(R.id.keyboard_view)
        attachLayout(TextKeyboard.Name)
        return keyboardView
    }

    private fun detachCurrentLayout() {
        currentKeyboard?.also {
            it.onDetach()
            keyboardView.removeView(it)
            it.keyActionListener = null
            it.popupActionListener = null
        }
    }

    private fun attachLayout(target: String) {
        SecLogger.d("KBWin", "KeyboardWindow.attachLayout: target='$target'")
        currentKeyboardName = target
        currentKeyboard?.let {
            it.keyActionListener = keyActionListener
            it.popupActionListener = popupActionListener
            keyboardView.apply { add(it, lParams(matchParent, matchParent)) }
            it.onAttach()
            it.onReturnDrawableUpdate(returnKeyDrawable.resourceId)
            it.onInputMethodUpdate(fcitx.runImmediately { inputMethodEntryCached })
        }
    }

    /**
     * Core layout switch — ALL paths converge here.
     * Lock: in T9 mode, TextKeyboard is blocked unless [allowQwertyInT9] gate is open.
     */
    fun switchLayout(to: String, remember: Boolean = true) {
        val rawTarget = to.ifEmpty { lastSymbolType }
        // Lock: T9 mode blocks TextKeyboard unless gate is open
        val target = if (keyboardMode == KeyboardMode.T9 && rawTarget == TextKeyboard.Name && !allowQwertyInT9) {
            SecLogger.d("KBWin", "KeyboardWindow.switchLayout: LOCKED — redirect TextKeyboard→T9 in T9 mode")
            T9Keyboard.Name
        } else {
            rawTarget
        }
        // Close gate after one use
        allowQwertyInT9 = false
        SecLogger.d("KBWin", "KeyboardWindow.switchLayout: raw='$rawTarget', resolved='$target', current='$currentKeyboardName', mode=$keyboardMode")
        ContextCompat.getMainExecutor(service).execute {
            if (keyboards.containsKey(target)) {
                if (remember && target != TextKeyboard.Name) {
                    lastSymbolType = target
                }
                if (target == currentKeyboardName) return@execute
                val wasT9 = currentKeyboardName == T9Keyboard.Name
                detachCurrentLayout()
                attachLayout(target)
                if (wasT9 && target == TextKeyboard.Name) {
                    inputDecisionBus.onT9Reset()
                    service.lifecycleScope.launch {
                        languageAdapter.activateQwertySchema()
                    }
                }
                if (!wasT9 && target == T9Keyboard.Name) {
                    service.lifecycleScope.launch {
                        languageAdapter.activateT9Schema()
                    }
                }
                if (windowManager.isAttached(this)) {
                    notifyBarLayoutChanged()
                }
            } else {
                if (remember) {
                    lastSymbolType = PickerWindow.Key.Symbol.name
                }
                windowManager.attachWindow(PickerWindow.Key.Symbol)
            }
        }
    }

    /** Called from toolbar picker — the ONLY way to change keyboardMode */
    fun setKeyboardMode(mode: KeyboardMode) {
        SecLogger.d("KBWin", "KeyboardWindow.setKeyboardMode: mode=$mode, current=$keyboardMode")
        val wasT9 = keyboardMode == KeyboardMode.T9
        keyboardMode = mode
        allowQwertyInT9 = true // allow switchLayout to reach TextKeyboard if switching to QWERTY
        when (mode) {
            KeyboardMode.T9 -> switchLayout(T9Keyboard.Name)
            KeyboardMode.QWERTY -> {
                switchLayout(TextKeyboard.Name)
                // switchLayout handles T9→Text schema switch via wasT9 check,
                // but if we were! already in QWERTY mode (e.g. re-tapping QWERTY),
                // ensure schema is rime_frost not rime_frost_t9
                if (!wasT9) {
                    service.lifecycleScope.launch {
                        languageAdapter.activateQwertySchema()
                    }
                }
            }
        }
    }

    /** T9 mode 中/英切换：toggle between 9键拼音 and 26键英文 */
    private fun switchToQwertyEnglish() {
        SecLogger.d("KBWin", "KeyboardWindow.switchToQwertyEnglish: currentKeyboard='$currentKeyboardName'")
        if (currentKeyboardName == T9Keyboard.Name) {
            // 9键拼音 → 26键英文: open gate, then switch
            allowQwertyInT9 = true
            service.lifecycleScope.launch {
                languageAdapter.commitCurrentPreedit()
                languageAdapter.enumerateIme()
            }
            switchLayout(TextKeyboard.Name)
        } else {
            // 26键英文 → 9键拼音: switchLayout(T9) already calls ensureChineseIme
            switchLayout(T9Keyboard.Name)
        }
    }

    override fun onStartInput(info: EditorInfo, capFlags: CapabilityFlags) {
        val targetLayout = when (info.inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER -> NumberKeyboard.Name
            InputType.TYPE_CLASS_PHONE -> NumberKeyboard.Name
            else -> when (keyboardMode) {
                KeyboardMode.T9 -> T9Keyboard.Name
                KeyboardMode.QWERTY -> TextKeyboard.Name
            }
        }
        SecLogger.d("KBWin", "KeyboardWindow.onStartInput: inputType=${info.inputType}, targetLayout='$targetLayout', mode=$keyboardMode")
        switchLayout(targetLayout, remember = false)
    }

    override fun onImeUpdate(ime: InputMethodEntry) {
        SecLogger.d("KBWin", "KeyboardWindow.onImeUpdate: ime=${ime.name}, languageCode=${ime.languageCode}, currentKeyboard='$currentKeyboardName', mode=$keyboardMode")
        currentKeyboard?.onInputMethodUpdate(ime)
        if (keyboardMode == KeyboardMode.T9) {
            val isChinese = ime.languageCode.startsWith("zh")
            if (isChinese && currentKeyboardName != T9Keyboard.Name) {
                // IME切回中文 → 自动回到T9键盘
                switchLayout(T9Keyboard.Name)
            } else if (!isChinese && currentKeyboardName == T9Keyboard.Name) {
                // IME切到英文 → 切到26键英文键盘（开gate）
                allowQwertyInT9 = true
                switchLayout(TextKeyboard.Name)
            }
            // 其他情况不切换：已经在26键英文上且IME是英文，或已经在T9上且IME是中文
        }
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        currentKeyboard?.onPunctuationUpdate(mapping)
    }

    override fun onReturnKeyDrawableUpdate(resourceId: Int) {
        currentKeyboard?.onReturnDrawableUpdate(resourceId)
    }

    override fun onAttached() {
        currentKeyboard?.let {
            it.keyActionListener = keyActionListener
            it.popupActionListener = popupActionListener
            it.onAttach()
        }
        notifyBarLayoutChanged()
    }

    override fun onDetached() {
        currentKeyboard?.let {
            it.onDetach()
            it.keyActionListener = null
            it.popupActionListener = null
        }
        popup.dismissAll()
    }

    private fun notifyBarLayoutChanged() {
        val isNumber = currentKeyboardName == NumberKeyboard.Name || currentKeyboardName == T9Keyboard.Name
        SecLogger.d("KBWin", "KeyboardWindow.notifyBarLayoutChanged: current='$currentKeyboardName', isNumber=$isNumber, mode=$keyboardMode")
        bar.onKeyboardLayoutSwitched(isNumber)
        bar.onKeyboardModeChanged(keyboardMode == KeyboardMode.T9)
    }
}