package io.wangler.esr

import java.util.regex.Matcher

/**
 * https://www.postfinance.ch/binp/postfinance/public/dam.lV50-NaX1dZO8RpqAVs3sX6Qx3icDH6LOvV7N-uGY2w.spool/content/dam/pf/de/doc/consult/manual/dlserv/inpayslip_isr_man_de.pdf
 * http://www.techzoom.net/tools/payment-order-encoder.en
 */
class EsrScanner {
    static Esr scan(String esrCodeLine) {

        def esr = new Esr()
        def slipType = esrCodeLine.substring(0, 2)

        def currency = isEuroPaymentSlip(slipType) ? Currency.getInstance('EUR') : Currency.getInstance('CHF')

        if (paymentContainsAmount(slipType)) {
            // payment slips containing the payment amount
            def amount = esrCodeLine.substring(2, 12)
            def amountValue = (amount as BigDecimal) / 100
            esr.amount = new Amount(currency: currency, value: amountValue)
        }
        else {
            esr.amount = new Amount(currency: currency, value: null)
        }

        def refNumber = retrieveReferenceNumber(esrCodeLine)
        def accountNumber = retrieveAccountNumber(esrCodeLine)

        esr.referenceNumber = refNumber
        esr.account = String.format('%1$02d-%2$d-%3$d',
                accountNumber.substring(0, 2) as Integer,
                accountNumber.substring(2, 8) as Integer,
                accountNumber.substring(8) as Integer)

        return esr
    }

    private static Matcher paymentContainsAmount(String slipType) {
        slipType =~ /(01|11|21|23)/
    }

    private static Matcher isEuroPaymentSlip(String slipType) {
        slipType =~ '(21|23|31)'
    }

    private static String retrieveReferenceNumber(final String esrCodeLine) {
        def referenceNumber = retrieveSubString(esrCodeLine =~ />(\d*)\+/)
        return referenceNumber
    }

    private static String retrieveAccountNumber(final String esrCodeLine) {
        def accountNumber = retrieveSubString(esrCodeLine =~ /\+ (\d*)>/).trim()
        return accountNumber
    }

    private static String retrieveSubString(Matcher regexMatcher) {
        if (regexMatcher.find()) {
            return regexMatcher.group().replaceAll(">", "").replaceAll("\\+", "")
        }
        throw new RuntimeException('Did not match')
    }
}
