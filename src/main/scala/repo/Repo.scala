package repo

import com.datastax.driver.core.Session
import model.PreparedStatements
import util.RichListenableFuture


class Repo(session: Session, preparedStatements: PreparedStatements, isTest: Boolean = false)
  extends RichListenableFuture with RepoMapper with RepoBinder {


  val ps: PreparedStatements = preparedStatements
  val isUnitTest: Boolean = isTest

}
