# FSLogin
ADFS &amp; Vulcan CUFS Login module

## Usage

#### Install the library dependency.
```
dependencies {
    implementation 'com.github.kuba2k2.FSLogin:lib:<version>'
}
```
You can check the version on the GitHub releases page, or use `master-SNAPSHOT` for
the latest version (not recommended though, may be unstable).

#### Create a Realm.
Make sure that the URL (toString()) matches the normal form URL you would get in a web browser.

Some examples:
```kotlin
// CUFS
CufsRealm(host = "vulcan.net.pl", symbol = "default")
CufsRealm(host = "fakelog.cf", symbol = "powiatwulkanowy")
// ADFS
CufsRealm(host = "umt.tarnow.pl", symbol = "tarnow").toAdfsRealm(id = "adfs", authType = authType)
CufsRealm(host = "edu.gdansk.pl", symbol = "gdansk").toAdfsRealm(id = "adfs", authType = authType)
CufsRealm(host = "eszkola.opolskie.pl", symbol = "opole", httpCufs = true).toAdfsRealm(id = "eSzkola", authType = authType)
/* https://uonetplus.eszkola.opolskie.pl/brzeg/BrzegG1 */
CufsRealm(host = "eszkola.opolskie.pl", symbol = "brzeg", httpCufs = true, realmPath = "brzegg1/LoginEndpoint.aspx").toAdfsRealm(id = "eSzkola", authType = authType)
// ADFS LIGHT
CufsRealm(host = "resman.pl", symbol = "rzeszow").toAdfsLightRealm(id = "ADFS")
CufsRealm(host = "edu.lublin.eu", symbol = "lublin").toAdfsLightRealm(id = "AdfsLight", domain = "logowanie")
// ADFS LIGHT - scoped
CufsRealm(host = "vulcan.net.pl", symbol = "powiatketrzynski").toAdfsLightRealm(id = "ADFSLight", isScoped = true)
// IUCZNIOWIE
AdfsRealm(hostPrefix = "iuczniowie", host = "eduportal.koszalin.pl", path = "Default.aspx", id = "passive", authType = authType)
AdfsRealm(hostPrefix = "iuczniowie", host = "eszkola.opolskie.pl", path = "Default.aspx", id = "passive", authType = authType)
// LIBRUS SSO
AdfsLightRealm(hostPrefix = "synergia", host = "librus.pl", adfsHost = "oswiatawradomiu.pl", path = "loguj/radom", id = "passive")
```

#### Log in
In order to get a certificate, create an instance of `FSLogin`.
```kotlin
val http = OkHttpClient.Builder()
                // you need to have a working cookie jar
                .cookieJar(MyCookieJar())
                // to support most of the realms, as some are using insecure TLS
                .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS))
                .build()
val fsLogin = FSLogin(http, debug = true)
```
Then just call `fsLogin.performLogin(realm, username, password, onSuccess, onFailure)`:
```kotlin
fsLogin.performLogin(
    realm = CufsRealm(host = "fakelog.cf", symbol = "powiatwulkanowy"),
    username = "jan@fakelog.cf",
    password = "jan123",
    onSuccess = { certificate -> 

    },
    onFailure = { errorText ->

    }
)
```

#### Final step
A certificate retrieved in `onSuccess` may be `POST`ed to `realm.getFinalRealm()` to complete the login process:
```kotlin
// returns an HTML body of the website
val result = fsLogin.api.postCertificate(certificate.formAction, mapOf(
    "wa" to certificate.wa,
    "wresult" to certificate.wresult,
    "wctx" to certificate.wctx
)).execute().body()
```
You can use a custom method for posting those form fields.

### Sample code
Check the [`sample` module](https://github.com/kuba2k2/FSLogin/blob/master/sample/src/main/kotlin/pl/szczodrzynski/fslogin/Main.kt).

## Special thanks to
[Wulkanowy's SDK library](https://github.com/wulkanowy/sdk) for a general idea
about how all this works and for testing the library by their great community.

Provided under [Apache License 2.0](https://github.com/wulkanowy/sdk/blob/master/LICENSE).

## License
```
MIT License

Copyright (c) 2020 kuba2k2

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
Use whatever you want, but please include a copyright notice :)
