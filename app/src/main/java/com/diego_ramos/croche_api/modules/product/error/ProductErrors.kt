package com.diego_ramos.croche_api.modules.product.error

import com.diego_ramos.croche_api.common.result.ResultError

object ProductErrors {

    val NameRequired = ResultError(
        code = "PRODUCT_NAME_REQUIRED",
        message = "O nome do produto é obrigatório."
    )

    val DescriptionTooLong = ResultError(
        code = "PRODUCT_DESCRIPTION_TOO_LONG",
        message = "A descrição não pode passar de 300 caracteres."
    )

    val ColorRequired = ResultError(
        code = "PRODUCT_COLOR_REQUIRED",
        message = "A cor do produto é obrigatória."
    )

    val SizeRequired = ResultError(
        code = "PRODUCT_SIZE_REQUIRED",
        message = "O tamanho do produto é obrigatório."
    )

    val InvalidPrice = ResultError(
        code = "PRODUCT_INVALID_PRICE",
        message = "O preço deve ser maior que zero."
    )

    val AlreadyExists = ResultError(
        code = "PRODUCT_ALREADY_EXISTS",
        message = "Já existe um produto com essas características."
    )

    val NotFound = ResultError(
        code = "PRODUCT_NOT_FOUND",
        message = "Produto não encontrado."
    )

    val AlreadyDeactivated = ResultError(
        code = "PRODUCT_ALREADY_DEACTIVATED",
        message = "O produto já está desativado."
    )

    val AlreadyActive = ResultError(
        code = "PRODUCT_ALREADY_ACTIVE",
        message = "O produto já está ativo."
    )

    val IsInactive = ResultError(
        code = "PRODUCT_IS_INACTIVE",
        message = "Não é possível realizar esta operação com um produto desativado."
    )

    val CannotBeDeleted = ResultError(
        code = "PRODUCT_CANNOT_BE_DELETED",
        message = "O produto não pode ser excluído definitivamente."
    )
}