package com.chx.custom_torch

import android.app.AlertDialog
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.Executor
import java.util.function.Consumer


class MainActivity: FlutterActivity() {
    private lateinit var cameraManager: CameraManager
    private var maxFlashlightBrightnessLevel: Int? = 25
    private var vibrationsMenu = true
    private var status: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        TileChannelManager.init(flutterEngine.dartExecutor.binaryMessenger)
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        vibrationsMenu = sharedPreferences.getBoolean("vibrationsMenu", true)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "torch_control_channel")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "turnOnTorchWithStrengthLevel" -> {
                        val torchStrength = call.arguments as Int
                        turnOnTorchWithStrengthLevel(torchStrength)
                        result.success(null)
                    }
                    "turnOffTorch" -> {
                        turnOffTorch()
                        result.success(null)
                    }
                    "saveBrightness" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val torchStrength = call.arguments as Int
                        Log.d("MyQSTileService", "brightnessLevel : $torchStrength")
                        prefs.putInt("brightnessLevel", torchStrength)
                        prefs.apply()
                        result.success(null)
                    }
                    "saveStepsNumber" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val stepsNumber = call.arguments as Int
                        Log.d("MyQSTileService", "stepsNumber : $stepsNumber")
                        prefs.putInt("stepsNumber", stepsNumber)
                        prefs.apply()
                        result.success(null)
                    }
                    "popup" -> {
                        val intent = Intent(this, PopupActivity::class.java)
                        startActivity(intent)
                        result.success(null)
                    }
                    "checkFlash" -> {
                        checkFlash()
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        maxFlashlightBrightnessLevel?.let {
                            prefs.putInt("maxFlashlightBrightnessLevel",
                                it
                            )
                        }
                        prefs.apply()
                        result.success(maxFlashlightBrightnessLevel)
                    }
                    "checkStatus" -> {
                        checkStatus()
                        result.success(status)
                    }
                    "checkPrefs" -> {
                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        vibrationsMenu = sharedPreferences.getBoolean("vibrationsMenu", true)
                        result.success(status)
                    }
                    "getColor1" -> {
                        val resources: Resources = resources
                        val colorResourceId = resources.getIdentifier("system_neutral1_900", "color", packageName)
                        val colorValue = if (colorResourceId != 0) ContextCompat.getColor(context, R.color.system_neutral1_900) else 0
                        //Log.d("color", "color : $colorValue")
                        result.success(colorValue)
                    }
                    "getColor2" -> {
                        val resources: Resources = resources
                        val colorResourceId = resources.getIdentifier("system_accent1_200", "color", packageName)
                        val colorValue = if (colorResourceId != 0) ContextCompat.getColor(context, R.color.system_accent1_200) else 0
                        //Log.d("color", "color : $colorValue")
                        result.success(colorValue)
                    }
                    "getColor3" -> {
                        val resources: Resources = resources
                        val colorResourceId = resources.getIdentifier("system_neutral1_50", "color", packageName)
                        val colorValue = if (colorResourceId != 0) ContextCompat.getColor(context, R.color.system_neutral1_50) else 0
                        //Log.d("color", "color : $colorValue")
                        result.success(colorValue)
                    }
                    "getColor4" -> {
                        val resources: Resources = resources
                        val colorResourceId = resources.getIdentifier("system_accent1_600", "color", packageName)
                        val colorValue = if (colorResourceId != 0) ContextCompat.getColor(context, R.color.system_accent1_600) else 0
                        //Log.d("color", "color : $colorValue")
                        result.success(colorValue)
                    }
                    "vibrationsMenu" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val vibrationsMenu = call.arguments as Boolean
                        Log.d("Settings", "vibrationsMenu : $vibrationsMenu")
                        prefs.putBoolean("vibrationsMenu", vibrationsMenu)
                        prefs.apply()
                        result.success(null)
                    }
                    "vibrationsTile" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val vibrationsTile = call.arguments as Boolean
                        Log.d("Settings", "vibrationsTile : $vibrationsTile")
                        prefs.putBoolean("vibrationsTile", vibrationsTile)
                        prefs.apply()
                        result.success(null)
                    }
                    "vibrationsPopup" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val vibrationsPopup = call.arguments as Boolean
                        Log.d("Settings", "vibrationsPopup : $vibrationsPopup")
                        prefs.putBoolean("vibrationsPopup", vibrationsPopup)
                        prefs.apply()
                        result.success(null)
                    }
                    "tileEffect" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val tileEffect = call.arguments as Boolean
                        Log.d("Settings", "tileEffect : $tileEffect")
                        prefs.putBoolean("tileEffect", tileEffect)
                        prefs.apply()
                        result.success(null)
                    }
                    "popupAutoOn" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val popupAutoOn = call.arguments as Boolean
                        Log.d("Settings", "popupAutoOn : $popupAutoOn")
                        prefs.putBoolean("popupAutoOn", popupAutoOn)
                        prefs.apply()
                        result.success(null)
                    }
                    "popupAutoOff" -> {
                        val sharedPreferences = context.getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        val popupAutoOff = call.arguments as Boolean
                        Log.d("Settings", "popupAutoOff : $popupAutoOff")
                        prefs.putBoolean("popupAutoOff", popupAutoOff)
                        prefs.apply()
                        result.success(null)
                    }
                    else -> result.notImplemented()

                }
            }
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "tile_channel")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "addTile" -> {
                        addTile()
                        result.success(null)
                    }
                    "toggleOn" -> {
                        TileChannelManager.isOn = 1
                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        prefs.putInt("torchStatus", 1)
                        prefs.apply()
                        result.success(null)
                    }
                    "toggleOff" -> {
                        TileChannelManager.isOn = 0
                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        prefs.putInt("torchStatus", 0)
                        prefs.apply()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }


    private fun checkFlash() {
        try {
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val flashlightAvailable = characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) ?: false

                if (flashlightAvailable) {
                    maxFlashlightBrightnessLevel = characteristics.get(
                        CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL
                    )

                    if (maxFlashlightBrightnessLevel != null && maxFlashlightBrightnessLevel!! > 1) {
                        // Do nothing
                        Log.d("TorchCompatibility", "Torch seems compatible with the app, maxBrightness : $maxFlashlightBrightnessLevel")
                        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                        val prefs = sharedPreferences.edit()
                        prefs.putInt("maxBrightness", maxFlashlightBrightnessLevel!!)
                        prefs.apply()
                    } else {
                        // Flashlight brightness can't be modified
                        Log.d("TorchCompatibility", "Torch seems not compatible with the app")
                        val alertDialogBuilder = AlertDialog.Builder(this)

                        alertDialogBuilder.setMessage("Your phone seems not compatible with this app, sorry.")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun addTile() {
        if (!TileChannelManager.isAdded) {
            val tileServiceComponentName = ComponentName(context, MyQSTileService::class.java)
            val tileLabel = "Custom Torch"
            val icon = Icon.createWithResource(context, R.mipmap.ic_launcher)
            val statusBarManager: StatusBarManager = getSystemService(StatusBarManager::class.java)
            val resultSuccessExecutor = Executor {
                Log.d("MyQSTileService", "requestAddTileService result success")
            }
            val callback = Consumer<Int> { resultCode ->
                Log.d("MyQSTileService", "requestAddTileService result code: $resultCode")
                when (RequestResult.findByCode(resultCode)) {
                    RequestResult.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                    }

                    else -> {
                        Log.e(
                            "MyQSTileService",
                            "Tile addition failed with result code: $resultCode"
                        )
                    }
                }
            }

            //Log.d("MyQSTileService", "onCreate: statusBarManager $statusBarManager")

            statusBarManager.requestAddTileService(
                tileServiceComponentName,
                tileLabel,
                icon,
                resultSuccessExecutor,
                callback
            ) //callback is broken :(
        } else {
            TileChannelManager.invokeTileAlreadyAdded()
        }
    }

    private fun checkStatus() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        status = sharedPreferences.getInt("torchStatus", 0)
    }

    private fun turnOffTorch() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            cameraManager.setTorchMode(cameraId, false)
            val vibrator = getSystemService(Vibrator::class.java)
            if (vibrationsMenu) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOnTorchWithStrengthLevel(torchStrength: Int) {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, torchStrength)
            val vibrator = getSystemService(Vibrator::class.java)
            if (vibrationsMenu) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}
