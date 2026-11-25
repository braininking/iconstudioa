package com.example.data.model

enum class MinimalCategory(val labelPt: String) {
    REDES_SOCIAIS("Redes sociais"),
    COMUNICACAO("Comunicação"),
    ENTRETENIMENTO("Entretenimento"),
    PRODUTIVIDADE("Produtividade"),
    JOGOS("Jogos"),
    COMPRAS("Compras"),
    BANCOS("Bancos"),
    SISTEMA("Sistema"),
    FOTOGRAFIA("Fotografia"),
    NAVEGACAO("Navegação")
}

enum class RenderMode(val labelPt: String) {
    LINE_ART("Line Art"),
    SILHOUETTE("Silhueta")
}

enum class StrokeStyle(val labelPt: String) {
    ROUND("Arredondado"),
    GEOMETRIC("Geométrico"),
    THIN("Ultralight"),
    DOUBLE("Contorno Duplo")
}

enum class ProcessingStatus(val labelPt: String) {
    PENDING("Pendente"),
    PROCESSED_LIBRARY("Biblioteca"),
    PROCESSED_AUTO_LINE_ART("Line Art Auto"),
    PROCESSED_AUTO_SILHOUETTE("Silhueta Auto"),
    MANUAL_OVERRIDE("Ajuste Manual"),
    ERROR("Erro")
}

enum class PreviewGridSize(val labelPt: String, val dpSize: Int) {
    SMALL("Compacto", 46),
    MEDIUM("Padrão", 56),
    LARGE("Ampliado", 68)
}

enum class DetectionEventType(val labelPt: String) {
    ADDED("Instalado"),
    REPLACED("Atualizado"),
    REMOVED("Removido"),
    BATCH_SYNC("Varredura")
}

data class NovaLauncherStatus(
    val isInstalled: Boolean,
    val isPrimeInstalled: Boolean,
    val versionName: String?,
    val supportsAutoTheming: Boolean,
    val mappedLibraryCount: Int,
    val totalExportedCount: Int
)
