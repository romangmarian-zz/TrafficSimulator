package util

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import scala.concurrent.{Future, Promise}

//TODO look into this
/**
  * Wrapper to return a Scala's Future[ResultSet] instead of java's ResultSetFuture
  */
trait RichListenableFuture {
  val isUnitTest: Boolean

  /**
    * @param lf Java Future
    * @tparam ResultSet ResultSetFuture
    */
  implicit class RichListenableFuture[ResultSet](lf: ListenableFuture[ResultSet]) {
    /**
      *
      * @return Scala Future of ResultSet
      */
    def asScalaFuture: Future[ResultSet] = {
      if (isUnitTest)
        return Future.successful(lf.get)

      val p = Promise[ResultSet]()

      Futures.addCallback(lf, new FutureCallback[ResultSet] {
        def onFailure(ex: Throwable): Unit = {
          p failure ex
        }

        def onSuccess(result: ResultSet): Unit = {
          p success result
        }
      })

      p.future
    }
  }
}
