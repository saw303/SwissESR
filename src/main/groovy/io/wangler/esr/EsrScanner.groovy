package io.wangler.esr

import java.util.regex.Matcher

/**
 * https://www.postfinance.ch/binp/postfinance/public/dam.lV50-NaX1dZO8RpqAVs3sX6Qx3icDH6LOvV7N-uGY2w.spool/content/dam/pf/de/doc/consult/manual/dlserv/inpayslip_isr_man_de.pdf
 * http://www.techzoom.net/tools/payment-order-encoder.en
 */
class EsrScanner {
    static EsrPlus scan(String esrCodeLine) {

        def slipType = esrCodeLine.substring(0, 2)
        def amount = esrCodeLine.substring(2, 12)
        def amountValue = (amount as BigDecimal) / 100

        def refNumber = retrieveReferenceNumber(esrCodeLine)
        def accountNumber = retrieveAccountNumber(esrCodeLine)

        def currency = slipType =~ '21' ? Currency.getInstance('EUR') : Currency.getInstance('CHF')

        def esr = new Esr(
                amount: new Amount(currency: currency, value: amountValue),
                referenceNumber: refNumber,
                // http://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html
                account: String.format('%1$02d-%2$d-%3$d',
                        accountNumber.substring(0, 2) as Integer,
                        accountNumber.substring(2, 8) as Integer,
                        accountNumber.substring(8) as Integer)
        )
        return esr
    }

    private static String retrieveReferenceNumber(final String esrCodeLine) {
        def referenceNumber = retrieveSubString(esrCodeLine, esrCodeLine =~ />(.*)\+/)
        return referenceNumber
    }

    private static String retrieveAccountNumber(final String esrCodeLine) {
        def accountNumber = retrieveSubString(esrCodeLine, esrCodeLine =~ /\+(.*)>/).trim()
        return accountNumber
    }

    private static String retrieveSubString(final String esrCodeLine, Matcher regexMatcher) {
        if (regexMatcher.find()) {
            return regexMatcher.group().replaceAll(">", "").replaceAll("\\+", "")
        }
        return null
    }
}
