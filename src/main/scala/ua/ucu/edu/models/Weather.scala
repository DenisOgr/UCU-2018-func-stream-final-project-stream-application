package ua.ucu.edu.models

case class Weather(
  temp: BigDecimal,
  pressure: BigDecimal,
  temp_min: BigDecimal,
  temp_max: BigDecimal,
  wind_speed: BigDecimal,
  clouds: BigInt,
  humidity: BigDecimal
)
