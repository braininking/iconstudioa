package com.example.engine

import com.example.data.model.MinimalCategory

data class VectorSymbolSpec(
    val id: String,
    val displayName: String,
    val drawableResName: String,
    val category: MinimalCategory,
    val strokePaths: List<String>,
    val fillPaths: List<String> = emptyList(),
    val exactPackages: Set<String> = emptySet(),
    val keywords: Set<String> = emptySet()
)

object MinimalIconLibrary {

    val allSymbols: List<VectorSymbolSpec> = listOf(
        // Comunicação
        VectorSymbolSpec(
            id = "whatsapp",
            displayName = "WhatsApp",
            drawableResName = "ic_bm_whatsapp",
            category = MinimalCategory.COMUNICACAO,
            strokePaths = listOf(
                "M24,13.5 C18.2,13.5 13.5,18.2 13.5,24 C13.5,26.1 14.1,28.1 15.2,29.7 L13.8,34.2 L18.5,32.9 C20.1,33.9 22,34.5 24,34.5 C29.8,34.5 34.5,29.8 34.5,24 C34.5,18.2 29.8,13.5 24,13.5 Z",
                "M19.8,19.4 C19.4,19.8 19.2,20.8 19.8,22.2 C20.6,24 22.1,25.5 23.9,26.3 C25.3,26.9 26.3,26.7 26.7,26.3 L27.6,25.4 C28,25 28.5,25 28.9,25.4 L30.1,26.6 C30.5,27 30.5,27.5 30.1,27.9 L29.2,28.8 C28.1,29.9 25.8,29.7 23.2,27.9 C20.6,26.1 18.8,23.8 18.4,22 C18.1,20.6 18.5,19.3 19.2,18.7 L20.1,17.8 C20.5,17.4 21,17.4 21.4,17.8 L22.6,19 C23,19.4 23,19.9 22.6,20.3 L21.7,21.2"
            ),
            exactPackages = setOf("com.whatsapp", "com.whatsapp.w4b"),
            keywords = setOf("whatsapp")
        ),
        VectorSymbolSpec(
            id = "telegram",
            displayName = "Telegram",
            drawableResName = "ic_bm_telegram",
            category = MinimalCategory.COMUNICACAO,
            strokePaths = listOf(
                "M34,15.5 L13.5,23.8 L20.5,26.2 L29.5,19.5 L23,27.2 L29.8,32.5 Z",
                "M20.5,26.2 L21.4,32.2 L24.6,28.5"
            ),
            exactPackages = setOf("org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram"),
            keywords = setOf("telegram")
        ),
        VectorSymbolSpec(
            id = "discord",
            displayName = "Discord",
            drawableResName = "ic_bm_discord",
            category = MinimalCategory.COMUNICACAO,
            strokePaths = listOf(
                "M19.5,17.2 L21.2,18.2 C23,17.8 25,17.8 26.8,18.2 L28.5,17.2 C30.5,18 32.3,19.6 33.4,21.6 C34.5,24.8 34.4,28 33,30.6 C31.2,31.8 29.2,32.5 27.3,32.7 L26.2,31.1 C24.7,31.4 23.3,31.4 21.8,31.1 L20.7,32.7 C18.8,32.5 16.8,31.8 15,30.6 C13.6,28 13.5,24.8 14.6,21.6 C15.7,19.6 17.5,18 19.5,17.2 Z"
            ),
            fillPaths = listOf(
                "M20.3,23.5 A1.3,1.3 0 1 0 20.3,26.1 A1.3,1.3 0 1 0 20.3,23.5 Z",
                "M27.7,23.5 A1.3,1.3 0 1 0 27.7,26.1 A1.3,1.3 0 1 0 27.7,23.5 Z"
            ),
            exactPackages = setOf("com.discord"),
            keywords = setOf("discord")
        ),
        VectorSymbolSpec(
            id = "gmail",
            displayName = "Gmail",
            drawableResName = "ic_bm_gmail",
            category = MinimalCategory.COMUNICACAO,
            strokePaths = listOf(
                "M15,16.5 L33,16.5 A2,2 0 0 1 35,18.5 L35,29.5 A2,2 0 0 1 33,31.5 L15,31.5 A2,2 0 0 1 13,29.5 L13,18.5 A2,2 0 0 1 15,16.5 Z",
                "M14,18 L24,25.5 L34,18"
            ),
            exactPackages = setOf("com.google.android.gm", "com.microsoft.office.outlook"),
            keywords = setOf("gmail", "mail", "email", "outlook")
        ),
        VectorSymbolSpec(
            id = "phone",
            displayName = "Telefone",
            drawableResName = "ic_bm_phone",
            category = MinimalCategory.COMUNICACAO,
            strokePaths = listOf(
                "M16.2,15 C15.2,16 14.8,18.2 15.8,21.2 C17.1,24.8 20.2,28.9 23.8,31.2 C26.8,33.2 29,32.8 30,31.8 L32.2,29.6 C33,28.8 33,27.6 32.2,26.8 L29.4,24 C28.6,23.2 27.4,23.2 26.6,24 L25.1,25.5 C23.6,24.6 22.4,23.4 21.5,21.9 L23,20.4 C23.8,19.6 23.8,18.4 23,17.6 L20.2,14.8 C19.4,14 18.2,14 17.4,14.8 Z"
            ),
            exactPackages = setOf(
                "com.google.android.dialer",
                "com.android.dialer",
                "com.samsung.android.dialer",
                "com.android.contacts"
            ),
            keywords = setOf("dialer", "phone", "telefone", "chamadas")
        ),

        // Redes Sociais
        VectorSymbolSpec(
            id = "instagram",
            displayName = "Instagram",
            drawableResName = "ic_bm_instagram",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M18,14 L30,14 A4,4 0 0 1 34,18 L34,30 A4,4 0 0 1 30,34 L18,34 A4,4 0 0 1 14,30 L14,18 A4,4 0 0 1 18,14 Z",
                "M24,19.5 A4.5,4.5 0 1 0 24,28.5 A4.5,4.5 0 1 0 24,19.5 Z"
            ),
            fillPaths = listOf("M29.8,17.3 A0.9,0.9 0 1 0 29.8,19.1 A0.9,0.9 0 1 0 29.8,17.3 Z"),
            exactPackages = setOf("com.instagram.android", "com.instagram.lite"),
            keywords = setOf("instagram")
        ),
        VectorSymbolSpec(
            id = "facebook",
            displayName = "Facebook",
            drawableResName = "ic_bm_facebook",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M28.5,14 L25.5,14 C22.5,14 21,15.8 21,18.8 L21,22 L18,22 L18,25.5 L21,25.5 L21,34.5 L24.8,34.5 L24.8,25.5 L28,25.5 L28.6,22 L24.8,22 L24.8,19 C24.8,17.9 25.4,17.4 26.5,17.4 L28.5,17.4 Z"
            ),
            exactPackages = setOf("com.facebook.katana", "com.facebook.lite"),
            keywords = setOf("facebook")
        ),
        VectorSymbolSpec(
            id = "tiktok",
            displayName = "TikTok",
            drawableResName = "ic_bm_tiktok",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M25.5,13.5 L25.5,28.5 C25.5,31.5 23.1,34 20,34 C17,34 14.8,31.7 14.8,28.8 C14.8,25.9 17.1,23.6 20,23.6 C20.7,23.6 21.4,23.7 22,24",
                "M25.5,13.5 C26.6,16.8 29.5,19 33,19.2"
            ),
            exactPackages = setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"),
            keywords = setOf("tiktok", "musically")
        ),
        VectorSymbolSpec(
            id = "pinterest",
            displayName = "Pinterest",
            drawableResName = "ic_bm_pinterest",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M19.5,34.5 L22.2,23",
                "M20.5,26.5 C21.8,27.8 23.5,28.2 25.2,27.8 C28.5,27.1 30.8,24.1 30.5,20.7 C30.2,17.2 27.3,14.8 23.8,15 C19.8,15.2 17,18.2 17.5,21.8 C17.7,23.1 18.4,24.1 19.3,24.6"
            ),
            exactPackages = setOf("com.pinterest"),
            keywords = setOf("pinterest")
        ),
        VectorSymbolSpec(
            id = "snapchat",
            displayName = "Snapchat",
            drawableResName = "ic_bm_snapchat",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M24,14 C20.4,14 18.2,16.5 18.2,20.2 L18.2,23 C16.8,23.4 16,24.2 16,25 C16,25.8 17.4,26.4 18.2,26.8 C17.5,29.1 15.6,30.5 13.8,31.2 C15.2,32.2 17.4,32.5 19.2,32.5 C20.6,33.6 22.2,34.4 24,34.4 C25.8,34.4 27.4,33.6 28.8,32.5 C30.6,32.5 32.8,32.2 34.2,31.2 C32.4,30.5 30.5,29.1 29.8,26.8 C30.6,26.4 32,25.8 32,25 C32,24.2 31.2,23.4 29.8,23 L29.8,20.2 C29.8,16.5 27.6,14 24,14 Z"
            ),
            exactPackages = setOf("com.snapchat.android"),
            keywords = setOf("snapchat")
        ),
        VectorSymbolSpec(
            id = "x_social",
            displayName = "X / Rede",
            drawableResName = "ic_bm_x",
            category = MinimalCategory.REDES_SOCIAIS,
            strokePaths = listOf(
                "M15,15 L21.5,15 L33,33 L26.5,33 Z",
                "M33,15 L15,33"
            ),
            exactPackages = setOf("com.twitter.android"),
            keywords = setOf("twitter", "threads", "reddit")
        ),

        // Entretenimento
        VectorSymbolSpec(
            id = "youtube",
            displayName = "YouTube",
            drawableResName = "ic_bm_youtube",
            category = MinimalCategory.ENTRETENIMENTO,
            strokePaths = listOf(
                "M16.5,16.5 L31.5,16.5 A3.5,3.5 0 0 1 35,20 L35,28 A3.5,3.5 0 0 1 31.5,31.5 L16.5,31.5 A3.5,3.5 0 0 1 13,28 L13,20 A3.5,3.5 0 0 1 16.5,16.5 Z",
                "M21.8,20.5 L28.2,24 L21.8,27.5 Z"
            ),
            exactPackages = setOf("com.google.android.youtube", "com.google.android.apps.youtube.music"),
            keywords = setOf("youtube", "twitch", "video")
        ),
        VectorSymbolSpec(
            id = "spotify",
            displayName = "Spotify",
            drawableResName = "ic_bm_spotify",
            category = MinimalCategory.ENTRETENIMENTO,
            strokePaths = listOf(
                "M24,13.5 A10.5,10.5 0 1 0 24,34.5 A10.5,10.5 0 1 0 24,13.5 Z",
                "M18.2,20.5 C21.8,19.3 26.5,19.6 30,21.5",
                "M19.2,24 C22.2,23 25.8,23.3 28.8,24.8",
                "M20.2,27.4 C22.5,26.6 25.2,26.9 27.4,28.1"
            ),
            exactPackages = setOf("com.spotify.music", "com.spotify.lite"),
            keywords = setOf("spotify", "music", "deezer", "tidal")
        ),
        VectorSymbolSpec(
            id = "netflix",
            displayName = "Netflix",
            drawableResName = "ic_bm_netflix",
            category = MinimalCategory.ENTRETENIMENTO,
            strokePaths = listOf(
                "M16.5,34 L16.5,14 L19.8,14 L28.2,34 L31.5,34 L31.5,14",
                "M19.8,14 L19.8,34",
                "M28.2,14 L28.2,34"
            ),
            exactPackages = setOf("com.netflix.mediaclient"),
            keywords = setOf("netflix", "primevideo", "disney", "max")
        ),

        // Navegação
        VectorSymbolSpec(
            id = "chrome",
            displayName = "Chrome",
            drawableResName = "ic_bm_chrome",
            category = MinimalCategory.NAVEGACAO,
            strokePaths = listOf(
                "M24,13.5 A10.5,10.5 0 1 0 24,34.5 A10.5,10.5 0 1 0 24,13.5 Z",
                "M24,19.8 A4.2,4.2 0 1 0 24,28.2 A4.2,4.2 0 1 0 24,19.8 Z",
                "M24,19.8 L33.5,19.8",
                "M20.3,26.1 L15.6,18",
                "M27.6,26.1 L22.9,34.2"
            ),
            exactPackages = setOf("com.android.chrome", "com.chrome.beta", "org.mozilla.firefox", "com.brave.browser"),
            keywords = setOf("chrome", "browser", "firefox", "brave", "edge")
        ),
        VectorSymbolSpec(
            id = "maps",
            displayName = "Google Maps",
            drawableResName = "ic_bm_maps",
            category = MinimalCategory.NAVEGACAO,
            strokePaths = listOf(
                "M24,35 C24,35 31.5,27.2 31.5,21.5 C31.5,17.4 28.1,14 24,14 C19.9,14 16.5,17.4 16.5,21.5 C16.5,27.2 24,35 24,35 Z",
                "M24,18.7 A2.8,2.8 0 1 0 24,24.3 A2.8,2.8 0 1 0 24,18.7 Z"
            ),
            exactPackages = setOf("com.google.android.apps.maps"),
            keywords = setOf("maps", "mapa")
        ),
        VectorSymbolSpec(
            id = "navigation",
            displayName = "Navegação / GPS",
            drawableResName = "ic_bm_navigation",
            category = MinimalCategory.NAVEGACAO,
            strokePaths = listOf(
                "M24,13.5 A10.5,10.5 0 1 0 24,34.5 A10.5,10.5 0 1 0 24,13.5 Z",
                "M28,20 L25.5,25.5 L20,28 L22.5,22.5 Z"
            ),
            exactPackages = setOf("com.waze", "com.ubercab"),
            keywords = setOf("waze", "uber", "99", "gps", "compass")
        ),

        // Sistema & Produtividade
        VectorSymbolSpec(
            id = "playstore",
            displayName = "Play Store",
            drawableResName = "ic_bm_playstore",
            category = MinimalCategory.SISTEMA,
            strokePaths = listOf(
                "M16.5,14 L33,24 L16.5,34 Z",
                "M17.2,14.8 L27.5,27.2",
                "M17.2,33.2 L27.5,20.8"
            ),
            exactPackages = setOf("com.android.vending"),
            keywords = setOf("vending", "playstore", "store")
        ),
        VectorSymbolSpec(
            id = "settings",
            displayName = "Configurações",
            drawableResName = "ic_bm_settings",
            category = MinimalCategory.SISTEMA,
            strokePaths = listOf(
                "M24,20.5 A3.5,3.5 0 1 0 24,27.5 A3.5,3.5 0 1 0 24,20.5 Z",
                "M22.2,13.5 L25.8,13.5 L26.3,16.1 C27.1,16.4 27.9,16.8 28.6,17.3 L31,16.4 L32.8,19.5 L30.8,21.2 C30.9,22.1 30.9,22.9 30.8,23.8 L30.8,26.8 L32.8,28.5 L31,31.6 L28.6,30.7 C27.9,31.2 27.1,31.6 26.3,31.9 L25.8,34.5 L22.2,34.5 L21.7,31.9 C20.9,31.6 20.1,31.2 19.4,30.7 L17,31.6 L15.2,28.5 L17.2,26.8 C17.1,25.9 17.1,25.1 17.2,24.2 L17.2,21.2 L15.2,19.5 L17,16.4 L19.4,17.3 C20.1,16.8 20.9,16.4 21.7,16.1 Z"
            ),
            exactPackages = setOf("com.android.settings"),
            keywords = setOf("settings", "config")
        ),
        VectorSymbolSpec(
            id = "files",
            displayName = "Arquivos",
            drawableResName = "ic_bm_files",
            category = MinimalCategory.SISTEMA,
            strokePaths = listOf(
                "M14,17.5 A2,2 0 0 1 16,15.5 L21.5,15.5 L24,18.5 L32,18.5 A2,2 0 0 1 34,20.5 L34,31 A2,2 0 0 1 32,33 L16,33 A2,2 0 0 1 14,31 Z"
            ),
            exactPackages = setOf("com.google.android.apps.nbu.files", "com.android.documentsui", "com.sec.android.app.myfiles"),
            keywords = setOf("files", "arquivos", "documentsui", "explorer")
        ),
        VectorSymbolSpec(
            id = "productivity",
            displayName = "Produtividade",
            drawableResName = "ic_bm_productivity",
            category = MinimalCategory.PRODUTIVIDADE,
            strokePaths = listOf(
                "M17,14 L31,14 A2,2 0 0 1 33,16 L33,32 A2,2 0 0 1 31,34 L17,34 A2,2 0 0 1 15,32 L15,16 A2,2 0 0 1 17,14 Z",
                "M19,20.5 L29,20.5",
                "M19,24.5 L29,24.5",
                "M19,28.5 L25,28.5"
            ),
            exactPackages = setOf("com.google.android.keep", "com.google.android.calendar", "com.google.android.apps.docs", "notion.id"),
            keywords = setOf("notes", "keep", "calendar", "agenda", "docs", "notion", "drive")
        ),

        // Fotografia
        VectorSymbolSpec(
            id = "camera",
            displayName = "Câmera",
            drawableResName = "ic_bm_camera",
            category = MinimalCategory.FOTOGRAFIA,
            strokePaths = listOf(
                "M13.5,19.5 A2.5,2.5 0 0 1 16,17 L18.5,17 L20.5,14.5 L27.5,14.5 L29.5,17 L32,17 A2.5,2.5 0 0 1 34.5,19.5 L34.5,31 A2.5,2.5 0 0 1 32,33.5 L16,33.5 A2.5,2.5 0 0 1 13.5,31 Z",
                "M24,20.8 A4.2,4.2 0 1 0 24,29.2 A4.2,4.2 0 1 0 24,20.8 Z"
            ),
            exactPackages = setOf("com.android.camera", "com.android.camera2", "com.google.android.GoogleCamera", "com.sec.android.app.camera"),
            keywords = setOf("camera", "câmera")
        ),
        VectorSymbolSpec(
            id = "gallery",
            displayName = "Galeria",
            drawableResName = "ic_bm_gallery",
            category = MinimalCategory.FOTOGRAFIA,
            strokePaths = listOf(
                "M16.5,14.5 L31.5,14.5 A2.5,2.5 0 0 1 34,17 L34,31 A2.5,2.5 0 0 1 31.5,33.5 L16.5,33.5 A2.5,2.5 0 0 1 14,31 L14,17 A2.5,2.5 0 0 1 16.5,14.5 Z",
                "M14.5,29.5 L20.5,23.5 L25,28 L28,25 L33.5,30.5"
            ),
            fillPaths = listOf("M19,18.2 A1.3,1.3 0 1 0 19,20.8 A1.3,1.3 0 1 0 19,18.2 Z"),
            exactPackages = setOf("com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d"),
            keywords = setOf("photos", "fotos", "gallery", "galeria")
        ),

        // Bancos, Compras, Jogos
        VectorSymbolSpec(
            id = "bank",
            displayName = "Bancos",
            drawableResName = "ic_bm_bank",
            category = MinimalCategory.BANCOS,
            strokePaths = listOf(
                "M14,20 L24,14 L34,20 Z",
                "M17,20 L17,31",
                "M21.5,20 L21.5,31",
                "M26.5,20 L26.5,31",
                "M31,20 L31,31",
                "M14,34 L34,34"
            ),
            exactPackages = setOf("com.nu.production", "br.com.intermedium", "com.itau", "com.bradesco"),
            keywords = setOf("bank", "banco", "nubank", "inter", "itau", "picpay", "wallet", "pay")
        ),
        VectorSymbolSpec(
            id = "shopping",
            displayName = "Compras",
            drawableResName = "ic_bm_shopping",
            category = MinimalCategory.COMPRAS,
            strokePaths = listOf(
                "M16,19 L32,19 L33.5,33.5 L14.5,33.5 Z",
                "M20,19 L20,17 A4,4 0 0 1 28,17 L28,19"
            ),
            exactPackages = setOf("com.amazon.mShop.android.shopping", "com.mercadolibre", "com.shopee.br"),
            keywords = setOf("shop", "amazon", "mercado", "shopee", "aliexpress", "loja")
        ),
        VectorSymbolSpec(
            id = "games",
            displayName = "Jogos",
            drawableResName = "ic_bm_games",
            category = MinimalCategory.JOGOS,
            strokePaths = listOf(
                "M17.5,18.5 L30.5,18.5 C32.8,18.5 34.5,20.8 34.5,24.5 L34,29.5 C33.8,31.2 32.5,32 31,32 C29.5,32 28.5,30.5 27.5,29 L20.5,29 C19.5,30.5 18.5,32 17,32 C15.5,32 14.2,31.2 14,29.5 L13.5,24.5 C13.5,20.8 15.2,18.5 17.5,18.5 Z",
                "M18,23.5 L21.5,23.5",
                "M19.75,21.75 L19.75,25.25"
            ),
            fillPaths = listOf(
                "M28.5,22 A1,1 0 1 0 28.5,24 A1,1 0 1 0 28.5,22 Z",
                "M26.5,24 A1,1 0 1 0 26.5,26 A1,1 0 1 0 26.5,24 Z"
            ),
            exactPackages = setOf("com.google.android.play.games"),
            keywords = setOf("game", "jogo", "playgames", "supercell", "roblox", "minecraft")
        )
    )

    private val byId = allSymbols.associateBy { it.id }

    fun getById(id: String?): VectorSymbolSpec? = id?.let { byId[it] }

    fun findSymbolForApp(packageName: String, appName: String): VectorSymbolSpec? {
        val pkgLower = packageName.lowercase()
        val nameLower = appName.lowercase()

        allSymbols.firstOrNull { pkgLower in it.exactPackages }?.let { return it }

        return allSymbols.firstOrNull { spec ->
            spec.keywords.any { kw -> pkgLower.contains(kw) || nameLower.contains(kw) }
        }
    }

    fun inferCategory(packageName: String, appName: String): MinimalCategory {
        findSymbolForApp(packageName, appName)?.let { return it.category }
        val text = "${packageName.lowercase()} ${appName.lowercase()}"
        return when {
            text.contains("social") || text.contains("insta") || text.contains("face") || text.contains("threads") -> MinimalCategory.REDES_SOCIAIS
            text.contains("chat") || text.contains("msg") || text.contains("sms") || text.contains("mail") || text.contains("tele") -> MinimalCategory.COMUNICACAO
            text.contains("video") || text.contains("music") || text.contains("stream") || text.contains("tv") || text.contains("radio") -> MinimalCategory.ENTRETENIMENTO
            text.contains("note") || text.contains("calc") || text.contains("doc") || text.contains("office") || text.contains("clock") -> MinimalCategory.PRODUTIVIDADE
            text.contains("game") || text.contains("play") -> MinimalCategory.JOGOS
            text.contains("shop") || text.contains("store") || text.contains("buy") -> MinimalCategory.COMPRAS
            text.contains("bank") || text.contains("pay") || text.contains("pix") || text.contains("fin") -> MinimalCategory.BANCOS
            text.contains("cam") || text.contains("photo") || text.contains("edit") -> MinimalCategory.FOTOGRAFIA
            text.contains("map") || text.contains("nav") || text.contains("gps") -> MinimalCategory.NAVEGACAO
            else -> MinimalCategory.SISTEMA
        }
    }
}
