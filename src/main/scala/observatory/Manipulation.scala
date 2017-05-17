package observatory

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Double)]): (Int, Int) => Double = {
    (lon: Int, lat: Int) => {
      Visualization.predictTemperature(temperatures, Location(lon, lat))
    }
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Double)]]): (Int, Int) => Double = {
    (lon: Int, lat: Int) => {
      val temperatureOverYears =
        temperaturess.map(temperatures => Visualization.predictTemperature(temperatures,
          Location(lon, lat)))

      temperatureOverYears.sum / temperatureOverYears.size
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param normals      A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Double)], normals: (Int, Int) => Double): (Int, Int) => Double = {
    (lon: Int, lat: Int) => {

      val calculated = makeGrid(temperatures)(lon, lat)
      val normal = normals(lon, lat)

      calculated - normal
    }

  }


}

