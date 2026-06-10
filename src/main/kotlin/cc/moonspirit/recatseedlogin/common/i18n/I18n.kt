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

package cc.moonspirit.recatseedlogin.common.i18n

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class I18n(
 private val dataFolder: File,
 private val resourceProvider: ResourceProvider,
) {

 interface ResourceProvider {
 fun getResource(name: String): InputStream?
 }

 private var currentLocale: Locale = Locale.SIMPLIFIED_CHINESE
 private val messages: MutableMap<Locale, MutableMap<String, String>> = ConcurrentHashMap()
 private val placeholders: MutableMap<String, Any> = ConcurrentHashMap()
 private var colorChar: Char = '&'

 fun setLocale(locale: Locale) {
 this.currentLocale = locale
 loadMessages(locale)
 }

 fun setLocale(languageTag: String) {
 setLocale(Locale.forLanguageTag(languageTag))
 }

 fun getLocale(): Locale = currentLocale

 fun loadMessages(locale: Locale) {
 if (messages.containsKey(locale)) {
 return
 }

 val localeMessages: MutableMap<String, String> = HashMap()
 val fileName = locale.toLanguageTag() + ".yml"
 val languagesFolder = File(dataFolder, LANGUAGES_FOLDER)

 val customFile = File(languagesFolder, fileName)
 if (customFile.exists()) {
 loadFromFile(customFile, localeMessages)
 }

 val resourcePath = "$LANGUAGES_FOLDER/$fileName"
 try {
 resourceProvider.getResource(resourcePath)?.use { defaultStream ->
 loadFromStream(defaultStream, localeMessages)
 }
 } catch (e: Exception) {
 e.printStackTrace()
 }

 if (localeMessages.isEmpty()) {
 try {
 resourceProvider.getResource("language.yml")?.use { fallbackStream ->
 loadFromStream(fallbackStream, localeMessages)
 }
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 try {
 messages[locale] = localeMessages
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 private fun loadYaml(reader: Reader, messages: MutableMap<String, String>) {
 val yaml = org.yaml.snakeyaml.Yaml()
 try {
 @Suppress("UNCHECKED_CAST")
 val data = yaml.load(reader) as? Map<String, Any>
 if (data != null) {
 flattenMap(messages, "", data)
 }
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 private fun loadFromFile(file: File, messages: MutableMap<String, String>) {
 try {
 InputStreamReader(java.io.FileInputStream(file), StandardCharsets.UTF_8).use { reader ->
 loadYaml(reader, messages)
 }
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 private fun loadFromStream(stream: InputStream, messages: MutableMap<String, String>) {
 try {
 InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
 loadYaml(reader, messages)
 }
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 @Suppress("UNCHECKED_CAST")
 private fun flattenMap(result: MutableMap<String, String>, prefix: String, map: Map<String, Any>) {
 for ((mapKey, value) in map) {
 val key = buildKey(prefix, mapKey)
 processValue(result, key, value)
 }
 }

 private fun buildKey(prefix: String, key: String): String =
 if (prefix.isEmpty()) key else "$prefix.$key"

 @Suppress("UNCHECKED_CAST")
 private fun processValue(result: MutableMap<String, String>, key: String, value: Any?) {
 if (value is Map<*, *>) {
 flattenMap(result, key, value as Map<String, Any>)
 return
 }
 if (value == null) {
 return
 }
 try {
 result[key] = value.toString()
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 private fun lookup(key: String): String? {
 if (currentLocale == null) {
 return null
 }
 val localeMessages = messages[currentLocale]
 if (localeMessages != null && localeMessages.containsKey(key)) {
 val value = localeMessages[key]
 return value?.let { translateColors(it) }
 }
 return null
 }

 fun get(key: String): String? {
 return try {
 val message = lookup(key)
 message ?: key
 } catch (e: Exception) {
 key
 }
 }

 fun get(key: String, vararg args: Any?): String? {
 return try {
 val message = get(key)
 if (args.isNotEmpty()) {
 MessageFormat.format(message, *args)
 } else {
 message
 }
 } catch (e: Exception) {
 key
 }
 }

 fun getOrDefault(key: String, defaultValue: String): String {
 val message = lookup(key)
 return message ?: defaultValue
 }

 fun setPlaceholder(key: String, value: Any) {
 try {
 placeholders[key] = value
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 fun removePlaceholder(key: String) {
 try {
 placeholders.remove(key)
 } catch (e: Exception) {
 e.printStackTrace()
 }
 }

 fun clearPlaceholders() {
 placeholders.clear()
 }

 fun translateColors(message: String?): String? {
 if (message == null) return null
 return message.replace(colorChar, '\u00A7')
 }

 fun setColorChar(colorChar: Char) {
 this.colorChar = colorChar
 }

 fun getColorChar(): Char = colorChar

 fun reload() {
 messages.clear()
 loadMessages(currentLocale)
 }

 companion object {
 lateinit var instance: I18n
 private const val LANGUAGES_FOLDER = "languages"

 @JvmStatic
 fun tr(key: String): String? = if (::instance.isInitialized) instance.get(key) else key

 @JvmStatic
 fun tr(key: String, vararg args: Any?): String? =
 if (::instance.isInitialized) instance.get(key, *args) else key
 }
}
