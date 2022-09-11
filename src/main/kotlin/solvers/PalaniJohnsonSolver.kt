package solvers

import PUSH_LIMIT
import Vec2
import geode.GeodeProjection
import solution.Solution
import solution.SolutionGroup
import solution.Solver
import solution.StickyBlockType
import kotlin.math.pow
import kotlin.math.sqrt

class PalaniJohnsonSolver() : Solver {
    override fun name(): String = "Palani Johnson Solver"

    private fun neighbors(pos: Vec2): Set<Vec2> = setOf(pos.up(), pos.down(), pos.right(), pos.left())

    private fun makeOneGroup(crystals: Set<Vec2>, linkPoints: Set<Vec2>): SolutionGroup {
        val all = crystals + linkPoints
        val thisGroup = mutableSetOf<Vec2>()

        var toCheck = crystals.toMutableList()
        while (toCheck.isNotEmpty()) {
            if (thisGroup.size >= PUSH_LIMIT) break

            val c = toCheck.removeAt(0)
            thisGroup.add(c)

            val checkNext = neighbors(c).intersect(all).minus(thisGroup)

            if (checkNext.isEmpty()) break

            val avgX = checkNext.map { it.x }.average()
            val avgY = checkNext.map { it.y }.average()

            toCheck = checkNext.sortedBy {
                sqrt((it.x - avgX).pow(2) + (it.y - avgY).pow(2))
            }.toMutableList()
        }

        return SolutionGroup(thisGroup)
    }

    override fun solve(proj: GeodeProjection): Solution {
        val crystals = proj.crystals().toMutableSet()
        val linkPoints = crystals.flatMap(::neighbors).toMutableSet()

        val groups = mutableListOf<SolutionGroup>()

        while (crystals.isNotEmpty()) {
            val newGroup = makeOneGroup(crystals, linkPoints)
            crystals -= newGroup.blockLocations
            linkPoints -= newGroup.blockLocations
            groups.add(newGroup)
        }

        return Solution(proj, groups)
    }
}