package com.diego_ramos.croche_api.common.base

import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class BaseEntity(
    open val id: Uuid = Uuid.random()
) {
    var createdAt: Instant = Instant.now()
        protected set

    var updatedAt: Instant = createdAt
        protected set

    protected fun markAsUpdated() {
        updatedAt = Instant.now()
    }
}