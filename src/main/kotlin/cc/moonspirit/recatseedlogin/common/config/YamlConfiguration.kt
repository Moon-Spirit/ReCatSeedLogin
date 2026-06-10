/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

package cc.moonspirit.recatseedlogin.common.config

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.HashMap

class YamlConfiguration(private val file: File?) : Configuration {

    private val yaml: org.yaml.snakeyaml.Yaml = org.yaml.snakeyaml.Yaml()
    private var data: MutableMap<String, Any?> = HashMap()

    companion object {
        @JvmStatic
        fun loadConfiguration(file: File?): YamlConfiguration {
            val config = YamlConfiguration(file)
            if (file != null && file.exists()) {
                try {
                    config.load()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return config
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun load() {
        if (file != null && file.exists()) {
            FileInputStream(file).use { fis ->
                val loaded: Any? = yaml.load(InputStreamReader(fis, StandardCharsets.UTF_8))
                if (loaded is Map<*, *>) {
                    this.data = loaded as MutableMap<String, Any?>
                }
            }
        }
    }

    fun loadFromResource(inputStream: InputStream?) {
        if (inputStream != null) {
            try {
                InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                    @Suppress("UNCHECKED_CAST")
                    val loaded: Any? = yaml.load(reader)
                    if (loaded is Map<*, *>) {
                        this.data = HashMap<String, Any?>(loaded as Map<String, Any?>)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun save() {
        if (file == null) return
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            yaml.dump(data, writer)
        }
    }

    fun getDataMap(): Map<String, Any?> = data

    // ---- Common value parsing ----

    override fun getBoolean(path: String, defaultValue: Boolean): Boolean {
        val value = get(path)
        if (value is Boolean) {
            return value
        }
        if (value is String) {
            return value.toBoolean()
        }
        return defaultValue
    }

    private fun <T> parseNumeric(
        path: String,
        defaultValue: T,
        numberFn: (Number) -> T,
        stringFn: (String) -> T
    ): T {
        val value = getNumeric(path) ?: return defaultValue
        if (value is Number) return numberFn(value)
        return try {
            stringFn(value as String)
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    private fun getNumeric(path: String): Any? {
        val value = get(path)
        if (value is Number) return value
        if (value is String) return value
        return null
    }

    override fun getInt(path: String, defaultValue: Int): Int =
        parseNumeric(path, defaultValue, Number::toInt, String::toInt)

    override fun getLong(path: String, defaultValue: Long): Long =
        parseNumeric(path, defaultValue, Number::toLong, String::toLong)

    override fun getDouble(path: String, defaultValue: Double): Double =
        parseNumeric(path, defaultValue, Number::toDouble, String::toDouble)

    override fun getString(path: String, defaultValue: String): String {
        val value = get(path)
        return value?.toString() ?: defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringList(path: String): List<String> {
        val value = get(path)
        if (value is List<*>) {
            return value.map { it.toString() }
        }
        return Collections.emptyList()
    }

    override fun getSection(path: String): ConfigurationSection =
        ConfigurationSectionImpl(this, path)

    override fun set(path: String, value: Any?) {
        setNestedValue(data, path.split(".").toTypedArray(), 0, value)
    }

    override fun contains(path: String): Boolean = get(path) != null

    @Suppress("UNCHECKED_CAST")
    private fun get(path: String): Any? {
        return try {
            val parts = path.split(".").toTypedArray()
            var current: Map<String, Any?> = data
            for (i in 0 until parts.size - 1) {
                val next = current[parts[i]]
                if (next !is Map<*, *>) {
                    return null
                }
                current = next as Map<String, Any?>
            }
            current[parts[parts.size - 1]]
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setNestedValue(
        map: MutableMap<String, Any?>,
        keys: Array<String>,
        index: Int,
        value: Any?
    ) {
        try {
            if (index == keys.size - 1) {
                if (value == null) {
                    map.remove(keys[index])
                } else {
                    map[keys[index]] = value
                }
                return
            }
            var next = map.computeIfAbsent(keys[index]) { HashMap<String, Any?>() }
            if (next !is Map<*, *>) {
                next = HashMap<String, Any?>()
                map[keys[index]] = next
            }
            setNestedValue(next as MutableMap<String, Any?>, keys, index + 1, value)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun getFile(): File? = file

    private class ConfigurationSectionImpl(
        private val root: Configuration,
        private val path: String
    ) : ConfigurationSection {

        override fun getPath(): String = path

        override fun getRoot(): Configuration = root
    }
}