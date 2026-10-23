package com.diego_ramos.croche_api.modules.order.error

import com.diego_ramos.croche_api.common.result.ResultError

object OrderErrors { // Object cria um singleton
    val InvalidValue = ResultError(
        code = "ORDER_INVALID_VALUE",
        message = "O valor do pedido deve ser maior que zero."
    )

    val CustomerRequired = ResultError(
        code = "ORDER_CLIENT_REQUIRED",
        message = "O nome do cliente é obrigatório."
    )

    val OrderNotFound = ResultError(
        code = "ORDER_NOT_FOUND",
        message = "Pedido não encontrado."
    )
}