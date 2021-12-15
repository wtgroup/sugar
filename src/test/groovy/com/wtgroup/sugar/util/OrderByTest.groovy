package com.wtgroup.sugar.util


import com.wtgroup.sugar.enums.CaseTransform
import spock.lang.Specification
import spock.lang.Unroll

class OrderByTest extends Specification {

    @Unroll
    def "config Case Transform where caseTransform=#caseTransform then expect: #expectedResult"() {
        def segment = OrderBy.of().configCaseTransform(caseTransform).orderBy(col, asc).orderSegment()
        expect:
        println segment
        segment == expectedResult

        where:
        caseTransform       | col          | asc   || expectedResult
        CaseTransform.LC2LU | "colOfCamel" | false || "col_of_camel DESC"
        CaseTransform.LU2LC | "col_of_camel" | false || "colOfCamel DESC"
        CaseTransform.LU2UC | "col_of_camel" | false || "ColOfCamel DESC"
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme