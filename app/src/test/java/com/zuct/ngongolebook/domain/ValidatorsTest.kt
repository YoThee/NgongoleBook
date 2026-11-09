package com.zuct.ngongolebook.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun name_blank_isRejected() {
        assertNotNull(Validators.name("   "))
        assertNull(Validators.name("Mwila"))
    }

    @Test
    fun phone_isOptional_butChecked() {
        assertNull(Validators.phone(""))
        assertNull(Validators.phone("0977 123 456"))
        assertNull(Validators.phone("+260977123456"))
        assertNotNull(Validators.phone("12345"))
        assertNotNull(Validators.phone("0877123456"))
    }

    @Test
    fun item_cannotBeBlank() {
        assertNotNull(Validators.item("  "))
        assertNull(Validators.item("2kg sugar"))
    }

    @Test
    fun amount_mustBePositiveNumber() {
        assertNull(Validators.amount("150"))
        assertNull(Validators.amount("1,250.50"))
        assertNotNull(Validators.amount(""))
        assertNotNull(Validators.amount("abc"))
        assertNotNull(Validators.amount("0"))
        assertNotNull(Validators.amount("-5"))
        assertNotNull(Validators.amount("NaN"))
    }

    @Test
    fun dueDate_cannotBeMissingOrPast() {
        val today = 1_000_000_000L
        assertNotNull(Validators.dueDate(null, today))
        assertNotNull(Validators.dueDate(today - 1, today))
        assertNull(Validators.dueDate(today, today))
    }

    @Test
    fun payment_cannotExceedBalance() {
        assertNull(Validators.payment("50", 100.0))
        assertNull(Validators.payment("100", 100.0))
        assertNotNull(Validators.payment("100.50", 100.0))
        assertNotNull(Validators.payment("", 100.0))
    }

    @Test
    fun parseAmount_handlesCommas() {
        assertEquals(1250.5, Validators.parseAmount("1,250.50")!!, 0.0001)
    }
}
