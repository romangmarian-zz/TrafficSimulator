package repo

import com.datastax.driver.core.Session
import model.PreparedStatements
import model.QueryConstants._

trait RepoInit {

  def initializePreparedStatements(session: Session): PreparedStatements = {
    PreparedStatements()
  }


}
