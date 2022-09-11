package geode

import Vec2

enum class CellType {
    AIR,
    CRYSTAL,
    BUD;

    fun max(other: CellType) =
        when {
            this == BUD || other == BUD -> BUD
            this == CRYSTAL || other == CRYSTAL -> CRYSTAL
            else -> AIR
        }

}

class GeodeProjection(private val cells: Map<Vec2, CellType>) {
    fun xRange() =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.x }!!..cells.keys.maxOfOrNull { it.y }!!

    fun yRange() =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.y }!!..cells.keys.maxOfOrNull { it.y }!!

    operator fun get(x: Int, y: Int): CellType =
        get(Vec2(x, y))

    operator fun get(pos: Vec2): CellType =
        cells[pos] ?: CellType.AIR

    fun print() {
        fun IntRange.expand(amount: Int = 1) =
            IntRange(this.first - amount, this.last + amount)

        for (y in yRange().expand()) {
            for (x in xRange().expand()) {
                when (get(x, y)) {
                    CellType.AIR -> print(" ")
                    CellType.CRYSTAL -> print("\u001B[35m\u001B[1mx\u001B[0m")
                    CellType.BUD -> print("\u001B[90m#\u001B[0m")
                }
            }
            println()
        }
    }
}