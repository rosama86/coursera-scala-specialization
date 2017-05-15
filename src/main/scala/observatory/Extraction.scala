package observatory

import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import java.lang.Double.{valueOf => DoubleValueOf}
import java.lang.Integer.{valueOf => IntegerValueOf}

/**
  * 1st milestone: data extraction
  */
object Extraction {

  // Common Spark configurations
  import observatory.Common._

  // For implicit conversions like converting RDDs to DataFrames
  import sparkSession.implicits._

  // Stations Schema
  val stationSchema = StructType(
    StructField("stn", StringType, nullable = true) ::
      StructField("wban", StringType, nullable = true) ::
      StructField("latitude", DoubleType, nullable = true) ::
      StructField("longitude", DoubleType, nullable = true) ::
      Nil
  )

  // temperatures Schema
  val temperaturesSchema = StructType(
    StructField("stn", StringType, nullable = true) ::
      StructField("wban", StringType, nullable = true) ::
      StructField("month", IntegerType, nullable = true) ::
      StructField("day", IntegerType, nullable = true) ::
      StructField("temperature", DoubleType, nullable = true) ::
      Nil
  )

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Double)] = {

    // Read station DF
    val stationsDF = readStations(stationsFile)
    // Read temperature DF
    val temperatureDF = readTemperatures(temperaturesFile)

    implicit val LocalDateEncoder = org.apache.spark.sql.Encoders.kryo[LocalDate]

    val result =
      temperatureDF
        .join(stationsDF,
          temperatureDF.col("stn") === stationsDF.col("stn") && temperatureDF.col("wban") === stationsDF.col("wban")
        )
        .map(row => {

          val month: Int = row.getAs[Int]("month")
          val day: Int = row.getAs[Int]("day")
          val localDate = LocalDate.of(year, month, day)

          val latitude: Double = row.getAs[Double]("latitude")
          val longitude: Double = row.getAs[Double]("longitude")
          val location = Location(latitude, longitude)

          val temperature: Double = row.getAs[Double]("temperature")

          (localDate, location, temperature)
        }
        )
        .filter(filterRow(_))
        .collect()

    result
  }

  /** Filter stations with no location data */
  def filterRow(row: (LocalDate, Location, Double)) = row._2.lat != 0 && row._2.lat != 0

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {
    records
      .groupBy(entry => entry._2)
      .map(group => (group._1, group._2.map(_._3).sum / group._2.map(_._3).size))
  }

  /** Read data frame */
  def read(resource: String, schema: StructType, row: List[String] => Row): DataFrame = {
    val rdd = sparkSession.sparkContext.textFile(fsPath(resource))

    val data =
      rdd
        .map(_.split(",").to[List])
        .map(row)

    sparkSession.createDataFrame(data, schema)
  }

  /** Read as data frame of station row */
  def readStations(resource: String) = read(resource, stationSchema, stationRow).as[StationRow]

  /** Read as data frame of temperature row */
  def readTemperatures(resource: String) = read(resource, temperaturesSchema, temperaturesRow).as[TemperatureRow]

  /** @return The filesystem path of the given resource */
  def fsPath(resource: String): String =
    Paths.get(getClass.getResource(resource).toURI).toString

  /** @return An RDD Row
    * @param line Raw fields
    * */
  def stationRow(line: List[String]): Row = {
    val stn: String = if (line.isEmpty) "" else line.head
    val wbn: String = if (line.length > 1) line(1) else ""
    val lat: Double = if (line.length > 2) DoubleValueOf(line(2)) else 0
    val lon: Double = if (line.length > 3) DoubleValueOf(line(3)) else 0
    Row.fromSeq(stn :: wbn :: lat :: lon :: Nil)
  }

  /** @return An RDD Row
    * @param line Raw fields
    * */
  def temperaturesRow(line: List[String]): Row = {
    def round1(num: Double): Double = (num * 10).round / 10d

    val stn: String = if (line.isEmpty) "" else line.head
    val wbn: String = if (line.length > 1) line(1) else ""
    val mon: Int = if (line.length > 2) IntegerValueOf(line(2)) else 0
    val day: Int = if (line.length > 3) IntegerValueOf(line(3)) else 0
    val tem: Double = if (line.length > 4) round1((DoubleValueOf(line(4)) - 32) / 1.8) else 0
    Row.fromSeq(stn :: wbn :: mon :: day :: tem :: Nil)
  }

}