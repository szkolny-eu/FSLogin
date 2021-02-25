package pl.szczodrzynski.fslogin.response

import pl.droidsonroids.jspoon.annotation.Selector

class FSCertificateResponse {

    @Selector("title")
    var pageTitle: String = ""

    @Selector("#ErrorTextLabel")
    var errorTextAdfs: String = ""

    @Selector(".ErrorMessage")
    var errorTextCufs: String = ""

    @Selector("form[name=hiddenform]", attr = "action")
    var formAction: String = ""

    @Selector("input[name=wa]", attr = "value")
    var wa: String = ""

    @Selector("input[name=wresult]", attr = "value")
    var wresult: String = ""

    @Selector("input[name=wctx]", attr = "value")
    var wctx: String = ""

    val isValid = pageTitle.startsWith("Working...")
}
