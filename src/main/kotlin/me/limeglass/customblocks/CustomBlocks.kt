package me.limeglass.customblocks

import org.bukkit.plugin.java.JavaPlugin

class CustomBlocks : JavaPlugin() {

    companion object {
        lateinit var instance: CustomBlocks
    }

    override fun onEnable() {
        instance = this
    }

}
