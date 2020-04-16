package pl.szczodrzynski.fslogin.response

import pl.droidsonroids.jspoon.annotation.Selector

class ADFSFormResponse {

    @Selector("title")
    var pageTitle: String = ""

    @Selector("#__VIEWSTATE", attr = "value")
    var viewState: String = ""

    @Selector("#__VIEWSTATEGENERATOR", attr = "value")
    var viewStateGenerator: String = ""

    @Selector("#__EVENTVALIDATION", attr = "value")
    var eventValidation: String = ""
}
