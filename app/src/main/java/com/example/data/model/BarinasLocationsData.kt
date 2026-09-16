package com.example.data.model

data class BarinasLocation(
    val id: String,
    val name: String,
    val type: String, // "Urbanización", "Barrio", "Avenida", "Sector", "Municipio"
    val parroquia: String,
    val block: String, // "Bloque A", "Bloque B", "Bloque C", "Bloque D"
    val circuitCode: String,
    val sectorEntityId: String,
    val description: String,
    val keywords: List<String>
)

object BarinasLocationsCatalog {

    val LOCATIONS: List<BarinasLocation> = listOf(
        // ==========================================
        // PARROQUIA EL CARMEN
        // ==========================================
        BarinasLocation(
            id = "loc_cincuentena_3",
            name = "Urb. La Cincuentena III",
            type = "Urbanización",
            parroquia = "El Carmen",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Sector La Cincuentena Etapa III, Parroquia El Carmen",
            keywords = listOf("cincuentena", "cincuentena 3", "cincuentena iii", "el carmen", "corocito")
        ),
        BarinasLocation(
            id = "loc_cincuentena_1_2",
            name = "Urb. La Cincuentena I y II",
            type = "Urbanización",
            parroquia = "El Carmen",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Etapas I y II, Parroquia El Carmen",
            keywords = listOf("cincuentena", "cincuentena 1", "cincuentena 2", "el carmen")
        ),
        BarinasLocation(
            id = "loc_barrio_el_carmen",
            name = "Barrio El Carmen",
            type = "Barrio",
            parroquia = "El Carmen",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Casco del Barrio El Carmen y calles aledañas",
            keywords = listOf("el carmen", "barrio el carmen", "carmen", "cincuentena")
        ),

        // ==========================================
        // PARROQUIA ALTO BARINAS
        // ==========================================
        BarinasLocation(
            id = "loc_don_samuel",
            name = "Urb. Don Samuel",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Sectores I, II y III, Av. Principal Don Samuel",
            keywords = listOf("don samuel", "samuel", "alto barinas", "farmatodo", "el dorado")
        ),
        BarinasLocation(
            id = "loc_alto_barinas_norte",
            name = "Alto Barinas Norte",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel / Norte",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Av. Alberto Arvelo Torrealba, Alrededores C.C. Cima",
            keywords = listOf("alto barinas norte", "arvelo torrealba", "cima", "garzon", "norte")
        ),
        BarinasLocation(
            id = "loc_los_proceres",
            name = "Urb. Los Próceres",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Norte",
            sectorEntityId = "sec_a_norte",
            description = "Etapas I, II y III, cercana a la Av. Los Próceres",
            keywords = listOf("los proceres", "proceres", "alto barinas", "etapa")
        ),
        BarinasLocation(
            id = "loc_las_ingenieras_1",
            name = "Urb. Las Ingenieras I",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Norte",
            sectorEntityId = "sec_a_norte",
            description = "Sector Las Ingenieras Etapa I, Alto Barinas",
            keywords = listOf("las ingenieras", "las ingenieras 1", "las ingenieras i", "ingenieras", "alto barinas")
        ),
        BarinasLocation(
            id = "loc_las_ingenieras_2",
            name = "Urb. Las Ingenieras II",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Norte",
            sectorEntityId = "sec_a_norte",
            description = "Sector Las Ingenieras Etapa II, Alto Barinas",
            keywords = listOf("las ingenieras", "las ingenieras 2", "las ingenieras ii", "ingenieras", "alto barinas")
        ),
        BarinasLocation(
            id = "loc_las_ingenieras_3",
            name = "Urb. Las Ingenieras III",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Norte",
            sectorEntityId = "sec_a_norte",
            description = "Sector Las Ingenieras Etapa III, Alto Barinas",
            keywords = listOf("las ingenieras", "las ingenieras 3", "las ingenieras iii", "ingenieras", "ingenieras 3", "alto barinas")
        ),
        BarinasLocation(
            id = "loc_la_rosaleda",
            name = "Urb. La Rosaleda",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Alto Barinas I",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Sector residencial La Rosaleda",
            keywords = listOf("la rosaleda", "rosaleda", "alto barinas")
        ),
        BarinasLocation(
            id = "loc_vista_hermosa",
            name = "Urb. Vista Hermosa",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Zona residencial Vista Hermosa",
            keywords = listOf("vista hermosa", "alto barinas")
        ),
        BarinasLocation(
            id = "loc_villas_del_pilar",
            name = "Urb. Villas del Pilar",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Alto Barinas I",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Conjunto Residencial Villas del Pilar",
            keywords = listOf("villas del pilar", "del pilar", "pilar")
        ),
        BarinasLocation(
            id = "loc_campo_movil",
            name = "Urb. Campo Móvil",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Norte",
            sectorEntityId = "sec_a_norte",
            description = "Alto Barinas cerca de la UNELLEZ",
            keywords = listOf("campo movil", "campo movil alto barinas")
        ),
        BarinasLocation(
            id = "loc_terrazas_cuatricentenario",
            name = "Terrazas del Cuatricentenario",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Alto Barinas terrazas residenciales",
            keywords = listOf("terrazas del cuatricentenario", "terrazas", "cuatricentenario")
        ),
        BarinasLocation(
            id = "loc_silvia_sofia",
            name = "Urb. Silvia Sofía",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Alto Barinas I",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Alto Barinas detrás del C.C. Doña Grazzia",
            keywords = listOf("silvia sofia", "dona grazzia")
        ),
        BarinasLocation(
            id = "loc_la_mansion",
            name = "Urb. La Mansión",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Cercanías de la Av. Francia y Don Samuel",
            keywords = listOf("la mansion", "mansion")
        ),
        BarinasLocation(
            id = "loc_farmacia",
            name = "Urb. Farmacia",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Sector Colegio de Farmacéuticos",
            keywords = listOf("farmacia", "colegio de farmaceuticos")
        ),
        BarinasLocation(
            id = "loc_palma_de_oro",
            name = "Urb. Palma de Oro",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque A",
            circuitCode = "Circuito Don Samuel",
            sectorEntityId = "sec_a_alto_barinas_1",
            description = "Conjunto cerrado Palma de Oro",
            keywords = listOf("palma de oro", "palma oro")
        ),
        BarinasLocation(
            id = "loc_alto_barinas_sur",
            name = "Alto Barinas Sur",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque C",
            circuitCode = "Circuito Alto Barinas II 34,5 kV",
            sectorEntityId = "sec_c_alto_barinas_2",
            description = "Av. Los Andes, Av. Francia Sur, Farmatodo Sur",
            keywords = listOf("alto barinas sur", "av francia", "av los andes", "sur")
        ),
        BarinasLocation(
            id = "loc_raul_leoni",
            name = "Urb. Raúl Leoni",
            type = "Urbanización",
            parroquia = "Alto Barinas / R. Méndez",
            block = "Bloque A",
            circuitCode = "Circuito Raúl Leoni",
            sectorEntityId = "sec_a_raul_leoni",
            description = "Urb. Raúl Leoni, canchas y zona escolar",
            keywords = listOf("raul leoni", "leoni", "los pozones")
        ),

        // ==========================================
        // CIUDAD VARYNA & CIUDAD TAVACARE
        // ==========================================
        BarinasLocation(
            id = "loc_cdad_varyna_samanes",
            name = "Ciudad Varyna - Los Samanes",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Varyna",
            sectorEntityId = "sec_b_cdad_varyna",
            description = "Sector Los Samanes, Ciudad Varyna",
            keywords = listOf("ciudad varyna", "varyna", "los samanes", "samanes")
        ),
        BarinasLocation(
            id = "loc_cdad_varyna_bucares",
            name = "Ciudad Varyna - Los Bucares",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Varyna",
            sectorEntityId = "sec_b_cdad_varyna",
            description = "Sector Los Bucares, Ciudad Varyna",
            keywords = listOf("ciudad varyna", "varyna", "los bucares", "bucares")
        ),
        BarinasLocation(
            id = "loc_cdad_varyna_palmas",
            name = "Ciudad Varyna - Las Palmas",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Varyna",
            sectorEntityId = "sec_b_cdad_varyna",
            description = "Sector Las Palmas, Ciudad Varyna",
            keywords = listOf("ciudad varyna", "varyna", "las palmas")
        ),
        BarinasLocation(
            id = "loc_cdad_varyna_chaguaramos",
            name = "Ciudad Varyna - Chaguaramos",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Varyna",
            sectorEntityId = "sec_b_cdad_varyna",
            description = "Sector Los Chaguaramos, Ciudad Varyna",
            keywords = listOf("ciudad varyna", "varyna", "chaguaramos")
        ),
        BarinasLocation(
            id = "loc_cdad_tavacare_a",
            name = "Ciudad Tavacare - Terraza A",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Tavacare",
            sectorEntityId = "sec_b_cdad_tavacare",
            description = "Terraza A, entrada principal Ciudad Tavacare",
            keywords = listOf("ciudad tavacare", "tavacare", "terraza a")
        ),
        BarinasLocation(
            id = "loc_cdad_tavacare_b",
            name = "Ciudad Tavacare - Terraza B",
            type = "Urbanización",
            parroquia = "Alto Barinas",
            block = "Bloque B",
            circuitCode = "Circuito Cdad Tavacare",
            sectorEntityId = "sec_b_cdad_tavacare",
            description = "Terraza B y C, Bloques de apartamentos",
            keywords = listOf("ciudad tavacare", "tavacare", "terraza b", "terraza c")
        ),

        // ==========================================
        // PARROQUIA BARINAS (CASCO CENTRAL)
        // ==========================================
        BarinasLocation(
            id = "loc_casco_central",
            name = "Centro - Casco Central",
            type = "Sector",
            parroquia = "Barinas Centro",
            block = "Bloque A",
            circuitCode = "Circuito Centro",
            sectorEntityId = "sec_a_centro",
            description = "Plaza Bolívar, Gobernación, Alcaldía, Catedral",
            keywords = listOf("centro", "casco central", "plaza bolivar", "catedral", "gobernacion")
        ),
        BarinasLocation(
            id = "loc_calle_camejo",
            name = "Calle Camejo (Centro)",
            type = "Avenida",
            parroquia = "Barinas Centro",
            block = "Bloque A",
            circuitCode = "Circuito Centro",
            sectorEntityId = "sec_a_centro",
            description = "Desde Av. 23 de Enero hasta el Río Santo Domingo",
            keywords = listOf("calle camejo", "camejo", "centro")
        ),
        BarinasLocation(
            id = "loc_calle_cedeno",
            name = "Calle Cedeño (Centro)",
            type = "Avenida",
            parroquia = "Barinas Centro",
            block = "Bloque A",
            circuitCode = "Circuito Centro",
            sectorEntityId = "sec_a_centro",
            description = "Zona comercial central de la Calle Cedeño",
            keywords = listOf("calle cedeno", "cedeno", "centro")
        ),
        BarinasLocation(
            id = "loc_av_medina_jimenez",
            name = "Av. Medina Jiménez",
            type = "Avenida",
            parroquia = "Barinas Centro",
            block = "Bloque B",
            circuitCode = "Circuito Centro Norte",
            sectorEntityId = "sec_b_centro_norte",
            description = "Eje vial Centro Norte y comercios",
            keywords = listOf("medina jimenez", "centro norte")
        ),
        BarinasLocation(
            id = "loc_calle_cruz_paredes",
            name = "Calle Cruz Paredes",
            type = "Avenida",
            parroquia = "Barinas Centro",
            block = "Bloque A",
            circuitCode = "Circuito Centro",
            sectorEntityId = "sec_a_centro",
            description = "Centro de Barinas, comercios y residencias",
            keywords = listOf("cruz paredes", "calle cruz paredes")
        ),
        BarinasLocation(
            id = "loc_barrio_23_enero",
            name = "Barrio 23 de Enero",
            type = "Barrio",
            parroquia = "Barinas Centro",
            block = "Bloque B",
            circuitCode = "Circuito Centro Norte",
            sectorEntityId = "sec_b_centro_norte",
            description = "Cercano a la Av. 23 de Enero y redoma",
            keywords = listOf("23 de enero", "barrio 23 de enero", "redoma")
        ),
        BarinasLocation(
            id = "loc_barrio_el_carmen",
            name = "Barrio El Carmen",
            type = "Barrio",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Centro",
            sectorEntityId = "sec_a_centro",
            description = "Barrio tradicional El Carmen y mercado",
            keywords = listOf("el carmen", "barrio el carmen")
        ),

        // ==========================================
        // PARROQUIA CORAZÓN DE JESÚS
        // ==========================================
        BarinasLocation(
            id = "loc_la_floresta",
            name = "Urb. La Floresta",
            type = "Urbanización",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Floresta",
            sectorEntityId = "sec_b_floresta",
            description = "Urb. La Floresta, Av. Cuatricentenaria",
            keywords = listOf("la floresta", "floresta", "cuatricentenaria")
        ),
        BarinasLocation(
            id = "loc_los_pinos",
            name = "Sector Los Pinos",
            type = "Sector",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Los Pinos",
            sectorEntityId = "sec_b_los_pinos",
            description = "Sectores I y II Los Pinos",
            keywords = listOf("los pinos", "pinos")
        ),
        BarinasLocation(
            id = "loc_primero_diciembre",
            name = "Barrio 1ero de Diciembre",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque A",
            circuitCode = "Circuito Primero Diciembre",
            sectorEntityId = "sec_a_primero_diciembre",
            description = "Barrio Primero de Diciembre, todas las calles",
            keywords = listOf("1ero de diciembre", "primero de diciembre", "1 de diciembre")
        ),
        BarinasLocation(
            id = "loc_urb_cuatricentenaria",
            name = "Urb. Cuatricentenaria",
            type = "Urbanización",
            parroquia = "Corazón de Jesús",
            block = "Bloque A",
            circuitCode = "Circuito Sur",
            sectorEntityId = "sec_a_sur",
            description = "Sectores adyacentes a la Av. Cuatricentenaria",
            keywords = listOf("cuatricentenaria", "urb cuatricentenaria")
        ),
        BarinasLocation(
            id = "loc_la_cinquena_1",
            name = "Barrio La Cinqueña I",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Floresta",
            sectorEntityId = "sec_b_floresta",
            description = "Sector La Cinqueña I",
            keywords = listOf("la cinquena", "cinquena 1", "cinquena i")
        ),
        BarinasLocation(
            id = "loc_la_cinquena_2",
            name = "Barrio La Cinqueña II",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Los Pinos",
            sectorEntityId = "sec_b_los_pinos",
            description = "Sector La Cinqueña II y ampliación",
            keywords = listOf("la cinquena 2", "cinquena ii", "cinquena")
        ),
        BarinasLocation(
            id = "loc_mi_jardin",
            name = "Barrio Mi Jardín",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Los Pinos",
            sectorEntityId = "sec_b_los_pinos",
            description = "Sectores I, II y III Barrio Mi Jardín",
            keywords = listOf("mi jardin", "jardin")
        ),
        BarinasLocation(
            id = "loc_negro_primero",
            name = "Barrio Negro Primero",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Negro Primero",
            sectorEntityId = "sec_b_negro_primero",
            description = "Barrio Negro Primero cerca del río",
            keywords = listOf("negro primero")
        ),
        BarinasLocation(
            id = "loc_la_hormiga",
            name = "Sector La Hormiga",
            type = "Sector",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Hormiga",
            sectorEntityId = "sec_b_hormiga",
            description = "Sector La Hormiga y adyacencias",
            keywords = listOf("la hormiga", "hormiga")
        ),
        BarinasLocation(
            id = "loc_la_cardenera",
            name = "Sector La Cardenera",
            type = "Sector",
            parroquia = "Corazón de Jesús",
            block = "Bloque C",
            circuitCode = "Circuito Cardenera",
            sectorEntityId = "sec_c_cardenera",
            description = "La Cardenera y vías de conexión",
            keywords = listOf("la cardenera", "cardenera")
        ),
        BarinasLocation(
            id = "loc_la_esperanza",
            name = "Urb. La Esperanza",
            type = "Urbanización",
            parroquia = "Corazón de Jesús",
            block = "Bloque C",
            circuitCode = "Circuito Esperanza",
            sectorEntityId = "sec_c_esperanza",
            description = "Urb. La Esperanza I y II",
            keywords = listOf("la esperanza", "esperanza")
        ),
        BarinasLocation(
            id = "loc_estadio_cuatricentenario",
            name = "Sector Estadio",
            type = "Sector",
            parroquia = "Corazón de Jesús",
            block = "Bloque C",
            circuitCode = "Circuito Estadio",
            sectorEntityId = "sec_c_estadio",
            description = "Estadio Cuatricentenario, IND, canchas",
            keywords = listOf("estadio", "estadio cuatricentenario", "ind")
        ),
        BarinasLocation(
            id = "loc_5_de_julio",
            name = "Barrio 5 de Julio",
            type = "Barrio",
            parroquia = "Corazón de Jesús",
            block = "Bloque B",
            circuitCode = "Circuito Carolina / 23 de Enero",
            sectorEntityId = "sec_b_carolina",
            description = "Barrio 5 de Julio y adyacencias de Av. 23 de Enero",
            keywords = listOf("5 de julio", "cinco de julio", "5 julio", "julio", "corazon de jesus")
        ),
        BarinasLocation(
            id = "loc_barinas_2",
            name = "Urb. Barinas II",
            type = "Urbanización",
            parroquia = "Corazón de Jesús",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Urbanización Barinas II",
            keywords = listOf("barinas 2", "barinas ii", "urb barinas 2", "corazon de jesus")
        ),
        BarinasLocation(
            id = "loc_barinas_4",
            name = "Urb. Barinas IV",
            type = "Urbanización",
            parroquia = "Corazón de Jesús",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Urbanización Barinas IV",
            keywords = listOf("barinas 4", "barinas iv", "urb barinas 4", "corazon de jesus")
        ),
        BarinasLocation(
            id = "loc_punta_gorda",
            name = "Sector Punta Gorda",
            type = "Sector",
            parroquia = "Corazón de Jesús",
            block = "Bloque D",
            circuitCode = "Circuito Punta Gorda",
            sectorEntityId = "sec_d_punta_gorda",
            description = "Sector Punta Gorda y adyacencias",
            keywords = listOf("punta gorda", "gorda", "bloque d")
        ),

        // ==========================================
        // PARROQUIA EL CARMEN & ZONA INDUSTRIAL
        // ==========================================
        BarinasLocation(
            id = "loc_zona_industrial",
            name = "Zona Industrial de Barinas",
            type = "Sector",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Industrial",
            sectorEntityId = "sec_a_industrial",
            description = "Empresas, galpones y Av. Industrial",
            keywords = listOf("zona industrial", "industrial", "galpones")
        ),
        BarinasLocation(
            id = "loc_av_industrial",
            name = "Av. Industrial",
            type = "Avenida",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Industrial",
            sectorEntityId = "sec_a_industrial",
            description = "Toda la extensión de la Av. Industrial",
            keywords = listOf("av industrial", "avenida industrial")
        ),
        BarinasLocation(
            id = "loc_el_progreso",
            name = "Urb. El Progreso",
            type = "Urbanización",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Progreso",
            sectorEntityId = "sec_a_progreso",
            description = "Residencias El Progreso",
            keywords = listOf("el progreso", "progreso")
        ),
        BarinasLocation(
            id = "loc_don_simon_rodriguez",
            name = "Urb. Don Simón Rodríguez",
            type = "Urbanización",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Don Simón",
            sectorEntityId = "sec_a_don_simon",
            description = "Urb. Don Simón Rodríguez",
            keywords = listOf("don simon", "don simon rodriguez", "simon rodriguez")
        ),
        BarinasLocation(
            id = "loc_las_palmas_carmen",
            name = "Sector Las Palmas",
            type = "Sector",
            parroquia = "El Carmen",
            block = "Bloque A",
            circuitCode = "Circuito Las Palmas",
            sectorEntityId = "sec_a_las_palmas",
            description = "Las Palmas El Carmen",
            keywords = listOf("las palmas", "palmas")
        ),
        BarinasLocation(
            id = "loc_palacio_del_pan",
            name = "Palacio del Pan / Av. 23 de Enero",
            type = "Avenida",
            parroquia = "El Carmen",
            block = "Bloque B",
            circuitCode = "Circuito Centro Norte",
            sectorEntityId = "sec_b_centro_norte",
            description = "Sector comercial Palacio del Pan, Av. 23 de Enero",
            keywords = listOf("palacio del pan", "23 de enero", "pan", "avenida 23 de enero", "el carmen")
        ),

        // ==========================================
        // PARROQUIA RAMÓN IGNACIO MÉNDEZ & RÓMULO BETANCOURT
        // ==========================================
        BarinasLocation(
            id = "loc_la_paz",
            name = "Barrio La Paz",
            type = "Barrio",
            parroquia = "Rómulo Betancourt",
            block = "Bloque C",
            circuitCode = "Circuito Esperanza",
            sectorEntityId = "sec_c_esperanza",
            description = "Barrio La Paz, cercano a Av. Cuatricentenaria",
            keywords = listOf("la paz", "paz", "barrio la paz", "romulo betancourt")
        ),
        BarinasLocation(
            id = "loc_mi_jardin_1",
            name = "Mi Jardín Sector 1",
            type = "Sector",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque B",
            circuitCode = "Circuito Hormiga",
            sectorEntityId = "sec_b_hormiga",
            description = "Urbanismo Mi Jardín Sector 1",
            keywords = listOf("mi jardin", "mi jardin 1", "jardin 1", "jardin")
        ),
        BarinasLocation(
            id = "loc_mi_jardin_2",
            name = "Mi Jardín Sector 2",
            type = "Sector",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque B",
            circuitCode = "Circuito Hormiga",
            sectorEntityId = "sec_b_hormiga",
            description = "Urbanismo Mi Jardín Sector 2",
            keywords = listOf("mi jardin", "mi jardin 2", "jardin 2")
        ),
        BarinasLocation(
            id = "loc_mi_jardin_3",
            name = "Mi Jardín Sector 3",
            type = "Sector",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque B",
            circuitCode = "Circuito Hormiga",
            sectorEntityId = "sec_b_hormiga",
            description = "Urbanismo Mi Jardín Sector 3",
            keywords = listOf("mi jardin", "mi jardin 3", "jardin 3")
        ),
        BarinasLocation(
            id = "loc_la_caramuca",
            name = "La Caramuca",
            type = "Sector",
            parroquia = "Manuel Palacio Fajardo",
            block = "Bloque D",
            circuitCode = "Circuito La Caramuca",
            sectorEntityId = "sec_d_caramuca",
            description = "Sector La Caramuca, Troncal 5 entrada sur",
            keywords = listOf("la caramuca", "caramuca", "troncal 5", "sur")
        ),
        BarinasLocation(
            id = "loc_los_pozones",
            name = "Urb. Los Pozones (Páez)",
            type = "Urbanización",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque A",
            circuitCode = "Circuito Raúl Leoni",
            sectorEntityId = "sec_a_raul_leoni",
            description = "Urb. José Antonio Páez (Los Pozones), módulos",
            keywords = listOf("los pozones", "pozones", "jose antonio paez", "paez")
        ),
        BarinasLocation(
            id = "loc_mijagua_1",
            name = "Barrio Mijagua I",
            type = "Barrio",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque A",
            circuitCode = "Circuito Mijagua 34,5 kV",
            sectorEntityId = "sec_a_mijagua",
            description = "Sector Mijagua I y subestación",
            keywords = listOf("mijagua", "mijagua 1", "subestacion mijagua")
        ),
        BarinasLocation(
            id = "loc_mijagua_2_3",
            name = "Barrio Mijagua II y III",
            type = "Barrio",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque A",
            circuitCode = "Circuito Mijagua 34,5 kV",
            sectorEntityId = "sec_a_mijagua",
            description = "Sectores Mijagua II, III y El Molino",
            keywords = listOf("mijagua 2", "mijagua 3", "el molino")
        ),
        BarinasLocation(
            id = "loc_corocito",
            name = "Sector Corocito",
            type = "Sector",
            parroquia = "Ramón Ignacio Méndez",
            block = "Bloque C",
            circuitCode = "Circuito Corocito",
            sectorEntityId = "sec_c_corocito",
            description = "Av. Intercomunal Barinas - Corocito",
            keywords = listOf("corocito", "intercomunal corocito")
        ),
        BarinasLocation(
            id = "loc_fundacea",
            name = "Sector Fundacea / UNELLEZ",
            type = "Sector",
            parroquia = "R. Méndez / Alto Barinas",
            block = "Bloque C",
            circuitCode = "Circuito Fundacea",
            sectorEntityId = "sec_c_fundacea",
            description = "Alrededores del campus UNELLEZ y Fundacea",
            keywords = listOf("fundacea", "unellez", "campus unellez")
        ),
        BarinasLocation(
            id = "loc_guasimito",
            name = "Guasimito / Los Guasimitos",
            type = "Sector",
            parroquia = "Rómulo Betancourt / Obispos",
            block = "Bloque A",
            circuitCode = "Circuito Guasimito",
            sectorEntityId = "sec_a_guasimito",
            description = "Troncal 5 Guasimito, entrada a Barinas",
            keywords = listOf("guasimito", "los guasimitos", "troncal 5")
        ),
        BarinasLocation(
            id = "loc_pagueycito",
            name = "Sector Pagueycito",
            type = "Sector",
            parroquia = "Rómulo Betancourt",
            block = "Bloque A",
            circuitCode = "Circuito Pagueycito",
            sectorEntityId = "sec_a_pagueycito",
            description = "Pagueycito y vía hacia el llano",
            keywords = listOf("pagueycito")
        ),

        // ==========================================
        // MUNICIPIOS Y SUBESTACIONES FORÁNEAS (PAC OFICIAL)
        // ==========================================
        BarinasLocation(
            id = "loc_barinitas_centro",
            name = "Barinitas (Municipio Bolívar)",
            type = "Municipio",
            parroquia = "Barinitas",
            block = "Bloque B",
            circuitCode = "Circuito Barinitas 34,5 kV",
            sectorEntityId = "sec_b_barinitas",
            description = "Barinitas Casco Central, Plaza Bolívar, Bucaral",
            keywords = listOf("barinitas", "bolivar", "bucaral")
        ),
        BarinasLocation(
            id = "loc_parangula",
            name = "Parangula / Quebrada Seca",
            type = "Sector",
            parroquia = "Barinitas",
            block = "Bloque B",
            circuitCode = "Circuito Parangula",
            sectorEntityId = "sec_b_parangula",
            description = "Vía Barinitas, Quebrada Seca y Parangula",
            keywords = listOf("parangula", "quebrada seca")
        ),
        BarinasLocation(
            id = "loc_obispos_centro",
            name = "Obispos Casco Central",
            type = "Municipio",
            parroquia = "Obispos",
            block = "Bloque A",
            circuitCode = "Circuito Obispos 34,5 kV",
            sectorEntityId = "sec_a_obispos",
            description = "Municipio Obispos, Plaza y casco colonial",
            keywords = listOf("obispos", "municipio obispos")
        ),
        BarinasLocation(
            id = "loc_borburata",
            name = "Sector Borburata (Obispos)",
            type = "Sector",
            parroquia = "Obispos",
            block = "Bloque B",
            circuitCode = "Circuito Borburata",
            sectorEntityId = "sec_b_borburata",
            description = "Borburata y zonas rurales agrícolas",
            keywords = listOf("borburata", "obispos borburata")
        ),
        BarinasLocation(
            id = "loc_socopo_centro",
            name = "Socopó Casco Central",
            type = "Municipio",
            parroquia = "Socopó / Sucre",
            block = "Bloque C",
            circuitCode = "Circuito Socopo I",
            sectorEntityId = "sec_c_socopo_1",
            description = "Socopó Av. Bolívar, comercio central",
            keywords = listOf("socopo", "socopo centro", "sucre")
        ),
        BarinasLocation(
            id = "loc_socopo_sur",
            name = "Socopó Zona Sur",
            type = "Municipio",
            parroquia = "Socopó / Sucre",
            block = "Bloque C",
            circuitCode = "Circuito Socopo II",
            sectorEntityId = "sec_c_socopo_2",
            description = "Socopó Sur, Batatuy y adyacencias",
            keywords = listOf("socopo sur", "batatuy")
        ),
        BarinasLocation(
            id = "loc_bum_bum",
            name = "Sector Bum Bum (Troncal 5)",
            type = "Sector",
            parroquia = "Sucre",
            block = "Bloque C",
            circuitCode = "Circuito Bum Bum",
            sectorEntityId = "sec_c_bum_bum",
            description = "Bum Bum Troncal 5 vía Táchira",
            keywords = listOf("bum bum", "bumbum", "troncal 5 bum bum")
        ),
        BarinasLocation(
            id = "loc_cdad_bolivia_1",
            name = "Ciudad Bolivia (Pedraza)",
            type = "Municipio",
            parroquia = "Pedraza",
            block = "Bloque C",
            circuitCode = "Circuito Cdad Bolivia I",
            sectorEntityId = "sec_c_cdad_bolivia_1",
            description = "Ciudad Bolivia Centro, Plaza y Comercio",
            keywords = listOf("ciudad bolivia", "pedraza", "bolivia 1")
        ),
        BarinasLocation(
            id = "loc_cdad_bolivia_2",
            name = "Ciudad Bolivia II (Pedraza)",
            type = "Municipio",
            parroquia = "Pedraza",
            block = "Bloque A",
            circuitCode = "Circuito Cdad Bolivia II",
            sectorEntityId = "sec_a_cdad_bolivia_2",
            description = "Sectores residenciales de Ciudad Bolivia",
            keywords = listOf("ciudad bolivia 2", "pedraza sur")
        ),
        BarinasLocation(
            id = "loc_san_silvestre",
            name = "San Silvestre",
            type = "Municipio",
            parroquia = "San Silvestre",
            block = "Bloque B",
            circuitCode = "Circuito San Silvestre",
            sectorEntityId = "sec_b_san_silvestre",
            description = "Poblado de San Silvestre y campo petrolero",
            keywords = listOf("san silvestre", "silvestre")
        ),
        BarinasLocation(
            id = "loc_sta_ines_lucia",
            name = "Santa Inés - Santa Lucía",
            type = "Sector",
            parroquia = "Santa Inés",
            block = "Bloque B",
            circuitCode = "Circuito Sta Ines Lucia",
            sectorEntityId = "sec_b_sta_ines_lucia",
            description = "Parroquias Santa Inés y Santa Lucía",
            keywords = listOf("santa ines", "santa lucia", "sta ines")
        ),
        BarinasLocation(
            id = "loc_libertad",
            name = "Libertad (Municipio Rojas)",
            type = "Municipio",
            parroquia = "Libertad",
            block = "Bloque B",
            circuitCode = "Circuito Libertad",
            sectorEntityId = "sec_b_libertad",
            description = "Libertad de Barinas, Casco Central",
            keywords = listOf("libertad", "rojas", "libertad de barinas")
        ),
        BarinasLocation(
            id = "loc_santa_rosa",
            name = "Santa Rosa de Barinas",
            type = "Sector",
            parroquia = "Santa Rosa",
            block = "Bloque B",
            circuitCode = "Circuito Sta. Rosa",
            sectorEntityId = "sec_b_sta_rosa",
            description = "Poblado y circuito de Santa Rosa",
            keywords = listOf("santa rosa", "sta rosa")
        ),
        BarinasLocation(
            id = "loc_torunos",
            name = "Torunos",
            type = "Sector",
            parroquia = "Torunos",
            block = "Bloque C",
            circuitCode = "Circuito Torunos",
            sectorEntityId = "sec_c_torunos",
            description = "Parroquia Torunos y adyacencias",
            keywords = listOf("torunos")
        ),
        BarinasLocation(
            id = "loc_cdad_nutrias",
            name = "Ciudad de Nutrias",
            type = "Municipio",
            parroquia = "Sosa",
            block = "Bloque C",
            circuitCode = "Circuito Cdad Nutria 34,5 kV",
            sectorEntityId = "sec_c_cdad_nutrias",
            description = "Municipio Sosa, Ciudad de Nutrias",
            keywords = listOf("ciudad de nutrias", "nutrias", "sosa")
        ),
        BarinasLocation(
            id = "loc_dolores",
            name = "Dolores",
            type = "Sector",
            parroquia = "Dolores",
            block = "Bloque C",
            circuitCode = "Circuito Dolores",
            sectorEntityId = "sec_c_dolores",
            description = "Parroquia Dolores",
            keywords = listOf("dolores")
        ),
        BarinasLocation(
            id = "loc_el_paguey",
            name = "El Paguey",
            type = "Sector",
            parroquia = "M. Palacio Fajardo",
            block = "Bloque C",
            circuitCode = "Circuito El Paguey",
            sectorEntityId = "sec_c_el_paguey",
            description = "Río Paguey y sector rural La Caramuca",
            keywords = listOf("el paguey", "paguey", "la caramuca")
        ),
        BarinasLocation(
            id = "loc_capitanejo",
            name = "Capitanejo",
            type = "Municipio",
            parroquia = "Zamora",
            block = "Bloque B",
            circuitCode = "Circuito Capitanejo 34,5 kV",
            sectorEntityId = "sec_b_capitanejo",
            description = "Municipio Zamora, Capitanejo",
            keywords = listOf("capitanejo", "zamora")
        ),
        BarinasLocation(
            id = "loc_sabaneta_caaez",
            name = "Sabaneta / Caaez",
            type = "Municipio",
            parroquia = "Alberto Arvelo Torrealba",
            block = "Bloque D",
            circuitCode = "Circuito Caaez 34,5 kV",
            sectorEntityId = "sec_b_floresta",
            description = "Sabaneta de Barinas, Central Azucarero CAAEZ",
            keywords = listOf("sabaneta", "caaez", "central azucarero")
        ),
        BarinasLocation(
            id = "loc_boconoito",
            name = "Boconoito límite Barinas",
            type = "Sector",
            parroquia = "Límite Portuguesa/Barinas",
            block = "Bloque D",
            circuitCode = "Circuito Boconoito",
            sectorEntityId = "sec_c_corocito",
            description = "Intersección y circuito Boconoito",
            keywords = listOf("boconoito", "autopista")
        )
    )

    val BARINAS_PARROQUIAS = listOf(
        "El Carmen",
        "Alto Barinas",
        "Barinas (Centro)",
        "Rómulo Betancourt",
        "Corazón de Jesús",
        "Manuel Palacio Fajardo",
        "Juan Antonio Rodríguez Domínguez",
        "Dominga Ortiz de Páez",
        "Torunos",
        "San Silvestre",
        "Santa Inés",
        "Santa Lucía",
        "Alfredo Arvelo Larriva",
        "Quebrada Seca (Barinitas)"
    )

    private val customLocations = mutableListOf<BarinasLocation>()

    fun addCustomLocation(loc: BarinasLocation) {
        customLocations.removeAll { it.id == loc.id || it.name.equals(loc.name, ignoreCase = true) }
        customLocations.add(0, loc)
    }

    fun getAllLocations(): List<BarinasLocation> = customLocations + LOCATIONS

    /**
     * Fast, zero-allocation fuzzy search through Barinas locations.
     */
    fun searchLocations(query: String): List<BarinasLocation> {
        val all = getAllLocations()
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return all.take(8)

        val queryTokens = cleanQuery.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        return all.mapNotNull { loc ->
            val nameLower = loc.name.lowercase()
            val descLower = loc.description.lowercase()
            val parroquiaLower = loc.parroquia.lowercase()
            val circuitLower = loc.circuitCode.lowercase()

            var score = 0
            if (nameLower.startsWith(cleanQuery)) {
                score += 100
            } else if (nameLower.contains(cleanQuery)) {
                score += 50
            }

            var matchesAllTokens = true
            for (token in queryTokens) {
                val matchesToken = nameLower.contains(token) ||
                        descLower.contains(token) ||
                        parroquiaLower.contains(token) ||
                        circuitLower.contains(token) ||
                        loc.keywords.any { it.contains(token) }

                if (!matchesToken) {
                    matchesAllTokens = false
                    break
                }
                score += 10
            }

            if (matchesAllTokens || score > 0) Pair(loc, score) else null
        }.sortedByDescending { it.second }
            .map { it.first }
            .take(12)
    }
}
