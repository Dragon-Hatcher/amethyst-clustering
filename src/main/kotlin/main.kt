import geode.Geode

fun main() {
    val geode = Geode.random(5)
    val projectionX = geode.toProjection(Vec3Dir.X)
    val projectionY = geode.toProjection(Vec3Dir.Y)
    val projectionZ = geode.toProjection(Vec3Dir.Z)
    println("X:")
    projectionX.print()
    println("------------")
    println("Y:")
    projectionY.print()
    println("------------")
    println("Z:")
    projectionZ.print()
}