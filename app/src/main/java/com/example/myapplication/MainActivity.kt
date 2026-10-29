package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private val coinValues = listOf(1, 5, 10, 50, 100, 200, 500, 1000) // kopecks
    private val coinLabels = listOf("1 копейка", "5 копеек", "10 копеек", "50 копеек", "1 рубль", "2 рубля", "5 рублей", "10 рублей")
    private val billValues = listOf(500000, 200000, 100000, 50000, 20000, 10000, 5000) // kopecks
    private val billLabels = listOf("5000 рублей", "2000 рублей", "1000 рублей", "500 рублей", "200 рублей", "100 рублей", "50 рублей")
    private val quantityInputs = mutableListOf<EditText>()
    private lateinit var resultView: TextView
    private lateinit var totalView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Купюрообменник"
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.rgb(246, 248, 252))
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(24))
        }
        scroll.addView(root)

        root.addView(text("Купюрообменник", 28, Color.rgb(25, 55, 100), true))
        root.addView(text("Введите количество имеющихся монет. Приложение рассчитает сумму и разменяет её на купюры.", 16, Color.DKGRAY))
        root.addView(text("Количество монет", 20, Color.rgb(25, 55, 100), true, top = 18))

        coinLabels.forEachIndexed { index, label ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(5), dp(12), dp(5))
                setBackgroundColor(Color.WHITE)
            }
            val labelView = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(Color.rgb(35, 43, 55))
            }
            row.addView(labelView, LinearLayout.LayoutParams(0, dp(48), 1f))
            val input = EditText(this).apply {
                hint = "0"
                textSize = 16f
                inputType = InputType.TYPE_CLASS_NUMBER
                gravity = Gravity.CENTER
                setSingleLine(true)
                filters = arrayOf(android.text.InputFilter.LengthFilter(7))
            }
            quantityInputs.add(input)
            row.addView(input, LinearLayout.LayoutParams(dp(100), dp(48)))
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.bottomMargin = dp(2)
            root.addView(row, params)
        }

        val calculate = Button(this).apply {
            text = "Рассчитать размен"
            textSize = 16f
            isAllCaps = false
            setOnClickListener { calculateExchange() }
        }
        root.addView(calculate, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { topMargin = dp(18) })
        val reset = Button(this).apply {
            text = "Очистить поля"
            isAllCaps = false
            setOnClickListener {
                quantityInputs.forEach { it.text.clear() }
                totalView.text = "Общая сумма: 0,00 ₽"
                resultView.text = "Введите количество монет и нажмите «Рассчитать размен»."
            }
        }
        root.addView(reset, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)))

        totalView = text("Общая сумма: 0,00 ₽", 20, Color.rgb(25, 55, 100), true, top = 18)
        root.addView(totalView)
        root.addView(text("Результат размена", 20, Color.rgb(25, 55, 100), true, top = 12))
        resultView = text("Введите количество монет и нажмите «Рассчитать размен».", 16, Color.DKGRAY)
        resultView.setPadding(dp(14), dp(12), dp(14), dp(12))
        resultView.setBackgroundColor(Color.WHITE)
        root.addView(resultView)
        root.addView(text("Размен выполняется по убыванию номинала купюр. Остаток показывается отдельно.", 13, Color.GRAY, top = 12))

        setContentView(scroll)
    }

    private fun calculateExchange() {
        var totalKopecks = 0L
        for (i in quantityInputs.indices) {
            val raw = quantityInputs[i].text.toString().trim()
            val count = if (raw.isEmpty()) 0L else raw.toLongOrNull()
            if (count == null || count < 0L) {
                quantityInputs[i].error = "Введите целое неотрицательное число"
                return
            }
            totalKopecks += count * coinValues[i]
        }
        if (totalKopecks > Int.MAX_VALUE.toLong()) {
            resultView.text = "Слишком большая сумма. Уменьшите количество монет."
            return
        }
        totalView.text = "Общая сумма: ${formatMoney(totalKopecks)}"
        var remaining = totalKopecks
        val lines = mutableListOf<String>()
        billValues.forEachIndexed { index, value ->
            val count = remaining / value
            if (count > 0) {
                lines.add("${billLabels[index]} — $count шт.")
                remaining %= value
            }
        }
        if (lines.isEmpty()) lines.add("Купюры не выдаются: сумма меньше 50 рублей.")
        lines.add("")
        lines.add("Остаток: ${formatMoney(remaining)}")
        resultView.text = lines.joinToString("\n")
    }

    private fun formatMoney(kopecks: Long): String {
        val rubles = kopecks / 100
        val cents = kopecks % 100
        return "$rubles,${cents.toString().padStart(2, '0')} ₽"
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean = false, top: Int = 0): TextView =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(top), 0, dp(10))
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
