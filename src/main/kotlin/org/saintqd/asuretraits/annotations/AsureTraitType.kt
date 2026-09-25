package org.saintqd.asuretraits.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AsureTraitType(
    val name : String = ""
)
