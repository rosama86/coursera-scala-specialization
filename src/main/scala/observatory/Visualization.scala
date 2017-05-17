package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.log4j.Logger

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  val logger = Logger.getLogger(Visualization.getClass)

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Double)], location: Location): Double = {

    case class IWB(numerator: Double, denominator: Double) {
      val result: Double = numerator / denominator
    }

    val exact = temperatures.find(_._1 == location)

    exact match {
      case Some(station) => station._2
      case _ =>
        val iwb =
          temperatures
            .map(temp => {
              val p = 2
              val distance = iWBDistance(location, temp._1)
              val distanceP = Math.pow(distance, p)

              IWB(temp._2 / distanceP, 1 / distanceP)
            })
            .reduce((a: IWB, b: IWB) => IWB(a.numerator + b.numerator, a.denominator + b.denominator))

        iwb.result
    }
  }

  // http://www.movable-type.co.uk/scripts/latlong.html
  def iWBDistance(from: Location, to: Location): Double = {
    import Math._

    val φ1 = toRadians(from.lat)
    val φ2 = toRadians(to.lat)

    val Δφ = toRadians(to.lat - from.lat)
    val Δλ = toRadians(to.lon - from.lon)

    // 	a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
    val a = sin(Δφ / 2) + cos(φ1) * cos(φ2) * sin(Δλ / 2)

    val R = 6371000

    // cosines:	d = R * acos( sin φ1 ⋅ sin φ2 + cos φ1 ⋅ cos φ2 ⋅ cos Δλ )
    R * acos(sin(φ1) * sin(φ2) + cos(φ1) * cos(φ2) * cos(Δλ))
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {
    if (points.size == 1)
      points.head._2
    else {

      val sortedPoints =
        points
          .toList
          .sortWith(_._1 < _._1)

      val low = sortedPoints.head
      val high = sortedPoints.last

      if (high._1 <= value) {
        high._2
      } else if (value <= low._1) {
        low._2
      } else {

        val closestPoints =
          sortedPoints
            .map {
              p => (p._1, p._2, Math.abs(p._1 - value))
            }
            .sortWith(_._3 < _._3)
            .take(2)

        // http://www.alanzucconi.com/2016/01/06/colour-interpolation/
        // (color2.r - color1.r) * t + color1.r
        val color1 = closestPoints.head._2
        val color2 = closestPoints(1)._2

        val t = (value - closestPoints.head._1) / (closestPoints(1)._1 - closestPoints.head._1)

        val rt = ((color2.red - color1.red) * t + color1.red).round.toInt
        val gt = ((color2.green - color1.green) * t + color1.green).round.toInt
        val bt = ((color2.blue - color1.blue) * t + color1.blue).round.toInt

        val r = if (rt > 255) 255 else if (rt < 0) 0 else rt
        val g = if (gt > 255) 255 else if (gt < 0) 0 else gt
        val b = if (bt > 255) 255 else if (bt < 0) 0 else bt

        Color(r, g, b)
      }
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)]): Image = {
    import Common.WIDTH
    import Common.HEIGHT
    import Common.ALPHA

    val image = Image.apply(WIDTH, HEIGHT)

    val xc = 180
    val yc = 90

    def mapX(x: Int): Int = if (x == xc) 0 else x - xc
    def mapY(y: Int): Int = if (y == yc) 0 else yc - y

    val coordinates =
      for {
        x <- 0 until WIDTH
        y <- 0 until HEIGHT
      } yield (x, y, mapX(x), mapY(y))

    coordinates.par.foreach(coordinate => {
      val temperature = predictTemperature(temperatures, Location(coordinate._4, coordinate._3))
      val color = interpolateColor(colors, temperature)
      val pixel = Pixel.apply(color.red, color.green, color.blue, ALPHA)
      image.setPixel(coordinate._1, coordinate._2, pixel)
    })

    image
  }

}

