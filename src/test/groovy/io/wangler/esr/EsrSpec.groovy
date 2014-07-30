package io.wangler.esr

import spock.lang.Specification

import static io.wangler.esr.EsrScanner.scan

/**
 * Created by SWangler on 30.07.2014.
 */
class EsrSpec extends Specification {

    def "scan a CHF slip (ESR)"(String code, BigDecimal amountValue, Currency currency, String accountNumber, String referenceNumber, Class esrClass) {

        expect:
        def esr = scan code

        esr.class == esrClass
        esr.amount.value == amountValue
        esr.amount.currency == currency
        esr.account == accountNumber
        esr.referenceNumber == referenceNumber

        where:
        code                                                    | amountValue           | currency                    | accountNumber | referenceNumber               | esrClass
        '0100003949753>3139471430009343+ 010001628>'            | 3949.75 as BigDecimal | Currency.getInstance('CHF') | '01-162-8'    | '3139471430009343'            | Esr
        '0100003949753>210000000003139471430009017+ 010001628>' | 3949.75 as BigDecimal | Currency.getInstance('CHF') | '01-162-8'    | '210000000003139471430009017' | Esr
        '2100000440001>961116900000006600000009284+ 030001625>' | 440.00 as BigDecimal  | Currency.getInstance('EUR') | '03-162-5'    | '961116900000006600000009284' | Esr
    }
}
