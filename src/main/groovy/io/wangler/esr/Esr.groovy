package io.wangler.esr

import groovy.transform.Canonical

/**
 * Created by SWangler on 30.07.2014.
 */
@Canonical
class Esr {
    Amount amount
    String account
    String referenceNumber
}
