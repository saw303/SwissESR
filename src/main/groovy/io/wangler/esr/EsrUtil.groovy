package io.wangler.esr

/**
 * @author Silvio Wangler
 */
class EsrUtil {

    /*
    https://www.postfinance.ch/binp/postfinance/public/dam.7WVz5HV3I7Lnw9nUa0-i4Zv-lMxIR4ilETelHK5VeCo.spool/content/dam/pf/de/doc/consult/manual/dldata/efin_recdescr_man_de.pdf
    http://www.wins.ch/esr01.html
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

        def number = String.format('%1$026d', referenceNumberWithoutCheckDigit.toInteger())

        number.eachWithIndex { potentialNumber, index ->
            if (potentialNumber.isInteger()) {
                def sum = potentialNumber.toInteger() + mapIndex
                mapIndex = list[sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10

        return "${number}${checkDigit}".toString()
    }
}
