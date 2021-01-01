package at.shockbytes.dante.core.login

class UpgradeException(cause: Exception?) : Exception(cause?.message ?: "Unknown exception cause")