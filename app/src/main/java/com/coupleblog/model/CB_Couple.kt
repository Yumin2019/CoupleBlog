package com.coupleblog.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CB_Couple(
    var strCoupleAUid: String? = "",
    var strCoupleBUid: String? = "",
    )

// past-event-list   / eventKey 1 / event
// future-event-list / eventKey 1 / event
// annual-event-list / eventKey 1 / event