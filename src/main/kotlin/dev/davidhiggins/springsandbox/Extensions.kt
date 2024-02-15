package dev.davidhiggins.springsandbox

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import java.nio.charset.StandardCharsets

typealias ResourceName = String

object Extensions {

    fun String.sanitize(safelist: Safelist = Safelist.relaxed()) =
        Jsoup.clean(this, "", safelist, Document.OutputSettings().prettyPrint(false) )


    fun ResourceName.readAsResource(): String? =
        Application::class.java.getResource(this)
            ?.readText(StandardCharsets.UTF_8)

}