package org.milad.expense_share

/**
 * Normalizes Eastern Arabic (Persian and Arabic) numerals into standard Western ASCII digits.
 */
fun String.normalizePersianDigits(): String = this
    .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
    .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
    .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
    .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
