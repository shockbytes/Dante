package at.shockbytes.dante.core.data.local

class RealmBookLabelDeletionException(
        labelTitle: String
) : Exception("Could not delete label from book for label title=<$labelTitle>")