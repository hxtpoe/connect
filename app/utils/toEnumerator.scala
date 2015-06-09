package utils

class test {
//  implicit class ObservableExts[A](val observable: Observable[A])
//    extends AnyVal {
//    def toEnumerator(implicit ec: ExecutionContext) = Enumerator.generateM {
//      val promise = Promise[Option[A]]
//      val subscription = observable.subscribe(
//        v => promise.trySuccess(Some(v)),
//        e => promise.tryFailure(e),
//        () => promise.trySuccess(None)
//      )
//      promise.future.onComplete(_ => subscription.unsubscribe())
//
//      promise.future
//    }
//
//  }

}
