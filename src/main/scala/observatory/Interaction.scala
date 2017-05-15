package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  /**
    * @param zoom Zoom level
    * @param x    X coordinate
    * @param y    Y coordinate
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(zoom: Int, x: Int, y: Int): Location = {
    //http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Mathematics
    import Math._

    val n = 1 << zoom
    val lon = x.toDouble / n * Common.WIDTH - Common.HEIGHT
    val lat = toDegrees(atan(sinh(PI * (1 - 2 * y / n.toDouble))))

    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @param zoom         Zoom level
    * @param x            X coordinate
    * @param y            Y coordinate
    * @return A 256×256 image showing the contents of the tile defined by `x`, `y` and `zooms`
    */
  def tile(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)], zoom: Int, x: Int, y: Int): Image = {

    import Common._
    import Visualization._

    val locations =
      for {
        ix <- 0 until TILE_SIZE
        iy <- 0 until TILE_SIZE
      } yield (ix, iy, tileLocation(zoom, x + ix, y + iy))

    println("Tile locations calculated")

    val image = Image.apply(TILE_SIZE, TILE_SIZE)

    locations.par.foreach(location => {
      val prediction = predictTemperature(temperatures, location._3)
      val color = interpolateColor(colors, prediction)
      val pixel = Pixel.apply(color.red, color.green, color.blue, 127)
      image.setPixel(location._1, location._2, pixel)
    })

    println("Visualization completed")

    image
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    *
    * @param yearlyData    Sequence of (year, data), where `data` is some data associated with
    *                      `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
                           yearlyData: Iterable[(Int, Data)],
                           generateImage: (Int, Int, Int, Int, Data) => Unit
                         ): Unit = {

    yearlyData.foreach(year => {
      for {
        zoom <- 0 to 3
        x <- 0 until Math.pow(2, zoom).toInt
        y <- 0 until Math.pow(2, zoom).toInt
      } yield generateImage(year._1, zoom, x, y, year._2)
    })
  }
}