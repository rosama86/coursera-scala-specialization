package observatory


import java.io.File

import com.sksamuel.scrimage.nio.JpegWriter
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class VisualizationTest extends FunSuite with Checkers {

  test("Color interpolation 1") {
    val scale = List((0.0, Color(0, 0, 255)))
    assert(Visualization.interpolateColor(scale, -0.5) == Color(0, 0, 255))
    assert(Visualization.interpolateColor(scale, 0.5) == Color(0, 0, 255))
    assert(Visualization.interpolateColor(scale, 0.0) == Color(0, 0, 255))
  }

  test("Color interpolation 2") {
    val scale = List((0.0, Color(255, 0, 0)), (1.0, Color(0, 0, 255)))
    assert(Visualization.interpolateColor(scale, 0.25) == Color(191, 0, 64))
  }

  test("Color interpolation 3") {
    val scale = List((-1.0, Color(255, 0, 0)), (0.0, Color(0, 0, 255)))
    assert(Visualization.interpolateColor(scale, -0.75) == Color(191, 0, 64))
  }

  test("Color interpolation Max") {
    val scale = List((-4.767820415349206, Color(255, 0, 0)), (-1.0, Color(0, 0, 255)))
    assert(Visualization.interpolateColor(scale, -1.0) == Color(0, 0, 255))
  }

  test("predictTemperature: some point closer") {
    val location1 = Location(1, 1)
    val temp1 = 10d
    val location2 = Location(-10, -10)
    val temp2 = 50d
    val list = List(
      (location1, temp1),
      (location2, temp2)
    )
    val result = Visualization.predictTemperature(list, Location(0, 0))
    assert(temp1 - result < temp2 - result)
  }

  test("predictTemperature: NAN") {
    val list = List(
      (Location(0.0, 0.0), 10.0)
    )
    val result = Visualization.predictTemperature(list, Location(0, 0))
    assert(10 == result)
  }

  test("predictTemperature: visualize") {
    val list = List(
      (Location(45.0, -90.0), -100.0),
      (Location(-45.0, 0.0), 1.0))

    val scale =
      List(
        (-45.0, Color(0, 0, 255)),
        (45.0, Color(255, 0, 0)))

    val result1 = Visualization.predictTemperature(list, Location(90.0, -180.0))
    val result2 = Visualization.predictTemperature(list, Location(26.0, -180.0))

    val color1 = Visualization.interpolateColor(scale, result1)
    val color2 = Visualization.interpolateColor(scale, result2)

    assert(color1 == Color(0, 0, 255))
    assert(color2 == Color(255, 0, 0))

  }

  test("visualize grader 2") {

    val locations = List(
      (new Location(45.0, -90.0), 20.0), (new Location(45.0, 90.0), 0.0), (new Location(0.0, 0.0), 10.0), (new Location(-45.0, -90.0), 0.0), (new Location(-45.0, 90.0), 20.0))
    val colorMap = List(
      (0.0, Color(255, 0, 0)), (10.0, Color(0, 255, 0))
      ,(20.0 , Color(0  , 0  , 255))
    )
    val pt: Location = Location(-27.059125784374057,-178.59375)
    val temperature = Visualization.predictTemperature(locations, pt)

    val color = Visualization.interpolateColor(colorMap, temperature)
    println((color, temperature))

    assert(color.red > color.blue, "interpolateColor")
  }
}
