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
        val solution = Solution(proj, mutableListOf())
        var noFirstClusterExists = true

        // Create clusters
        for (x in proj.xRange()) {
            for (y in proj.yRange()) {
                val curBlockType = solution.getType(x, y)
                var curBlockGroup = solution.getGroup(x, y)

                if (curBlockType == BlockType.CRYSTAL) {

                    if (noFirstClusterExists) {
                        curBlockGroup = solution.makeEmptyGroup()
                        curBlockGroup.addBlock(x, y)
                        noFirstClusterExists = false
                    }

                    if (curBlockGroup != null && curBlockGroup.blockCount() >= PUSH_LIMIT) {
                        continue
                    }

                    for (x2 in (x - 1)..(x + 1)) {
                        for (y2 in (y - 1)..(y + 1)) {
                            val searchBlockType = solution.getType(x2, y2)
                            val searchBlockGroup = solution.getGroup(x2, y2)

                            if (searchBlockType == BlockType.CRYSTAL && searchBlockGroup == null) {
                                if (curBlockGroup == null) {
                                    val path = solution.findPath(x, y, x2, y2, null)

                                    if (path.isNotEmpty() && path.size <= PUSH_LIMIT) {
                                        curBlockGroup = solution.makeEmptyGroup()
                                        path.forEach { curBlockGroup.addBlock(it) }
                                    }
                                } else if (curBlockGroup.blockCount() < PUSH_LIMIT) {
                                    val path = solution.findPath(x, y, x2, y2, curBlockGroup)

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
            do {
                var anyMerge = false
                outerMost@for (searchAreaWidth in 1..5) {
                    for (x in proj.xRange()) {
                        for (y in proj.yRange()) {
                            val curBlockType = solution.getType(x, y)
                            if (curBlockType != BlockType.CRYSTAL) continue

                            val curBlockGroup = solution.getGroup(x, y)
                            if (curBlockGroup!!.blockCount() >= PUSH_LIMIT) continue

                            for (x2 in (x - searchAreaWidth)..(x + searchAreaWidth)) {
                                for (y2 in (y - searchAreaWidth)..(y + searchAreaWidth)) {
                                    if (x == x2 && y == y2) continue

                                    val searchBlockType = solution.getType(x2, y2)
                                    if (searchBlockType != BlockType.CRYSTAL) continue

                                    val searchBlockGroup = solution.getGroup(x2, y2)
                                    if (searchBlockGroup == curBlockGroup) continue
                                    if (curBlockGroup.blockCount() + searchBlockGroup!!.blockCount() > PUSH_LIMIT) continue

                                    val path = solution.findPath(x, y, x2, y2, searchBlockGroup)

                                    if (path.isEmpty()) continue
                                    if (curBlockGroup.blockCount() + searchBlockGroup.blockCount() + path.size - 2 > PUSH_LIMIT) continue

                                    solution.mergeGroups(curBlockGroup, searchBlockGroup, path)
                                    anyMerge = true
                                    break@outerMost // Seems to be slightly faster
                                }
                            }
                        }
                    }
                }
            } while (anyMerge)
        }

        return solution
    }
}