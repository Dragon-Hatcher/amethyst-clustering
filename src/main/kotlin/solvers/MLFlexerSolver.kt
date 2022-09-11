package solvers

import PUSH_LIMIT
import Vec2
import geode.BlockType
import geode.GeodeProjection
import solution.Solution
import solution.SolutionGroup
import solution.Solver
import solution.StickyBlockType

class MLFlexerSolver(val merge: Boolean = true) : Solver {
    override fun name(): String = if (merge) "MLFlexer Solver" else "MLFlexer Solver (No Merge)"

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

        // Create clusters
        for (x in proj.xRange()) {
            for (y in proj.yRange()) {
                val curBlockType = proj[x, y]
                var curBlockGroup = findBlocksGroup(x, y)

                if (curBlockType == BlockType.CRYSTAL) {

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

                            if (searchBlockType == BlockType.CRYSTAL && searchBlockGroup == null) {
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

        // Merge clusters
        if (merge) {
            for (searchAreaWidth in 1..6) {
                for (x in proj.xRange()) {
                    for (y in proj.yRange()) {
                        val curBlockType = proj[x, y]
                        if (curBlockType != BlockType.CRYSTAL) continue

                        val curBlockGroup = findBlocksGroup(x, y)
                        if (curBlockGroup!!.blockCount() >= PUSH_LIMIT) continue

                        for (x2 in (x - searchAreaWidth)..(x + searchAreaWidth)) {
                            for (y2 in (y - searchAreaWidth)..(y + searchAreaWidth)) {
                                if (x == x2 && y == y2) continue

                                val searchBlockType = proj[x2, y2]
                                if (searchBlockType != BlockType.CRYSTAL) continue

                                val searchBlockGroup = findBlocksGroup(x2, y2)
                                if (searchBlockGroup == curBlockGroup) continue
                                if (curBlockGroup.blockCount() + searchBlockGroup!!.blockCount() > PUSH_LIMIT) continue

                                val path = findPath(proj, Vec2(x, y), Vec2(x2, y2), searchBlockGroup, groups)

                                if (path.isEmpty()) continue
                                if (curBlockGroup.blockCount() + searchBlockGroup.blockCount() + path.size - 2 > PUSH_LIMIT) continue

                                curBlockGroup.blockLocations.addAll(path)
                                curBlockGroup.blockLocations.addAll(searchBlockGroup.blockLocations)
                                groups.remove(searchBlockGroup)
                            }
                        }
                    }
                }
            }
        }

        return Solution(proj, groups)
    }
}