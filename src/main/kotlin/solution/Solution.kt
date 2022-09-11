package solution

import Vec2
import geode.BlockType
import geode.GeodeProjection
import java.util.Collections

enum class StickyBlockType {
    SLIME,
    HONEY
}

data class SolutionGroup(
    var blockLocations: MutableSet<Vec2> = mutableSetOf(),
    var blockType: StickyBlockType = StickyBlockType.SLIME,
    var flyingMachineLoc: Vec2? = null,
    var flyingMachineIsVert: Boolean? = null,
    var immovableLoc: Vec2? = null
) {
    fun includes(loc: Vec2) = loc in blockLocations

    fun blockCount() = blockLocations.size

    fun addBlock(x: Int, y: Int) {
        blockLocations.add(Vec2(x, y))
    }

    fun addBlock(pos: Vec2) {
        blockLocations.add(pos)
    }
}

enum class InvalidSolutionReason {

}


fun randRGB() = (0..128).random()
fun randomColor() = "\u001B[48;2;${randRGB()};${randRGB()};${randRGB()}m"

const val ANSI_RESET = "\u001B[0m"
const val ANSI_GRAY = "\u001B[38;2;128;128;128m"
val ANSI_COLORS = mutableListOf(
    "\u001B[48;2;128;0;0m",
    "\u001B[48;2;170;110;40m",
    "\u001B[48;2;128;128;0m",
    "\u001B[48;2;0;128;128m",
    "\u001B[48;2;0;0;128m",
    "\u001B[48;2;230;25;75m",
    "\u001B[48;2;245;130;48m\u001B[30m",
    "\u001B[48;2;255;255;25m\u001B[30m",
    "\u001B[48;2;210;245;60m\u001B[30m",
    "\u001B[48;2;60;180;75m\u001B[30m",
    "\u001B[48;2;70;240;240m\u001B[30m",
    "\u001B[48;2;0;130;200m",
    "\u001B[48;2;145;30;180m",
    "\u001B[48;2;240;50;230m\u001B[30m",
    "\u001B[48;2;250;190;212m\u001B[30m",
    "\u001B[48;2;255;215;180m\u001B[30m",
    "\u001B[48;2;255;250;200m\u001B[30m",
    "\u001B[48;2;170;255;195m\u001B[30m",
    "\u001B[48;2;220;190;255m\u001B[30m",
)

fun getColor(i: Int): String {
    while (i !in ANSI_COLORS.indices) {
        ANSI_COLORS.add(randomColor())
    }

    return ANSI_COLORS[i]
}

data class Solution(val proj: GeodeProjection, var groups: MutableList<SolutionGroup>) {


    fun checkIfValid(): List<InvalidSolutionReason> {
        // TODO
        return listOf()
    }

    fun getType(x: Int, y: Int) = proj[x, y]
    fun getType(pos: Vec2) = proj[pos]

    fun getGroup(x: Int, y: Int): SolutionGroup? = getGroup(Vec2(x, y))
    fun getGroup(pos: Vec2): SolutionGroup? = groups.find { pos in it.blockLocations }

    fun makeEmptyGroup(): SolutionGroup {
        val new = SolutionGroup()
        groups.add(new)
        return new
    }

    fun xRange() = proj.xRange()
    fun yRange() = proj.yRange()

    fun addGroup(group: SolutionGroup) {
        groups.add(group)
    }

    fun removeGroup(group: SolutionGroup) =
        groups.remove(group)

    fun mergeGroups(base: SolutionGroup, other: SolutionGroup, withPath: Collection<Vec2>) {
        val new = base.copy(blockLocations = (base.blockLocations + other.blockLocations + withPath).toMutableSet())
        groups.remove(base)
        groups.remove(other)
        groups.add(new)
    }

    fun crystalPercentage(): Double {
        val crystals = proj.crystals()
        val covered = crystals.count { c -> groups.any { it.includes(c) } }

        return covered.toDouble() / crystals.count()
    }

    fun groupCount(): Int = groups.count()

    fun findPath(x1: Int, y1: Int, x2: Int, y2: Int, group: SolutionGroup?) =
        findPath(Vec2(x1, y1), Vec2(x2, y2), group)

    fun findPath(start: Vec2, end: Vec2, group: SolutionGroup?) =
        solvers.findPath(proj, start, end, group, groups)

    fun stickyBlockCount() = groups.sumOf { it.blockCount() }

    fun crystalCount() = proj.crystals().size

    fun prettyPrint() {
        fun IntRange.expand(amount: Int = 1) =
            IntRange(this.first - amount, this.last + amount)

        val xRange = proj.xRange().expand()
        val yRange = proj.yRange().expand()

        for (y in yRange) {
            for (x in xRange) {
                when (proj[x, y]) {
                    BlockType.AIR -> {
                        val groupNum = groups.indexOfFirst { Vec2(x, y) in it.blockLocations }
                        if (groupNum != -1) {
                            val color = getColor(groupNum)
                            print("$color  $ANSI_RESET")
                        } else {
                            print("  ")
                        }
                    }

                    BlockType.CRYSTAL -> {
                        val groupNum = groups.indexOfFirst { Vec2(x, y) in it.blockLocations }
                        val color = if (groupNum == -1) ANSI_GRAY else getColor(groupNum)
                        if (groupNum < 0) {
                            print("$color..$ANSI_RESET")
                        } else if (groupNum < 10) {
                            print("${color}0$groupNum$ANSI_RESET")
                        } else {
                            print("$color$groupNum$ANSI_RESET")
                        }
                    }

                    BlockType.BUD -> print("$ANSI_GRAY##$ANSI_RESET")
                }
            }
            println()
        }
    }

    fun betterThan(other: Solution): Boolean {
        val tCP = this.crystalPercentage()
        val oCP = other.crystalPercentage()
        return if (tCP == oCP) {
            val tGC = this.groupCount()
            val oGC = other.groupCount()
            if (tGC == oGC) {
                this.stickyBlockCount() < other.stickyBlockCount()
            } else {
                tGC < oGC
            }
        } else {
            tCP > oCP
        }
    }

    fun max(other: Solution) = if (this.betterThan(other)) this else other
    fun min(other: Solution) = if (this.betterThan(other)) other else this
}