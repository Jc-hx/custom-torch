package com.chx.custom_torch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper


class MyQSTileService : TileService() {

    private lateinit var cameraManager: CameraManager
    private var brightnessLevel = 1
    private var maxFlashlightBrightnessLevel = 45
    private var stepsNumber = 5
    private var vibrationsTile = true
    private var tileEffect = true
    private var isSwitching = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        // registerFlashlightState(this)
    }


    override fun onStartListening() {
        super.onStartListening()
        try {
            cameraManager.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        brightnessLevel = sharedPreferences.getInt("brightnessLevel", 1)
        vibrationsTile = sharedPreferences.getBoolean("vibrationsTile", true)
        tileEffect = sharedPreferences.getBoolean("tileEffect", true)
        stepsNumber = sharedPreferences.getInt("stepsNumber", 5)
        maxFlashlightBrightnessLevel = sharedPreferences.getInt("maxFlashlightBrightnessLevel", 45)
        TileChannelManager.isOn = sharedPreferences.getInt("torchStatus", 0)
        if (TileChannelManager.isOn == 1) {
            activeTile()
        } else {
            inactiveTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick() {
        super.onClick()
        TileChannelManager.isAdded = true
        val vibrator = getSystemService(Vibrator::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        val prefs = sharedPreferences.edit()

        val currentBrightnessLevel = sharedPreferences.getInt("brightnessLevel", 1)

        if (qsTile.state == Tile.STATE_ACTIVE) {
            inactiveTile()
            job = coroutineScope.launch {
                turnOffTorch()
            }
            prefs.putInt("torchStatus", 0)
        } else {
            activeTile()
            job = coroutineScope.launch {
                turnOnTorchWithStrengthLevel(currentBrightnessLevel)
            }
            prefs.putInt("torchStatus", 1)
        }

        if (sharedPreferences.getBoolean("vibrationsTile", true)) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }

        prefs.apply()
        TileChannelManager.invokeOnToggle()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        TileChannelManager.isAdded = true
        TileChannelManager.invokeTileAdded()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        TileChannelManager.isAdded = false
    }

    private suspend fun turnOffTorch() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            var torchStrength2 = brightnessLevel * maxFlashlightBrightnessLevel / stepsNumber
            if (torchStrength2 == 0 || torchStrength2 <= (maxFlashlightBrightnessLevel / stepsNumber)) {
                torchStrength2 = 1
            }
            var delayTime = 500L / torchStrength2
            if (tileEffect && torchStrength2 != 1 && !isSwitching) {
                isSwitching = true
                for (i in torchStrength2 downTo 1) {
                    delay(delayTime)
                    var counter = 0
                    while (isSwitching && counter < 1 && i >= 1) {
                        cameraManager.turnOnTorchWithStrengthLevel(cameraId, i)
                        counter++
                    }
                    if (i == 1 && isSwitching) {
                        cameraManager.setTorchMode(cameraId, false)
                    }
                }
                isSwitching = false
            } else {
                isSwitching = false
                job?.cancel()
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private suspend fun turnOnTorchWithStrengthLevel(torchStrength: Int) {
        try {
            Log.i("TorchDebug", "torchStrength: $torchStrength")
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            var torchStrength2 = torchStrength * maxFlashlightBrightnessLevel / stepsNumber
            if (torchStrength2 == 0 || torchStrength2 <= (maxFlashlightBrightnessLevel / stepsNumber)) {
                torchStrength2 = 1
            }
            var delayTime = 500L / torchStrength2
            if (tileEffect && torchStrength2 != 1 && !isSwitching) {
                isSwitching = true
                for (i in 1..torchStrength2) {
                    delay(delayTime)
                    var counter = 0
                    while (isSwitching && counter < 1 && i < torchStrength2) {
                        cameraManager.turnOnTorchWithStrengthLevel(cameraId, i)
                        counter++
                    }
                }
                isSwitching = false
            } else {
                isSwitching = false
                job?.cancel()
                cameraManager.turnOnTorchWithStrengthLevel(cameraId, torchStrength2)
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun activeTile() {
        val newState = "On"
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.subtitle = "$newState"
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_fill)
        qsTile.updateTile()
    }

    private fun inactiveTile() {
        val newState = "Off"
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.subtitle = "$newState"
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_outline)
        qsTile.updateTile()
    }

    private fun registerFlashlightState(context: Context) {
        cameraManager.registerTorchCallback(torchCallback, null)
    }

    fun unregisterFlashlightState(context: Context) {
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    private val torchCallback: CameraManager.TorchCallback =
        object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
                val prefs = sharedPreferences.edit()
                super.onTorchModeChanged(cameraId, enabled)
                //TileChannelManager.invokeOnToggle()
                if (enabled) {
                    TileChannelManager.isOn = 1
                    Log.d("Torch status", "Torch On")
                    prefs.putInt("torchStatus", 1)
                    activeTile()
                } else {
                    TileChannelManager.isOn = 0
                    Log.d("Torch status", "Torch Off")
                    prefs.putInt("torchStatus", 0)
                    inactiveTile()
                }
                prefs.apply()
            }
        }

}