package geode

import Vec2

enum class BlockType {
    AIR,
    CRYSTAL,
    BUD;

    fun max(other: BlockType) =
        when {
            this == BUD || other == BUD -> BUD
            this == CRYSTAL || other == CRYSTAL -> CRYSTAL
            else -> AIR
        }

}

class GeodeProjection(private val cells: Map<Vec2, BlockType>) {
    fun xRange() =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.x }!!..cells.keys.maxOfOrNull { it.x }!!

    fun yRange() =
        if (cells.keys.isEmpty()) 0..0 else
            cells.keys.minOfOrNull { it.y }!!..cells.keys.maxOfOrNull { it.y }!!

    operator fun get(x: Int, y: Int): BlockType =
        get(Vec2(x, y))

    operator fun get(pos: Vec2): BlockType =
        cells[pos] ?: BlockType.AIR

    fun crystals() =
        cells.entries.filter { it.value == BlockType.CRYSTAL }.map { it.key }

    fun bud() =
        cells.entries.filter { it.value == BlockType.BUD }.map { it.key }

    fun print() {
        fun IntRange.expand(amount: Int = 1) =
            IntRange(this.first - amount, this.last + amount)

        for (y in yRange().expand()) {
            for (x in xRange().expand()) {
                when (get(x, y)) {
                    BlockType.AIR -> print(" ")
                    BlockType.CRYSTAL -> print("\u001B[35m\u001B[1mx\u001B[0m")
                    BlockType.BUD -> print("\u001B[90m#\u001B[0m")
                }
            }
            println()
        }
    }
}