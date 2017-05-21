package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param x X coordinate between 0 and 1
    * @param y Y coordinate between 0 and 1
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    x: Double,
    y: Double,
    d00: Double,
    d01: Double,
    d10: Double,
    d11: Double
  ): Double = {
    d00 * (1-x) * (1-y) +
    d10 * x * (1-y) +
    d01 * (1-x) * y +
    d11 * x * y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param zoom Zoom level of the tile to visualize
    * @param x X value of the tile to visualize
    * @param y Y value of the tile to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: (Int, Int) => Double,
    colors: Iterable[(Double, Color)],
    zoom: Int,
    x: Int,
    y: Int
  ): Image = {

    def predictTemperature(location: Location): Double = {

      val x00 = location.lat.floor.toInt
      val y00 = location.lon.floor.toInt

      val x01 = location.lat.floor.toInt
      val y01 = location.lon.ceil.toInt

      val x10 = location.lat.ceil.toInt
      val y10 = location.lon.floor.toInt

      val x11 = location.lat.ceil.toInt
      val y11 = location.lon.ceil.toInt

      bilinearInterpolation(location.lat - x00, location.lon - y00,
        grid(x00, y00), grid(x01, y01), grid(x10, y10), grid(x11, y11))
    }

    import Common._
    import Interaction._
    import Visualization._

    val locations = tileLocations(zoom, x, y)

    val image = Image.apply(TILE_SIZE, TILE_SIZE)

    locations.par.foreach(location => {
      val prediction = predictTemperature(location._3)
      val color = interpolateColor(colors, prediction)
      val pixel = Pixel.apply(color.red, color.green, color.blue, 127)
      image.setPixel(location._1, location._2, pixel)
    })

    image
  }

}
