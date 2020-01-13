package util

import com.datastax.driver.core._

import scala.util.{Failure, Success, Try}

//TODO modifica rahaturile de aici
class DBConfiguration {
  //  private val defaultDBPort: Int = 9042
  //  private val cassandraConfig: Config = config.getConfig("cassandra.config")
  //  private val dbUri: Uri = Uri(cassandraConfig.getString("uri"))
  //  private val dbHosts: List[String] = Uri.Query(dbUri.rawQueryString).getAll("host").::(dbUri.authority.host.toString())
  //  private val dbPort: Int = Some(dbUri.authority.port).getOrElse(defaultDBPort)
  //  private val keyspace: String = dbUri.path.toString.replaceFirst("/", "")
  //  private val localDataCenter: String = config.getString("drpDataCenter")
  //  private val minConnectionPool: Int = cassandraConfig.getInt("pool.min-connection")
  //  private val maxConnectionPool: Int = cassandraConfig.getInt("pool.max-connection")
  //  private val localMaxReqPerConnection: Int = cassandraConfig.getInt("pool.local-max-requests-per-connection")
  //  private val remoteMaxReqPerConnection: Int = cassandraConfig.getInt("pool.remote-max-requests-per-connection")
  //  private val maxQueueSize: Int = cassandraConfig.getInt("pool.max-queue-size")

  //  private val (dbUser: String, dbPass: String) = Some(dbUri.authority.userinfo) match {
  //    case Some(userInfo: String) => userInfo.split(":") match {
  //      case Array(user: String, pass: String) => (user, pass)
  //
  //      case Array(user: String) => (user, "")
  //    }
  //  }

  private val cluster: Cluster = {
    Cluster.builder()
      .addContactPoints("localhost")
      .withPort(9042)
      .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
      //      .withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build())
      //      .withPoolingOptions(new PoolingOptions()
      //        .setMaxRequestsPerConnection(HostDistance.LOCAL, localMaxReqPerConnection)
      //        .setMaxRequestsPerConnection(HostDistance.REMOTE, remoteMaxReqPerConnection)
      //        .setMaxQueueSize(maxQueueSize)
      //        .setConnectionsPerHost(HostDistance.LOCAL, minConnectionPool, maxConnectionPool)
      //        .setConnectionsPerHost(HostDistance.REMOTE, minConnectionPool, maxConnectionPool))
      .build()
  }

  def getSession: Session = {
    Try {
      cluster.connect("movie_local")
    } match {
      case Success(session: Session) => session

      case Failure(ex: Throwable) =>
        // TraceLogger.error(s"Error in creating session: ${ex.getMessage}")
        ex.printStackTrace()
        throw ex
    }
  }
}

object DBConfiguration {
  def apply(): DBConfiguration = new DBConfiguration
}
