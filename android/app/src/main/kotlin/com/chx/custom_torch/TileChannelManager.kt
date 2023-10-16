package com.chx.custom_torch

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

object TileChannelManager {
    private var methodChannel: MethodChannel? = null

    var isOn = 0
    var isAdded = false

    fun init(binaryMessenger: BinaryMessenger) {
        methodChannel = MethodChannel(binaryMessenger, "tile_channel")
    }

//    fun invokeTileClick() {
//        methodChannel?.invokeMethod("onTileClick", null)
//    }
    fun invokeTileAdded() {
        methodChannel?.invokeMethod("onTileAdded", null)
    }
    fun invokeTileAlreadyAdded() {
        methodChannel?.invokeMethod("onTileAlreadyAdded", null)
    }
    fun invokeOnToggle() {
        methodChannel?.invokeMethod("onToggle", null)
    }
}
