package solvers

import Vec2
import geode.CellType
import geode.GeodeProjection
import solution.Solution
import solution.SolutionGroup
import solution.Solver
import solution.StickyBlockType

const val PUSH_LIMIT = 12;

class MLFlexerSolver : Solver {
    override fun name(): String = "MLFlexer Solver"

    override fun solve(proj: GeodeProjection): Solution {
        val groups: MutableList<SolutionGroup> = mutableListOf()
        var noFirstClusterExists = true

        fun newGroup(): SolutionGroup {
            val new = SolutionGroup(mutableSetOf(), StickyBlockType.SLIME, Vec2(0, 0), true, Vec2(0, 0))
            groups.add(new)
            return new
        }


        fun findBlocksGroup(x: Int, y: Int): SolutionGroup? {
            val pos = Vec2(x, y)
            return groups.find { pos in it.blockLocations }
        }

        for (x in proj.xRange()) {
            for (y in proj.yRange()) {
                val curBlockType = proj[x, y]
                var curBlockGroup = findBlocksGroup(x, y)

                if (curBlockType == CellType.CRYSTAL) {

                    if (noFirstClusterExists) {
                        curBlockGroup = newGroup()
                        curBlockGroup.addBlock(x, y)
                        noFirstClusterExists = false
                    }

                    if (curBlockGroup != null && curBlockGroup.blockCount() >= PUSH_LIMIT) {
                        continue
                    }

                    for (x2 in (x - 1)..(x + 1)) {
                        for (y2 in (y - 1)..(y + 1)) {
                            val searchBlockType = proj[x2, y2]
                            val searchBlockGroup = findBlocksGroup(x2, y2)

                            if (searchBlockType == CellType.CRYSTAL && searchBlockGroup == null) {
                                if (curBlockGroup == null) {
                                    val path = findPath(proj, Vec2(x, y), Vec2(x2, y2), curBlockGroup, groups)

                                    if (path.isNotEmpty() && path.size <= PUSH_LIMIT) {
                                        curBlockGroup = newGroup()
                                        path.forEach { curBlockGroup.addBlock(it) }
                                    }
                                } else if (curBlockGroup.blockCount() < PUSH_LIMIT) {
                                    val path = findPath(proj, Vec2(x, y), Vec2(x2, y2), curBlockGroup, groups)

                                    if (path.size + curBlockGroup.blockCount() <= PUSH_LIMIT) {
                                        path.forEach { curBlockGroup.addBlock(it) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return Solution(proj, groups)
    }
}