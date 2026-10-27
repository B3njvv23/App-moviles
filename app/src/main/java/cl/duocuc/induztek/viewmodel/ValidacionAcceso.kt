package cl.duocuc.induztek.viewmodel

object ValidacionAcceso {
    fun validar(registro: Boolean, v: Map<String, String>): Map<String, String> = buildMap {
        fun valor(k: String) = v[k].orEmpty()
        val campos =
            if (registro) listOf("nombre", "rut", "empresa", "correo", "clave", "confirmar")
            else listOf("correo", "codigo", "clave", "confirmar")
        campos.forEach { if (valor(it).isBlank()) put(it, "Completa este campo") }
        if (valor("correo").isNotBlank() && !Reglas.correo(valor("correo").trim()))
            put("correo", "Ingresa un correo válido")
        if (registro) {
            listOf("rut", "empresa").forEach {
                if (valor(it).isNotBlank() && !Reglas.rut(valor(it)))
                    put(it, "Ingresa un RUT válido con dígito verificador")
            }
            if (valor("nombre").isNotBlank() && valor("nombre").trim().length < 3)
                put("nombre", "Ingresa tu nombre completo")
        }
        if (valor("clave").isNotBlank() && valor("clave").length < 10)
            put("clave", "Usa al menos 10 caracteres")
        if (valor("confirmar").isNotBlank() && valor("confirmar") != valor("clave"))
            put("confirmar", "Las contraseñas no coinciden")
    }
}
