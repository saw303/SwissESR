package io.wangler.esr

import java.util.regex.Matcher

/**
 * @author Silvio Wangler
 */
class EsrUtil {

    /*
    * https://www.postfinance.ch/binp/postfinance/public/dam.7WVz5HV3I7Lnw9nUa0-i4Zv-lMxIR4ilETelHK5VeCo.spool/content/dam/pf/de/doc/consult/manual/dldata/efin_recdescr_man_de.pdf
    * http://www.wins.ch/esr01.html
    *
    * https://www.postfinance.ch/binp/postfinance/public/dam.lV50-NaX1dZO8RpqAVs3sX6Qx3icDH6LOvV7N-uGY2w.spool/content/dam/pf/de/doc/consult/manual/dlserv/inpayslip_isr_man_de.pdf
    * http://www.techzoom.net/tools/payment-order-encoder.en
    */

    private static final list = [0, 9, 4, 6, 8, 2, 7, 1, 3, 5]

    static boolean isAccountNumberValid(final String postFinanceAccountNumber) {

        def mapIndex = 0
        def processedIntegers = 0

        def copy = postFinanceAccountNumber

        if (copy.length() != 10) {
            copy = formatAccountNumber(copy)
        }

        copy.eachWithIndex { character, index ->

            if (character.isInteger() && processedIntegers < 8) {
                mapIndex = list[((mapIndex + character.toInteger()) % 10)]
                processedIntegers++
            }
        }

        if (processedIntegers == 8) {
            return copy.endsWith(((10 - mapIndex) % 10) as String)
        }
        return false
    }

    static String formatAccountNumber(String accountNumber) {
        if (isValidAccountNumberFormat(accountNumber)) {
            def tokens = accountNumber.split('-')
            return String.format('%1$02d-%2$06d-%3$d', tokens[0].toInteger(), tokens[1].toInteger(), tokens[2].toInteger())
        }
        return accountNumber
    }

    private static boolean isValidAccountNumberFormat(String accountNumber) {
        return accountNumber ==~ /\d{1,2}-\d{1,6}-\d/
    }

    static boolean isReferenceNumberValid(String referenceNumberContainingCheckDigit) {
        def refNumberWithoutCheckDigit = referenceNumberContainingCheckDigit.substring(0, referenceNumberContainingCheckDigit.length() - 1)
        return generateRefNumberWithCheckDigit(refNumberWithoutCheckDigit) == referenceNumberContainingCheckDigit
    }

    static String generateRefNumberWithCheckDigit(String referenceNumberWithoutCheckDigit) {
        def mapIndex = 0

        def number = String.format('%1$026d', referenceNumberWithoutCheckDigit.toBigInteger())

        number.eachWithIndex { String potentialNumber, int index ->
            if (potentialNumber.isNumber()) {
                def sum = potentialNumber.toBigInteger() + mapIndex
                mapIndex = list[sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10

        return "${number}${checkDigit}".toString()
    }

    static Esr scan(String esrCodeLine) {

        def esr = new Esr()
        def slipType = esrCodeLine.substring(0, 2)

        def currency = isEuroPaymentSlip(slipType) ? Currency.getInstance('EUR') : Currency.getInstance('CHF')

        if (paymentContainsAmount(slipType)) {
            // payment slips containing the payment amount
            def amount = esrCodeLine.substring(2, 12)
            def amountValue = (amount as BigDecimal) / 100
            esr.amount = new Amount(currency: currency, value: amountValue)
        } else {
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

    static def generateCodeLine(Amount amount, String refNumber, String accountNumber) {

        def seg1 = "01${String.format('%1$010d', amount.value.multiply(100 as BigDecimal).toInteger())}"
        def mapIndex = 0
        seg1.eachWithIndex { String potentialNumber, int index ->
            if (potentialNumber.isNumber()) {
                def sum = potentialNumber.toBigInteger() + mapIndex
                mapIndex = list[sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10
        seg1 = "${seg1}${checkDigit}"


        def seg2 = generateRefNumberWithCheckDigit(refNumber.replaceAll(' ', ''))
        def seg3 = "${accountNumber.replaceAll('-','')}"

        "${seg1}>${seg2}+ ${seg3}>"
    }
}
