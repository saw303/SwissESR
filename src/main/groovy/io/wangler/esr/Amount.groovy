package io.wangler.esr

import groovy.transform.Canonical

@Canonical
class Amount {
    Currency currency
    BigDecimal value
}
