package ru.blays.hub.core.domain.data

import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A value class representing a localized message with support for retrieving messages in different languages.
 *
 * Xml sample:
 * ```xml
 * <messages>
 *     <en default="true">Some text</en>
 *     <ru>Какой-то текст</ru>
 * </messages>
 *  ```
 *
 * @property document The XML document containing the localized messages.
 * @constructor Creates a LocalizedMessage instance from a Document.
 */
@JvmInline
value class LocalizedMessage(private val document: Document?) {
    /**
     * Creates a LocalizedMessage instance from an XML string containing the localized messages.
     *
     * @param messageXml The XML string containing the localized messages.
     */
    constructor(messageXml: String): this(
        kotlin.runCatching {
            messageXml.byteInputStream().use { stream ->
                DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(stream)
            }
        }.getOrNull()
    )

    /**
     * Creates a LocalizedMessage instance from an InputStream containing the localized messages.
     *
     * @param inputStream The InputStream containing the localized messages.
     */
    constructor(inputStream: InputStream): this(
        kotlin.runCatching {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
        }.getOrNull()
    )

    /**
     * Indicates whether the document valid and contains root element
     *
     * @return True if the document valid, false otherwise.
     */
    val isValid: Boolean
        get() {
            if(document == null) return false
            return document.getElementsByTagName(ROOT_ELEMENT_NAME).isNotEmpty
        }

    /**
     * Retrieves the map of messages for each language.
     *
     * @return A map where the key is the language code and the value is the corresponding message.
     */
    val messages: Map<String, String>
        get() = kotlin.runCatching {
            messageNodes.associateBy(
                keySelector = Node::getNodeName,
                valueTransform = Node::getTextContent
            )
        }.getOrElse {
            emptyMap()
        }

    /**
     * Retrieves the message for the specified language.
     *
     * @param language The language code for the desired message.
     * @return The message corresponding to the specified language.
     * @throws DOMException If there is an error accessing the DOM.
     * @throws NullPointerException If the element corresponding to the specified language is null.
     */
    @Throws(DOMException::class, NullPointerException::class)
    fun getForLanguage(language: String): String =
        document!!.getElementsByTagName(language)
            .first
            .textContent
            .trimIndent()

    /**
     * Retrieves the message for the specified language, or returns null if the message is not available.
     *
     * @param language The language code for the desired message.
     * @return The message corresponding to the specified language, or null if the message is not available.
     */
    fun getForLanguageOrNull(language: String): String? = kotlin.runCatching {
        getForLanguage(language)
    }.getOrNull()

    /**
     * Retrieves the message for the specified language, or invokes the provided function if the message is not available.
     *
     * @param language The language code for the desired message. If the message is not available in the specified language,
     *                the provided function will be invoked instead.
     * @param onFailure The function to invoke if the message is not available in the specified language.
     * @return The message corresponding to the specified language, or the result of invoking the provided function.
     */
    fun getForLanguageOrElse(
        language: String,
        onFailure: () -> String
    ): String = kotlin.runCatching {
        getForLanguage(language)
    }.getOrElse { onFailure() }

    /**
     * Retrieves the message for the specified language, or returns a  message for default language if it is not available.
     *
     * @param language The language code for the desired message. If the message is not available in the specified language,
     *                the default language will be used instead.
     * @return The message corresponding to the specified language, or null if the message is not available.
     */
    fun getForLanguageOrDefault(language: String): String? = kotlin.runCatching {
        getForLanguage(language)
    }.getOrElse {
        kotlin.runCatching {
            messageNodes.asSequence()
                .first { node ->
                    node.attributes
                        .getNamedItem(ATTR_DEFAULT_NAME)
                        ?.nodeValue
                        ?.toBooleanStrictOrNull()
                        ?: false
                }
                .textContent
        }.getOrNull()
    }

    @get:Throws(DOMException::class, NullPointerException::class)
    private val messageNodes: NodeList
        get() = document!!.getElementsByTagName(ROOT_ELEMENT_NAME).first.childNodes

    private inline val NodeList.first: Node
        get() = item(0)

    private inline val NodeList.isEmpty: Boolean
        get() = length == 0

    private inline val NodeList.isNotEmpty: Boolean
        get() = length > 0

    private fun NodeList.asSequence(): Sequence<Node> {
        return (0 until length).asSequence().map(::item)
    }

    private inline fun NodeList.associateBy(
        keySelector: (Node) -> String,
        valueTransform: (Node) -> String
    ): Map<String, String> {
        return asSequence()
            .associateBy(
                keySelector = keySelector,
                valueTransform = valueTransform
            )
    }

    private fun NamedNodeMap.hasNodeWithName(name: String): Boolean {
        return getNamedItem(name) != null
    }

    companion object {
        private const val ROOT_ELEMENT_NAME = "messages"
        private const val ATTR_DEFAULT_NAME = "default"
    }
}
