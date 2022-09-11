import geode.Geode
import solution.Solution
import solution.Solver
import solvers.MLFlexerSolver
import solvers.UselessSolver

fun main() {
    testSolvers(
        UselessSolver(),
        MLFlexerSolver()
    )
}

fun testSolvers(vararg solvers: Solver, iterations: Int = 1000) {
    val percentTotals = MutableList(solvers.size) { 0.0 }
    val groupTotals = MutableList(solvers.size) { 0.0 }
    val blockTotals = MutableList(solvers.size) { 0.0 }
    val invalidSolutions = MutableList(solvers.size) { mutableListOf<Solution>() }
    val worstSolution = MutableList(solvers.size) { null as Solution? }

    repeat(iterations) {
        val geode = Geode.random(5)
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
            if (percent < (worstSolution[i]?.crystalPercentage() ?: 1.0)) {
                worstSolution[i] = solution
            }
        }
    }

    for ((i, solver) in solvers.withIndex()) {
        println("${solver.name()}:")
        println("  Avg. Crystal Percentage: ${percentTotals[i] / iterations * 100}%")
        println("  Avg. Group Count: ${groupTotals[i] / iterations}")
        println("  Avg. Block Count: ${blockTotals[i] / iterations}")
        println("  Num of Invalid Solutions: ${invalidSolutions[i].size} (${invalidSolutions[i].size / iterations * 100}%)")
        if (worstSolution[i] != null) {
            println("  Worst Solution:")
            worstSolution[i]!!.prettyPrint()
        }
        println()
    }

}