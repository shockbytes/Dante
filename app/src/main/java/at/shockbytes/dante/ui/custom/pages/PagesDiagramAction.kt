package at.shockbytes.dante.ui.custom.pages

sealed class PagesDiagramAction {
    object Gone : PagesDiagramAction()
    object Overflow : PagesDiagramAction()
    data class Action(val title: String) : PagesDiagramAction()
}