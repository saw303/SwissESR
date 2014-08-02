package io.wangler.esr

/**
 * @author Silvio Wangler
 * @since 0.3
 */
class EsrUtil {

    //https://www.postfinance.ch/binp/postfinance/public/dam.7WVz5HV3I7Lnw9nUa0-i4Zv-lMxIR4ilETelHK5VeCo.spool/content/dam/pf/de/doc/consult/manual/dldata/efin_recdescr_man_de.pdf

    private static final map = [
            0: [0, 9, 4, 6, 8, 2, 7, 1, 3, 5, 0],
            1: [9, 4, 6, 8, 2, 7, 1, 3, 5, 0, 9],
            2: [4, 6, 8, 2, 7, 1, 3, 5, 0, 9, 8],
            3: [6, 8, 2, 7, 1, 3, 5, 0, 9, 4, 7],
            4: [8, 2, 7, 1, 3, 5, 0, 9, 4, 6, 6],
            5: [2, 7, 1, 3, 5, 0, 9, 4, 6, 8, 5],
            6: [7, 1, 3, 5, 0, 9, 4, 6, 8, 2, 4],
            7: [1, 3, 5, 0, 9, 4, 6, 8, 2, 7, 3],
            8: [3, 5, 0, 9, 4, 6, 8, 2, 7, 1, 2],
            9: [5, 0, 9, 4, 6, 8, 2, 7, 1, 3, 1]
    ]

    static boolean isAccountNumberValid(final String postFinanceAccountNumber) {

        def mapIndex = 0
        def processedIntegers = 0

        def copy = postFinanceAccountNumber

        if (copy.length() != 10) {
            copy = formatAccountNumber(copy)
        }

        copy.eachWithIndex { character, index ->

            if (character.isInteger() && processedIntegers < 8) {
                mapIndex = map[mapIndex][character as int]
                processedIntegers++
            }
        }

        if (processedIntegers == 8) {
            return copy.endsWith(map[mapIndex][10] as String)
        }
        return false
    }

    static String formatAccountNumber(String accountNumber) {
        if (isValidAccountNumberFormat(accountNumber)) {
            def tokens = accountNumber.split('-')
            return String.format('%1$02d-%2$06d-%3$d', tokens[0] as int, tokens[1] as int, tokens[2] as int)
        }
        return accountNumber
    }

    private static boolean isValidAccountNumberFormat(String accountNumber) {
        return accountNumber ==~ /\d{1,2}-\d{1,6}-\d/
    }

    static boolean isReferenceNumberValid(String referenceNumber) {

        def mapIndex = 0

        referenceNumber.eachWithIndex { potentialNumber, index ->
            if (potentialNumber.isInteger() && index < referenceNumber.length() - 1) {
                mapIndex = map[mapIndex][potentialNumber as int]
            }
        }

        return referenceNumber.endsWith(mapIndex as String)
    }

    static String generateRefNumberWithCheckDigit(String referenceNumberWithoutCheckDigit) {
        def mapIndex = 0

        def number = String.format('%1$026d', referenceNumberWithoutCheckDigit.toInteger())

        number.eachWithIndex { potentialNumber, index ->
            if (potentialNumber.isInteger()) {
                def sum = potentialNumber.toInteger() + mapIndex
                mapIndex = map[0][sum % 10]
            }
        }

        def final checkDigit = (10 - mapIndex) % 10

        return "${number}${checkDigit}".toString()
    }
}
