package solvers

import PUSH_LIMIT
import Vec2
import geode.BlockType
import geode.GeodeProjection
import solution.Solution
import solution.Solver
import java.util.PriorityQueue

class IterniamSolver(val tries: Int = 1) : Solver {
    override fun name() = "Iterniam Solver"

    private fun calcAverageDist(cell: Vec2, solution: Solution): Double {
        var totalDist = 0.0
        var visitedCellCount = 0

        var currentDist = 0
        val visitedCells = mutableSetOf<Vec2>()
        var currentEdge = setOf(cell)

        while (currentEdge.isNotEmpty()) {
            totalDist += currentDist * currentEdge.size
            visitedCellCount += currentEdge.size

            val newEdge = currentEdge
                .flatMap(Vec2::neighbors)
                .minus(visitedCells)
                .filter { solution.proj.isInBounds(it) && solution.proj[it] != BlockType.BUD && solution.getGroup(it) == null }
                .toSet()

            visitedCells += currentEdge
            currentEdge = newEdge
            currentDist += 1
        }

        return totalDist / visitedCellCount
    }

    private fun computeAverageDist(solution: Solution, unassigned: Set<Vec2>, bridges: Set<Vec2>): Map<Vec2, Double> {
        val map = mutableMapOf<Vec2, Double>()

        (unassigned + bridges).forEach { map[it] = calcAverageDist(it, solution) }

        return map
    }

    fun oneSolution(proj: GeodeProjection): Solution {
        val solution = Solution(proj, mutableListOf())

        val unassigned = proj.crystals().toMutableSet()
        val bridges = proj.bridges().toMutableSet()

        while (unassigned.isNotEmpty()) {
            val distanceMap = computeAverageDist(solution, unassigned, bridges)

            val mostIsolated =
                unassigned.maxBy { (distanceMap[it] ?: Double.NEGATIVE_INFINITY) + (Math.random() * 2 - 1) }
            val newGroup = solution.makeEmptyGroup()

            val comparator = compareBy<Vec2> { -(distanceMap[it] ?: Double.POSITIVE_INFINITY) }
            val queue = PriorityQueue(comparator)
            val neighborsSoFar = mutableSetOf<Vec2>()

            queue.add(mostIsolated)
            neighborsSoFar.add(mostIsolated)
            while (newGroup.blockCount() < PUSH_LIMIT && queue.isNotEmpty()) {
                val nextMember = queue.remove()

                if (newGroup.blockCount() == PUSH_LIMIT - 1 && nextMember in bridges) continue

                newGroup.addBlock(nextMember)
                unassigned.remove(nextMember)
                bridges.remove(nextMember)

                for (newNeighbor in nextMember
                    .neighbors().toSet()
                    .intersect(unassigned + bridges)
                    .minus(neighborsSoFar)) {
                    queue.add(newNeighbor)
                    neighborsSoFar.add(newNeighbor)
                }
            }
        }

        return solution
    }


    override fun solve(proj: GeodeProjection): Solution {
        var solution = oneSolution(proj)

        repeat(tries - 1) {
            val newSolution = oneSolution(proj)
            if (newSolution.betterThan(solution)) solution = newSolution
        }

        return solution
    }

}