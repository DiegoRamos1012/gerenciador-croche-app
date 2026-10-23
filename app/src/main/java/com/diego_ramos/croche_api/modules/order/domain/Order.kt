package com.diego_ramos.croche_api.modules.order.domain

import com.diego_ramos.croche_api.common.base.BaseEntity
import com.diego_ramos.croche_api.common.exception.DomainException
import com.diego_ramos.croche_api.common.util.TextNormalizer
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class Order private constructor(
    val clientName: String
) : BaseEntity() {

    companion object {
        fun create(clientName: String): Order {
            if (clientName.isBlank()) {
                throw DomainException("O nome do cliente é obrigatório.")
            }

            return Order(
                clientName = TextNormalizer.normalizeName(clientName)
            )
        }
    }
}