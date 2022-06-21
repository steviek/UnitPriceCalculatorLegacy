package com.unitpricecalculator.unit

data class UnitEntry constructor(
    val cost: Double,
    val costString: String,
    val quantity: Int,
    val quantityString: String?,
    val size: Double,
    val sizeString: String,
    val unit: Unit
) {
    init {
        require(cost >= 0)
        require(quantity > 0)
        require(size > 0)
    }

    private val pricePerUnit: Double
        get() = cost / (quantity * size)
    val pricePerBaseUnit: Double
        get() = pricePer(1.0, unit.unitType.base)

    fun pricePer(size: Double, unit: Unit): Double {
        require(unit.unitType == this.unit.unitType) {
            "Expected " + this.unit.unitType + ", was " + unit.unitType
        }
        val costPerUnit = pricePerUnit
        val costPerOtherUnit = costPerUnit / (this.unit.factor / unit.factor)
        return size * costPerOtherUnit
    }

    class Builder {
        private var cost = 0.0
        private var costString: String? = null
        private var quantity = 1
        private var quantityString: String? = null
        private var size = 0.0
        private var sizeString: String? = null
        private var unit: Unit? = null

        fun setCost(cost: Double): Builder {
            require(cost >= 0)
            this.cost = cost
            return this
        }

        fun setQuantity(quantity: Int): Builder {
            require(quantity > 0)
            this.quantity = quantity
            return this
        }

        fun setUnit(unit: Unit): Builder {
            this.unit = unit
            return this
        }

        fun setSize(size: Double): Builder {
            require(size > 0)
            this.size = size
            return this
        }

        fun setCostString(costString: String): Builder {
            this.costString = costString
            return this
        }

        fun setQuantityString(quantityString: String): Builder {
            this.quantityString = quantityString
            return this
        }

        fun setSizeString(sizeString: String): Builder {
            this.sizeString = sizeString
            return this
        }

        fun build(): UnitEntry {
            return UnitEntry(
                cost,
                checkNotNull(costString) { "costString" },
                quantity,
                quantityString,
                size,
                checkNotNull(sizeString) { "sizeString" },
                checkNotNull(unit) { "unit" }
            )
        }
    }
}
