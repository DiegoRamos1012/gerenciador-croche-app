package com.diego_ramos.croche_api.modules.order.error

import com.diego_ramos.croche_api.common.result.ResultError

object OrderErrors {

    val CustomerNameRequired = ResultError(
        code = "ORDER_CUSTOMER_REQUIRED",
        message = "O nome do cliente é obrigatório."
    )

    val EmptyItems = ResultError(
        code = "ORDER_EMPTY_ITEMS",
        message = "O pedido deve conter pelo menos um item."
    )

    val NotFound = ResultError(
        code = "ORDER_NOT_FOUND",
        message = "Pedido não encontrado."
    )

    val AlreadyCancelled = ResultError(
        code = "ORDER_ALREADY_CANCELLED",
        message = "O pedido já está cancelado."
    )

    val ProductNotFound = ResultError(
        code = "ORDER_PRODUCT_NOT_FOUND",
        message = "Um dos produtos selecionados não foi encontrado."
    )

    val ProductInactive = ResultError(
        code = "ORDER_PRODUCT_INACTIVE",
        message = "Não é possível vender um produto inativo."
    )
}
