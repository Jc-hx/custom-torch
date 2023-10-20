package com.chx.custom_torch

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView


class PopupActivity : Activity() {
    private lateinit var cameraManager: CameraManager
    private lateinit var keyguardManager: KeyguardManager
    private var popupAutoOn = true
    private var popupAutoOff = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 101)
            finish()
        } else {
//            setContentView(R.layout.popup_layout)
            val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
            popupAutoOn = sharedPreferences.getBoolean("popupAutoOn", true)
            if (popupAutoOn) {
                turnOnTorchWithStrengthLevel(1)
                if (sharedPreferences.getInt("torchStatus", 0) == 0) {
                    val prefs = sharedPreferences.edit()
                    prefs.putInt("torchStatus", 1)
                    prefs.apply()
                    TileChannelManager.isOn = sharedPreferences.getInt("torchStatus", 0)
                    TileChannelManager.invokeOnToggle()
                }
            }


            val messageTextView = TextView(this)
            messageTextView.text = "This a test dialog."
            messageTextView.textSize = 30f
            messageTextView.setTextColor(Color.WHITE)
            messageTextView.gravity = Gravity.CENTER
            messageTextView.setPadding(16, 16, 16, 16)

            setShowWhenLocked(true)
            setTurnScreenOn(true)

            // Vertical SeekBar
            setContentView(R.layout.vertical_seekbar)

            val verticalSeekBar = findViewById<SeekBar>(R.id.verticalSeekBar)

            val screenHeight = resources.displayMetrics.heightPixels
            val screenWidth = resources.displayMetrics.widthPixels
            val seekBarHeight = (screenHeight * 0.5).toInt()
            val seekBarWidth = (screenWidth * 0.35).toInt()

            verticalSeekBar.layoutParams = LinearLayout.LayoutParams(
                seekBarWidth,
                seekBarHeight
            )

            verticalSeekBar.minHeight = seekBarWidth
            verticalSeekBar.maxHeight = seekBarWidth

            if (popupAutoOn) {
                verticalSeekBar.progress = 1
            }

            val maxStepsNumber = sharedPreferences.getInt("stepsNumber", 5)

            val dotBitmap = Bitmap.createBitmap( 20f.toInt() * 2,  20f.toInt() * 2, Bitmap.Config.ARGB_8888)
            val dotCanvas = Canvas(dotBitmap)
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            dotPaint.color = Color.WHITE
            dotCanvas.drawCircle(20f, 20f, 20f, dotPaint)
            val thumbDrawable = BitmapDrawable(resources, dotBitmap)
            //val tickMarkDrawable = resources.getDrawable(R.drawable.custom_ticks_seekbar, theme)
            //verticalSeekBar.tickMark = thumbDrawable
            verticalSeekBar.splitTrack = false
            verticalSeekBar.thumb.mutate().alpha = 0
            verticalSeekBar.max = maxStepsNumber

            val vibrator = getSystemService(Vibrator::class.java)

            verticalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val currentProgress = verticalSeekBar.progress
                    Log.d("seekbar", "current seekbar value : $currentProgress")
                    if (currentProgress == 0) {
                        turnOffTorch()
                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val vibrationsPopup = sharedPreferences.getBoolean("vibrationsPopup", true)
                        prefs.putInt("torchStatus", 0)
                        prefs.apply()
                        TileChannelManager.isOn = sharedPreferences.getInt("torchStatus", 0)
                        TileChannelManager.invokeOnToggle()
                        if (vibrationsPopup) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                        }
                    } else {

                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val maxBrightness = sharedPreferences.getInt("maxBrightness", 45)
                        val vibrationsPopup = sharedPreferences.getBoolean("vibrationsPopup", true)
                        if (currentProgress == 1) { //force min. brightness
                            turnOnTorchWithStrengthLevel(1)
                        } else {
                            turnOnTorchWithStrengthLevel(currentProgress * maxBrightness / maxStepsNumber)
                        }
                        if (sharedPreferences.getInt("torchStatus", 0) == 0) {
                            val prefs = sharedPreferences.edit()
                            prefs.putInt("torchStatus", 1)
                            prefs.apply()
                            TileChannelManager.isOn = sharedPreferences.getInt("torchStatus", 0)
                            TileChannelManager.invokeOnToggle()
                        }
                        if (vibrationsPopup) {
                            if (currentProgress == maxStepsNumber) {
                                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                            } else {
                                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                            }
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Handle tracking touch
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        popupAutoOff = sharedPreferences.getBoolean("popupAutoOff", true)
        if (popupAutoOff) {
            turnOffTorch()
            val prefs = sharedPreferences.edit()
            prefs.putInt("torchStatus", 0)
            prefs.apply()
            if (TileChannelManager.isOn == 1) {
                TileChannelManager.isOn = sharedPreferences.getInt("torchStatus", 0)
                TileChannelManager.invokeOnToggle()
            }
        }
        Log.d("popup", "popup dismiss")
    }

    private fun turnOffTorch() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOnTorchWithStrengthLevel(torchStrength: Int) {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            if (torchStrength == 0) {
                turnOffTorch()
            } else {
                cameraManager.turnOnTorchWithStrengthLevel(cameraId, torchStrength)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}