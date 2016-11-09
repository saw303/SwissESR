package io.wangler.esr

import groovy.transform.CompileStatic

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

    @CompileStatic
    private static final List<Integer> getList() {
        return [0, 9, 4, 6, 8, 2, 7, 1, 3, 5]
    }

    static boolean isAccountNumberValid(final String postFinanceAccountNumber) {

        def mapIndex = 0
        def processedIntegers = 0

        def copy = postFinanceAccountNumber

        if (copy.length() != 10) {
            copy = formatAccountNumber(copy)
        }

        copy.eachWithIndex { character, index ->

            if (character.isInteger() && processedIntegers < 8) {
                mapIndex = getList()[((mapIndex + character.toInteger()) % 10)]
                processedIntegers++
            }
        }

        if (processedIntegers == 8) {
            return copy.endsWith(((10 - mapIndex) % 10) as String)
        }
        return false
    }

    @CompileStatic
    static String formatAccountNumber(String accountNumber) {
        if (isValidAccountNumberFormat(accountNumber)) {
            def tokens = accountNumber.split('-')
            return String.format('%1$02d-%2$06d-%3$d', tokens[0].toInteger(), tokens[1].toInteger(), tokens[2].toInteger())
        }
        return accountNumber
    }

    @CompileStatic
    private static boolean isValidAccountNumberFormat(String accountNumber) {
        return accountNumber ==~ /\d{1,2}-\d{1,6}-\d/
    }

    @CompileStatic
    static boolean isReferenceNumberValid(String referenceNumberContainingCheckDigit) {
        def refNumberWithoutCheckDigit = referenceNumberContainingCheckDigit.substring(0, referenceNumberContainingCheckDigit.length() - 1)
        return generateRefNumberWithCheckDigit(refNumberWithoutCheckDigit) == referenceNumberContainingCheckDigit
    }

    static String generateRefNumberWithCheckDigit(String referenceNumberWithoutCheckDigit) {
        def mapIndex = 0

        def number = String.format(referenceNumberWithoutCheckDigit.length() <=15 ? '%1$015d' :  '%1$026d', referenceNumberWithoutCheckDigit.toBigInteger())

        number.eachWithIndex { String potentialNumber, int index ->
            if (potentialNumber.isNumber()) {
                def sum = potentialNumber.toBigInteger() + mapIndex
                mapIndex = getList()[sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10

        return "${number}${checkDigit}".toString()
    }

    @CompileStatic
    static Esr scan(String esrCodeLine) {

        Esr esr = new Esr()
        String slipType = esrCodeLine.substring(0, 2)

        Currency currency = isEuroPaymentSlip(slipType) ? Currency.getInstance('EUR') : Currency.getInstance('CHF')

        if (paymentContainsAmount(slipType)) {
            // payment slips containing the payment amount
            String amount = esrCodeLine.substring(2, 12)
            BigDecimal amountValue = (amount as BigDecimal).divide(100 as BigDecimal)
            esr.amount = new Amount(currency, amountValue)
        } else {
            esr.amount = new Amount(currency, null)
        }

        String refNumber = retrieveReferenceNumber(esrCodeLine)
        String accountNumber = retrieveAccountNumber(esrCodeLine)

        esr.referenceNumber = refNumber
        esr.account = String.format('%1$02d-%2$d-%3$d',
                accountNumber.substring(0, 2) as Integer,
                accountNumber.substring(2, 8) as Integer,
                accountNumber.substring(8) as Integer)

        return esr
    }

    @CompileStatic
    private static Matcher paymentContainsAmount(String slipType) {
        slipType =~ /(01|11|21|23)/
    }

    @CompileStatic
    private static Matcher isEuroPaymentSlip(String slipType) {
        slipType =~ '(21|23|31)'
    }

    @CompileStatic
    private static String retrieveReferenceNumber(final String esrCodeLine) {
        println esrCodeLine
        String referenceNumber = retrieveSubString(esrCodeLine =~ />(\d*)\+/)
        return referenceNumber
    }

    @CompileStatic
    private static String retrieveAccountNumber(final String esrCodeLine) {
        String accountNumber = retrieveSubString(esrCodeLine =~ /\+ (\d*)>/).trim()
        return accountNumber
    }

    @CompileStatic
    private static String retrieveSubString(Matcher regexMatcher) {
        if (regexMatcher.find()) {
            return regexMatcher.group().replaceAll(">", "").replaceAll("\\+", "")
        }
        throw new RuntimeException('Did not match')
    }

    @CompileStatic
    static String generateCodeLine(Amount amount, String refNumber, String accountNumber) {

        String seg1 = "01${String.format('%1$010d', amount.value.multiply(100 as BigDecimal).toInteger())}"
        int mapIndex = 0
        seg1.eachWithIndex { String potentialNumber, int index ->
            if (potentialNumber.isNumber()) {
                BigInteger sum = potentialNumber.toBigInteger().add(mapIndex as BigInteger)
                mapIndex = getList()[sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10
        seg1 = "${seg1}${checkDigit}"


        def seg2 = generateRefNumberWithCheckDigit(refNumber.replaceAll(' ', ''))
        def seg3 = "${accountNumber.replaceAll('-','')}"

        "${seg1}>${seg2}+ ${seg3}>"
    }
}
