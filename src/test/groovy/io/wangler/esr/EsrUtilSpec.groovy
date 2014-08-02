package io.wangler.esr

import spock.lang.Specification

import static EsrUtil.formatAccountNumber
import static EsrUtil.isAccountNumberValid
import static io.wangler.esr.EsrUtil.generateRefNumberWithCheckDigit
import static io.wangler.esr.EsrUtil.isReferenceNumberValid
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE

/**
 * @author Silvio Wangler
 */
class EsrUtilSpec extends Specification {

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
        refNumber  | expectedResult
        '99998888' | '000000000000000000999988885'
        '123333'   | '000000000000000000001233331'
        '123'      | '000000000000000000000001236'
    }
}
