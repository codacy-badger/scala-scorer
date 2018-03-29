package simplepredicate

object SimplePredicate {
  def setFromXml(xml_predicate: scala.xml.Elem): SimplePredicate = {
    val field: String = (xml_predicate \ "@field").text
    val operator: String = (xml_predicate \ "@operator").text
    val stringValue: String = (xml_predicate \ "@value").text
    val predicateValue = try { NumericalValue(stringValue.toDouble) } catch { case _ => CategoricalValue(stringValue) }
    new SimplePredicate(field, operator, predicateValue)
  }
}

sealed trait Value
final case class CategoricalValue(b: String) extends Value
final case class NumericalValue(a: Double) extends Value

class SimplePredicate(field: String, operator: String, predicateValue: Value) {
  val GREATER_THAN     = "greaterThan"
  val LESS_THAN        = "lessThan"
  val LESS_OR_EQUAL    = "lessOrEqual"
  val GREATER_OR_EQUAL = "greaterOrEqual"
  val EQUALS           = "equal"
  val IS_MISSING       = "isMissing"
  val MATH_OPS = Array(GREATER_THAN, LESS_THAN, LESS_OR_EQUAL, GREATER_OR_EQUAL)

  def isTrue(features: Map[String, Value]): Either[String, Boolean] = {
    val inputValue = features.get(field)
    predicateValue match {
      case NumericalValue(numericalValue) =>
        isTrueNum(inputValue, numericalValue)
      case CategoricalValue(categoricalValue) =>
        isTrueStr(inputValue, categoricalValue)
    }
  }

  private def isTrueNum(inputValue: Option[Value], predicateValue: Double) = {
    inputValue match {
      case Some(NumericalValue(numericalValue)) =>
        operator match {
          case GREATER_THAN =>
            Right(numericalValue > predicateValue)
          case LESS_THAN =>
            Right(numericalValue < predicateValue)
          case GREATER_OR_EQUAL =>
            Right(numericalValue >= predicateValue)
          case LESS_OR_EQUAL =>
            Right(numericalValue <= predicateValue)
          case EQUALS =>
            Right(numericalValue == predicateValue)
        }
      case Some(CategoricalValue(_)) =>
        Left("Expected Numerical Feature")
      case None =>
        Left("Missing Numerical Value")
    }
  }

  private def isTrueStr(inputValue: Option[Value], predicateValue: String) = {
    inputValue match {
      case Some(CategoricalValue(categoricalValue)) =>
        operator match {
          case IS_MISSING =>
            Right(categoricalValue == "")
          case EQUALS =>
            Right(categoricalValue == predicateValue)
        }
      case Some(NumericalValue(_)) =>
        Right(false)
      case None =>
        operator match {
          case IS_MISSING =>
            Right(inputValue.isEmpty)
        }
    }
  }
}
