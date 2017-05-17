package observatory

/**
  *
  * @author Radwa Osama
  * @since 5/8/2017
  */
object Common {

  import org.apache.log4j.{Level, Logger}
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  import org.apache.spark.sql.SparkSession

  val sparkSession: SparkSession =
    SparkSession
            .builder()
            .appName("Observatory")
            .config("spark.master", "local")
            .getOrCreate()

  val WIDTH = 360
  val HEIGHT = 180

  val TILE_SIZE = 256

  // For implicit conversions like converting RDDs to DataFrames
  import org.apache.spark.sql.{Encoder, Encoders}
  import scala.reflect.ClassTag

  implicit def single[A](implicit c: ClassTag[A]): Encoder[A] = Encoders.kryo[A](c)

  implicit def tuple2[A1, A2](
                                     implicit e1: Encoder[A1],
                                     e2: Encoder[A2]
                             ): Encoder[(A1, A2)] = Encoders.tuple[A1, A2](e1, e2)

  implicit def tuple3[A1, A2, A3](
                                         implicit e1: Encoder[A1],
                                         e2: Encoder[A2],
                                         e3: Encoder[A3]
                                 ): Encoder[(A1, A2, A3)] = Encoders.tuple[A1, A2, A3](e1, e2, e3)
}