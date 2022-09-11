import geode.Geode
import solution.Solution
import solution.Solver
import solvers.MLFlexerSolver
import solvers.UselessSolver
import kotlin.math.roundToInt

fun main() {
    testSolvers(
        UselessSolver(),
        MLFlexerSolver(),
        MLFlexerSolver(merge=false)
    )
}

fun testSolvers(vararg solvers: Solver, iterations: Int = 100) {
    val percentTotals = MutableList(solvers.size) { 0.0 }
    val groupTotals = MutableList(solvers.size) { 0.0 }
    val blockTotals = MutableList(solvers.size) { 0.0 }
    val invalidSolutions = MutableList(solvers.size) { mutableListOf<Solution>() }
    val worstSolution: MutableList<Solution?> = MutableList(solvers.size) { null }

    repeat(iterations) {
        val geode = Geode.random(6)
        val proj = geode.toProjection(Vec3Dir.X)

        for ((i, solver) in solvers.withIndex()) {
            val solution = solver.solve(proj)
            val percent = solution.crystalPercentage()
            percentTotals[i] += percent
            groupTotals[i] += solution.groupCount().toDouble()
            blockTotals[i] += solution.stickyBlockCount().toDouble()
            if (solution.checkIfValid().isNotEmpty()) {
                invalidSolutions[i].add(solution)
            }
            if (worstSolution[i] == null || worstSolution[i]!!.betterThan(solution)) {
                worstSolution[i] = solution
            }
        }
    }

    fun p(num: Double): String =
        "${(num * 10000).roundToInt().toDouble() / 100}%"

    solvers
        .withIndex()
        .sortedBy { blockTotals[it.index] }
        .sortedBy { groupTotals[it.index] }
        .sortedByDescending { percentTotals[it.index] }
        .forEach { (i, solver) ->
            println("${solver.name()}:")
            println("  Avg. Crystal Percentage: ${p(percentTotals[i] / iterations)}")
            println("  Avg. Group Count: ${groupTotals[i] / iterations}")
            println("  Avg. Block Count: ${blockTotals[i] / iterations}")
            println("  Num of Invalid Solutions: ${invalidSolutions[i].size} (${invalidSolutions[i].size / iterations * 100}%)")
            val worst = worstSolution[i]
            if (worst != null) {
                println("  Worst Solution: ${p(worst.crystalPercentage())} crystals, ${worst.groupCount()} groups, ${worst.stickyBlockCount()} blocks")
                worstSolution[i]!!.prettyPrint()
            }
            println()
        }

}