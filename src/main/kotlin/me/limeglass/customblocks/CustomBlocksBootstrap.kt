package me.limeglass.customblocks

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import java.nio.file.Files
import java.util.jar.JarFile

class CustomBlocksBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val dataFolder = context.dataDirectory
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder)
            val jarFile = context.pluginSource.toFile()
            if (!jarFile.exists() || !jarFile.name.endsWith(".jar")) {
                throw IllegalStateException("Plugin source is not a valid JAR file.")
            }

            val jar = JarFile(jarFile)
            val jsonFiles = jar.entries().asSequence()
                .filter { it.name.startsWith("templates/") && it.name.endsWith(".json") }
                .toList()

            jsonFiles.forEach { entry ->
                val name = entry.name.substringAfter("templates/")
                val targetFile = dataFolder.resolve(name)
                Files.createDirectories(targetFile.parent)
                Files.newOutputStream(targetFile).use { output ->
                    jar.getInputStream(entry).use { input -> input.copyTo(output) }
                }
            }
        }

        Files.list(dataFolder)
            .filter { Files.isRegularFile(it) && !it.fileName.toString().startsWith("-") }
            .filter { it.fileName.toString().endsWith(".json") }
            .forEach { file ->
                val jsonObject = JsonParser.parseString(Files.readString(file)).asJsonObject

                jsonObject["blocks"]?.asJsonArray?.forEach { blockElement ->
                    val blockObject = blockElement.asJsonObject
                    val id = blockObject["id"]?.asString?.lowercase() ?: return@forEach
                    println("Registering block: $id")
                    val namespace = id.substringBefore(":", "minecraft")
                    val value = if (!id.contains(":")) id else id.substringAfter(":")
                    val propertiesJson = blockObject["properties"]?.asJsonObject

                    val blockProperties = propertiesJson?.let {
                        BlockBehaviour.Properties.CODEC.parse(
                            JsonOps.INSTANCE, it
                        ).result().orElse(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE))
                    } ?: BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)

                    val blockKey = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, value))
                    Registry.register(BuiltInRegistries.BLOCK, blockKey, Block(blockProperties.setId(blockKey)))
                }

                jsonObject["items"]?.asJsonArray?.forEach { itemElement ->
                    val itemObject = itemElement.asJsonObject
                    val id = itemObject["id"]?.asString ?: return@forEach
                    println("Registering item: $id")
                    val namespace = id.substringBefore(":", "minecraft")
                    val value = if (!id.contains(":")) id else id.substringAfter(":")

                    val itemKey = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, value))
                    Registry.register(BuiltInRegistries.ITEM, itemKey, Item(Item.Properties().setId(itemKey)))
                }
            }
    }
}