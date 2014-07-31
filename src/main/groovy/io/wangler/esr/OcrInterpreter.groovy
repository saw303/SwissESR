package io.wangler.esr

import java.util.regex.Matcher

/**
 * Created by SWangler on 31.07.2014.
 */
class OcrInterpreter {
    String findEsrCode(InputStream inputStream) {
        def content = inputStream.text

        Matcher matcher = content =~ /\d*>\d*\+ \d*>/
        if (matcher.find()) {
            return  matcher.group()
        }

        StringBuilder sb = new StringBuilder()
        matcher = content =~ /\d*>\d*\+/
        if (matcher.find()) {
            sb << matcher.group()
            sb << ' '
        }
        else {
            return null
        }

        matcher = content =~ /\d*>/

        while (matcher.find()) {

            def line = matcher.group()

            if (! sb.contains(line)) {
                sb << line
                return sb.toString()
            }
        }
        return null
    }
}
