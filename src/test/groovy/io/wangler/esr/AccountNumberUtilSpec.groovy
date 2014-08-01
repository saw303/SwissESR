package io.wangler.esr

import spock.lang.Specification

import static io.wangler.esr.AccountNumberUtil.formatAccountNumber
import static io.wangler.esr.AccountNumberUtil.isValid
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE

/**
 * @author Silvio Wangler
 */
class AccountNumberUtilSpec extends Specification {

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

        isValid(postAccountNumber) == expectedResult

        where:

        postAccountNumber | expectedResult
        '87-580296-6'     | TRUE
        '87-584296-6'     | FALSE
        '01-000162-8'     | TRUE
        '01-162-8'        | TRUE
        '60-4586-5'       | TRUE
        '61-4586-5'       | FALSE
    }
}
