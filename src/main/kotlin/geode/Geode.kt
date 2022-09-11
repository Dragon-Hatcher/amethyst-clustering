package geode

import Vec2
import Vec3
import Vec3Dir
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt

class Geode(val buds: List<Vec3>) {

    companion object {
        const val BUD_DENSITY = 0.083

        fun random(radius: Int, density: Double = BUD_DENSITY): Geode {
            return random(radius.toDouble(), density)
        }

        fun random(radius: Double, density: Double = BUD_DENSITY, crack: Boolean = true): Geode {
            val surfaceArea = 4.0 / 3.0 * PI * radius.pow(3)
            val numBuds = (surfaceArea * density).roundToInt()
            var buds = (0 until numBuds).map { Vec3.randomOnSphere(radius) }

            if (crack) {
                val crackPoint = Vec3.randomOnSphere(radius)
                val crackDist = (2 * PI * radius) / 8
                buds = buds.filter { it.dist(crackPoint) > crackDist }
            }

            return Geode(buds)
        }
    }

    fun toProjection(dir: Vec3Dir): GeodeProjection {
        val cells: MutableMap<Vec2, CellType> = mutableMapOf()

        for (budPosition in buds) {
            val budXY = budPosition.without(dir)

            cells[budXY] = CellType.BUD

            cells[budXY.left()] = CellType.CRYSTAL.max(cells[budXY.left()] ?: CellType.AIR)
            cells[budXY.right()] = CellType.CRYSTAL.max(cells[budXY.right()] ?: CellType.AIR)
            cells[budXY.up()] = CellType.CRYSTAL.max(cells[budXY.up()] ?: CellType.AIR)
            cells[budXY.down()] = CellType.CRYSTAL.max(cells[budXY.down()] ?: CellType.AIR)
        }

        return GeodeProjection(cells)
    }

}