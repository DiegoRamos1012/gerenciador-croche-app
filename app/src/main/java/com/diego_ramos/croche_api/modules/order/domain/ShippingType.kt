package com.diego_ramos.croche_api.modules.order.domain

enum class ShippingType(val label: String) {
    ENTREGA_PESSOAL("Entrega Pessoal"),
    CORREIOS("Correios"),
    APLICATIVO("Aplicativo")
}
