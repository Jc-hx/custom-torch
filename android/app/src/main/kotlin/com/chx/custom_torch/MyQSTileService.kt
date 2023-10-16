package com.chx.custom_torch

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.hardware.camera2.CameraManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyQSTileService : TileService() {

    private lateinit var cameraManager: CameraManager
    private var brightnessLevel = 1
    private var maxFlashlightBrightnessLevel = 45
    private var stepsNumber = 5
    private var vibrationsTile = true
    private var tileEffect = true

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
    }


    override fun onStartListening() {
        super.onStartListening()
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
    override fun onClick() {
        super.onClick()
        TileChannelManager.isAdded = true
        val vibrator = getSystemService(Vibrator::class.java)
        val sharedPreferences: SharedPreferences = getSharedPreferences("AndroidSharedPrefs", Context.MODE_PRIVATE)
        val prefs = sharedPreferences.edit()
        if (qsTile.state == Tile.STATE_ACTIVE) {
            inactiveTile()
            CoroutineScope(Dispatchers.Default).launch {
                turnOffTorch()
            }
            prefs.putInt("torchStatus", 0)
        } else {
            activeTile()
            CoroutineScope(Dispatchers.Default).launch {
                turnOnTorchWithStrengthLevel(brightnessLevel)
            }

            prefs.putInt("torchStatus", 1)
        }
        if (vibrationsTile) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }

        prefs.apply()

        //TileChannelManager.invokeTileClick()
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
            if (tileEffect && torchStrength2 != 1) {
                for (i in torchStrength2 downTo 1) {
                    delay(delayTime)
                    cameraManager.turnOnTorchWithStrengthLevel(cameraId, i)
                }
                cameraManager.setTorchMode(cameraId, false)
            } else {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private suspend fun turnOnTorchWithStrengthLevel(torchStrength: Int) {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first available camera
            var torchStrength2 = torchStrength * maxFlashlightBrightnessLevel / stepsNumber
            if (torchStrength2 == 0 || torchStrength2 <= (maxFlashlightBrightnessLevel / stepsNumber)) {
                torchStrength2 = 1
            }
            var delayTime = 500L / torchStrength2
            if (tileEffect && torchStrength2 != 1) {
                for (i in 1..torchStrength2) {
                    delay(delayTime)
                    cameraManager.turnOnTorchWithStrengthLevel(cameraId, i)
                }
            } else {
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

}