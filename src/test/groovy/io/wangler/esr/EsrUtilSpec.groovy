package io.wangler.esr

import spock.lang.Specification

import static EsrUtil.formatAccountNumber
import static EsrUtil.isAccountNumberValid
import static io.wangler.esr.EsrUtil.*
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static java.util.Currency.getInstance

/**
 * @author Silvio Wangler
 */
class EsrUtilSpec extends Specification {

    def "Interpret PostFinance ESR payment slips"(String code, BigDecimal amountValue, Currency currency, String accountNumber, String referenceNumber) {

        expect:
        def esr = scan code

        esr.amount.value == amountValue
        esr.amount.currency == currency
        esr.account == accountNumber
        esr.referenceNumber == referenceNumber

        where:
        code                                                    | amountValue           | currency           | accountNumber | referenceNumber
        '0100003949753>3139471430009343+ 010001628>'            | 3949.75 as BigDecimal | getInstance('CHF') | '01-162-8'    | '3139471430009343'
        '0100003949753>210000000003139471430009017+ 010001628>' | 3949.75 as BigDecimal | getInstance('CHF') | '01-162-8'    | '210000000003139471430009017'
        '2100000440001>961116900000006600000009284+ 030001625>' | 440.00 as BigDecimal  | getInstance('EUR') | '03-162-5'    | '961116900000006600000009284'
        '042>250000000000135678765455541+ 010001628>'           | null                  | getInstance('CHF') | '01-162-8'    | '250000000000135678765455541'
        '319>961116900000006600000009284+ 030001625>'           | null                  | getInstance('EUR') | '03-162-5'    | '961116900000006600000009284'
        '1100003949754>210000000003139471430009017+ 010001628>' | 3949.75 as BigDecimal | getInstance('CHF') | '01-162-8'    | '210000000003139471430009017'
        '2300000440009>961116900000006600000009284+ 030001625>' | 440.00 as BigDecimal  | getInstance('EUR') | '03-162-5'    | '961116900000006600000009284'
        '0100000104509>248869000154564400528736708+ 010040406>' | 104.50 as BigDecimal  | getInstance('CHF') | '01-4040-6'   | '248869000154564400528736708'
        '0100000185159>100017334134978110100690007+ 010233764>' | 185.15 as BigDecimal  | getInstance('CHF') | '01-23376-4'  | '100017334134978110100690007'
        '0100000002852>000000021009384900197001017+ 010438848>' | 2.85 as BigDecimal    | getInstance('CHF') | '01-43884-8'  | '000000021009384900197001017'
    }

    def "correct account numbers"(String actualAccountNumber, String expectedAccountNumber) {
        expect:
        formatAccountNumber(actualAccountNumber) == expectedAccountNumber

        where:

        actualAccountNumber | expectedAccountNumber
        '01-162-8'          | '01-000162-8'
        '87-580296-6'       | '87-580296-6'
        '1234'              | '1234'
        '60-4586-5'         | '60-004586-5'
    }

    def "validation PostFinance account numbers"(String postAccountNumber, Boolean expectedResult) {

        expect:

        isAccountNumberValid(postAccountNumber) == expectedResult

        where:

        postAccountNumber | expectedResult
        '87-580296-6'     | TRUE
        '87-584296-6'     | FALSE
        '01-000162-8'     | TRUE
        '01-162-8'        | TRUE
        '60-4586-5'       | TRUE
        '61-4586-5'       | FALSE
    }

    def "Validate reference number"(String refNumber, Boolean expectedResult) {

        expect:
        isReferenceNumberValid(refNumber) == expectedResult

        where:
        refNumber                     | expectedResult
        '000000000000000000999988885' | TRUE
    }

    def "Generate reference number with check digit"(String refNumber, String expectedResult) {
        expect:
        generateRefNumberWithCheckDigit(refNumber) == expectedResult

        where:
        refNumber          | expectedResult
        '99998888'         | '0000000999988885'
        '123333'           | '0000000001233331'
        '123'              | '0000000000001236'
        '7667667676765447' | '000000000076676676767654472'
    }

    def "Create ESR code line"(Currency currency, BigDecimal amount, String referenceNumber, String accountNumber, String expectedResult) {

        expect:
        generateCodeLine(new Amount(currency: currency, value: amount), referenceNumber, accountNumber) == expectedResult

        where:
        currency                    | amount                | referenceNumber                   | accountNumber | expectedResult
        Currency.getInstance('CHF') | 100.0 as BigDecimal   | '80 58140 00000 00000 05000 7200' | '01-001525-2' | '0100000100009>805814000000000000500072001+ 010015252>'
        Currency.getInstance('CHF') | 9995.60 as BigDecimal | '12345678901'                     | '01-001525-2' | '0100009995609>0000123456789017+ 010015252>'
    }
}
