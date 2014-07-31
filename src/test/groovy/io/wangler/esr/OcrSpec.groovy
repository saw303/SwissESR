package io.wangler.esr

import spock.lang.Specification

/**
 * Created by SWangler on 31.07.2014.
 */
class OcrSpec extends Specification {

    def "read PostFinance payment slip code from OCR output"(InputStream inputStream, String esrCode) {
        given:
        def ocrInterpreter = new OcrInterpreter()

        expect:
        ocrInterpreter.findEsrCode(inputStream) == esrCode

        where:

        inputStream                         | esrCode
        getInputStream('ocr/simple.txt')    | '0100000002852>000000021889384900197001017+ 010438848>'
        getInputStream('ocr/multiline.txt') | '0100000002852>000000021009384900197001017+ 010438848>'
    }

    private InputStream getInputStream(String pathToFile) {
        return getClass().getClassLoader().getResourceAsStream(pathToFile)
    }
}
