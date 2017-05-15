package observatory

import java.io.File

import com.sksamuel.scrimage.nio.{JpegWriter, PngWriter}

object Main extends App {

  override def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "c:\\programs\\hadoop")

    val scale =
      List(
        (60d, Color(255, 255, 255)),
        (32d, Color(255, 0, 0)),
        (12d, Color(255, 255, 0)),
        (0d, Color(0, 255, 255)),
        (-15d, Color(0, 0, 255)),
        (-27d, Color(255, 0, 255)),
        (-50d, Color(33, 0, 107)),
        (-60d, Color(0, 0, 0))
      )

    val year = 2015

    val yearlyData =
      List(year)
              .map(year => (year, Extraction.locateTemperatures(year, "/stations.csv", s"/$year.csv")))
              .map(extract => (extract._1, Extraction.locationYearlyAverageRecords(extract._2)))

    println("Extraction completed")
    /* val generateImage =
       (year: Int, zoom: Int, x: Int, y: Int, data: Iterable[(Location, Double)]) => {

         println("before Visualize")
         val image = Interaction.tile(data, scale, zoom, x, y)
         image.output(new File(s"$year.jpeg"))(JpegWriter())
         println("Visualize completed")

       }: Unit


     Interaction.generateTiles[Iterable[(Location, Double)]](yearlyData, generateImage)*/

    val zoom = 0
    val x = 0
    val y = 0
    val image = Interaction.tile(yearlyData.head._2, scale, zoom, x, y)

    val directoryURL = s"target/temperatures/$year/$zoom/"

    val directory = new File(directoryURL)
    if (!directory.exists()) {
      directory.mkdirs()
    }
    image.output(new File(s"$directoryURL/$x-$y.jpeg"))(JpegWriter())

    println("Interaction completed")

    /*


    val out =
      Extraction.locateTemperatures(year, "/stations.csv", s"/$year.csv")


    println("locateTemperatures done")

    val result = Extraction.locationYearlyAverageRecords(out)
    println("locationYearlyAverageRecords done")


    val image = Visualization.visualize(result, scale)

    image.output(new File(s"$year.jpeg"))(JpegWriter())
    println("end")*/
    /*val zoom = 2
    val tiles = (Math.pow(2, zoom) * Math.pow(2, zoom)).toInt

    val tilesIndex =
      for {
        x <- 0 until 256 * Math.pow(2, zoom).toInt by 256
        y <- 0 until 256 * Math.pow(2, zoom).toInt by 256
      } yield (x, y)
*/
    /*   val values =
         for {
           zoom <- 0 to 3
           x <- 0 until 256 * Math.pow(2, zoom).toInt by 256
           y <- 0 until 256 * Math.pow(2, zoom).toInt  by 256
         } yield (zoom, x, y)*/

    println("hello")
  }

}

/*
* val locationsTemperatures = List(
        (new Location(45.0 , -90.0), 20.0)
        ,(new Location(45.0 , 90.0 ), 0.0)
        ,(new Location(0.0  , 0.0  ), 10.0)
        ,(new Location(-45.0, -90.0), 0.0)
        ,(new Location(-45.0, 90.0 ), 20.0)
    )

val colorMap = List(
         (0.0  , Color(255, 0  , 0))
        ,(10.0 , Color(0  , 255, 0))
        ,(20.0 , Color(0  , 0  , 255))
    )
* */