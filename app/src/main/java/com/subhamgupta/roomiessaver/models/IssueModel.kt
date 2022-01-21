package com.subhamgupta.roomiessaver.models

import java.io.Serializable

data class IssueModel(var ISSUE: String? = null,
                 var TIME: String? = null,
                 var PERSON_TO: String? = null,
                 var PERSON_FROM: String? = null) : Serializable