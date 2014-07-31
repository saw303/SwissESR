package io.wangler.esr

import spock.lang.Specification

import static io.wangler.esr.EsrScanner.scan
import static java.util.Currency.getInstance

/**
 * Created by SWangler on 30.07.2014.
 */
class EsrSpec extends Specification {

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
}
