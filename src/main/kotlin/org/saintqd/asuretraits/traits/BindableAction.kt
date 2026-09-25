package org.saintqd.asuretraits.traits

interface BindableAction {

    fun isBindable() : Boolean = true
    fun shouldCancelEvent() : Boolean = false
}