package exceptions


case class PowerStationNotFoundException(powerStationId: Long) extends RuntimeException
