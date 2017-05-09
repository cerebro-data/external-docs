// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package example

import com.cloudera.recordservice.mr.RecordServiceConfig
import com.cloudera.recordservice.spark.RecordServiceConf

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
 * A simple Spark app that shows how to use DataFrame with RecordService
 * To run this example, do:
 *
 * <pre>
 * {@code
 *   spark-submit \
 *     --class example.DataFrameExample \
 *     --master <master-url> \
 *     /path/to/recordservice-spark-2.x-example-<version>.jar
 * }
 * </pre>
 */
object DataFrameExample {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("DataFrameExample")

    // Set up the RecordService planner to use. Although in this case it isn't
    // necessary since it's the default value.
    RecordServiceConf.setSparkConf(sparkConf,
      RecordServiceConfig.ConfVars.PLANNER_HOSTPORTS_CONF, "localhost:12050")

    // Initialize SQLContext
    val ctx = new SparkContext(sparkConf)
    val sc = new SQLContext(ctx)

    // Create a DataFrame and apply operations on it:
    // SELECT n_regionkey, count(*) FROM tpch.nation GROUP BY 1 ORDER BY 1
    val df = sc.load("tpch.nation", "com.cloudera.recordservice.spark.DefaultSource")
    val results = df.groupBy("n_regionkey").count().orderBy("n_regionkey").collect()

    // Print the result rows, and stop the SparkContext.
    println("Result:")
    for (row <- results) println(row)
    ctx.stop()
  }
}
