package playground

object Playground extends App {

  val m = Map[String, Int]()

  def addElem(e: (String, Int), acc: Map[String, Int]): Map[String, Int] =
    if (acc.isDefinedAt(e._1)) acc.filter(_._1 != e._1) + (e._1 -> (acc(e._1) + 1))
    else acc + e

  def addElemOption(e: Option[String], acc: Map[String, Int]): Map[String, Int] =
    if (e.isDefined)
      if (acc.isDefinedAt(e.get)) (acc filter (_._1 != e.get)) + (e.get -> (acc(e.get) + 1))
      else acc + (e.get -> 1)
    else acc


  println(
    addElem("b" -> 1, addElem("a" -> 1, addElem("a" -> 1, m)))
  )

  println(
    addElemOption(None, addElemOption(Some("b"), addElemOption(Some("a"), addElemOption(Some("a"), m))))
  )


}
