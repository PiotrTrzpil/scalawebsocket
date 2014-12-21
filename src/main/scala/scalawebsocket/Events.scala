package scalawebsocket

class EventSource[A](handlers: scala.collection.mutable.ListBuffer[A => Unit]) {
   def +=(handler : A => Unit): this.type = {
      handlers += handler
      this
   }
   def -=(handler : A => Unit): this.type = {
      handlers -= handler
      this
   }
}

class EventPublisher[A] {

   private val handlers = scala.collection.mutable.ListBuffer[A => Unit]()
   val source = new EventSource[A](handlers)

   def publish(event: A) {
      handlers.foreach(_(event))
   }
}