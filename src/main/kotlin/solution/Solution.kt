package solution

import Vec2
import geode.GeodeProjection

enum class StickyBlockType {
    SLIME,
    HONEY
}

data class SolutionGroup(
    val blockLocations: Set<Vec2>,
    val blockType: StickyBlockType,
    val flyingMachineLoc: Vec2,
    val flyingMachineIsVert: Boolean,
    val immovableLoc: Vec2
) {
    fun includes(loc: Vec2) = loc in blockLocations

    fun blockCount() = blockLocations.size
}

enum class InvalidSolutionReason {

}

data class Solution(val forProj: GeodeProjection, val groups: List<SolutionGroup>) {


    fun checkIfValid(): List<InvalidSolutionReason> {
        // TODO
        return listOf()
    }

    fun crystalPercentage(): Double {
        val crystals = forProj.crystals()
        val covered = crystals.count { c -> groups.any { it.includes(c) } }

        return covered.toDouble() / crystals.count()
    }

    fun groupCount(): Int = groups.count()

    fun stickyBlockCount() = groups.sumOf { it.blockCount() }

    fun prettyPrint() {

    }

}