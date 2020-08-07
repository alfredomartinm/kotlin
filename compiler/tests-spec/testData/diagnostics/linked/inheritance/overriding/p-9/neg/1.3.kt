// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 1.4-rfc+0.3-591
 * MAIN LINK: inheritance, overriding -> paragraph 9 -> sentence 1
 * PRIMARY LINKS: inheritance, overriding -> paragraph 7 -> sentence 1
 * inheritance, overriding -> paragraph 2 -> sentence 1
 * inheritance, overriding -> paragraph 2 -> sentence 2
 * inheritance, overriding -> paragraph 4 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: overriding member hashCode function of open class
 */

// TESTCASE NUMBER: 1
open class BaseCase1(val a: Int, val b: CharSequence)

open class ChildCase1 : BaseCase1(1, "") {
    fun <!VIRTUAL_MEMBER_HIDDEN!>hashCode<!>(): Int = TODO() //(1)
}

// TESTCASE NUMBER: 2
open class BaseCase2(val a: Int, val b: CharSequence) {
    open fun <!VIRTUAL_MEMBER_HIDDEN!>hashCode<!>(): Int = TODO() //(0)
}

open class ChildCase2 : BaseCase2(1, "") {
    open fun <!VIRTUAL_MEMBER_HIDDEN!>hashCode<!>(): Int = TODO() //(1)
}

// TESTCASE NUMBER: 3
open class BaseCase3(val a: Int, val b: CharSequence) {
    fun <!VIRTUAL_MEMBER_HIDDEN!>hashCode<!>(): Int = TODO() //(0)
}

open class ChildCase3 : BaseCase3(1, "") {
    fun <!VIRTUAL_MEMBER_HIDDEN!>hashCode<!>(): Int = TODO() //(1)
}