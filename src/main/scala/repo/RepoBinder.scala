package repo

import com.datastax.driver.core.BoundStatement
import model._


trait RepoBinder {
  val ps: PreparedStatements
}


