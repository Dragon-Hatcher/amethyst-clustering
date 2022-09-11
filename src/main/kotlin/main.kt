import geode.Geode
import solution.Solution
import solution.Solver
import solvers.UselessSolver

fun main() {
    testSolvers(
        UselessSolver()
    )
//    val geode = Geode.random(5)
//    val projectionX = geode.toProjection(Vec3Dir.X)
//    val projectionY = geode.toProjection(Vec3Dir.Y)
//    val projectionZ = geode.toProjection(Vec3Dir.Z)
//    println("X:")
//    projectionX.print()
//    println("------------")
//    println("Y:")
//    projectionY.print()
//    println("------------")
//    println("Z:")
//    projectionZ.print()
}

fun testSolvers(vararg solvers: Solver, iterations: Int = 1000) {
    val percentTotals = MutableList(solvers.size) { 0.0 }
    val groupTotals = MutableList(solvers.size) { 0.0 }
    val blockTotals = MutableList(solvers.size) { 0.0 }
    val invalidSolutions = MutableList(solvers.size) { mutableListOf<Solution>() }

    repeat(iterations) {
        val geode = Geode.random(5)
        val proj = geode.toProjection(Vec3Dir.X)

        for ((i, solver) in solvers.withIndex()) {
            val solution = solver.solve(proj)
            percentTotals[i] += solution.crystalPercentage()
            groupTotals[i] += solution.groupCount().toDouble()
            blockTotals[i] += solution.stickyBlockCount().toDouble()
            if (solution.checkIfValid().isNotEmpty()) {
                invalidSolutions[i].add(solution)
            }
        }
    }

    for ((i, solver) in solvers.withIndex()) {
        println("${solver.name()}:")
        println("  Avg. Crystal Percentage: ${percentTotals[i] / iterations * 100}%")
        println("  Avg. Group Count: ${groupTotals[i] / iterations}")
        println("  Avg. Block Count: ${blockTotals[i] / iterations}")
        println("  Num of Invalid Solutions: ${invalidSolutions[i].size} (${invalidSolutions[i].size / iterations * 100}%)")
    }

}