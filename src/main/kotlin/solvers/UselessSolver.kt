package solvers

import geode.GeodeProjection
import solution.Solution
import solution.Solver

class UselessSolver() : Solver {
    override fun name(): String = "Useless Solver"

    override fun solve(proj: GeodeProjection): Solution = Solution(proj, listOf())

}