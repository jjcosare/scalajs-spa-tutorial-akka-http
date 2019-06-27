package spatutorial.client.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.SPAMain.{DashboardLoc, Loc, TodoLoc}
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.Icon._
import spatutorial.client.components._
import spatutorial.client.services._

import scalacss.ScalaCssReact._

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(router: RouterCtl[Loc], currentLoc: Loc, proxy: ModelProxy[Option[Int]])

  private case class MenuItem(idx: Int, badge: (Props) => VdomNode, icon: Icon, location: Loc)

  // build the Todo menu item, showing the number of open todos
  private def buildTodoMenu(props: Props): VdomElement = {
    val todoCount = props.proxy().getOrElse(0)
    <.span(
      <.span(" Todo "),
      <.span(bss.badgeOpt(CommonStyle.danger), bss.badgePill, todoCount).when(todoCount > 0)
    )
  }

  private val menuItems = Seq(
    MenuItem(1, _ => " Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(2, buildTodoMenu, Icon.check, TodoLoc)
  )

  private class Backend($: BackendScope[Props, Unit]) {
    def mounted(props: Props) =
      // dispatch a message to refresh the todos
      Callback.when(props.proxy.value.isEmpty)(props.proxy.dispatchCB(RefreshTodos))

    def render(props: Props) = {
      <.ul(bss.navbar)(
        // build a list of menu items
        menuItems.toVdomArray(item =>
          <.li(^.key := item.idx, if(props.currentLoc == item.location) (^.className := "nav-item active") else (^.className := "nav-item"),
          props.router.link(item.location)(item.icon, "", item.badge(props))(^.`class` := "nav-link")
        ))
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("MainMenu")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(ctl: RouterCtl[Loc], currentLoc: Loc, proxy: ModelProxy[Option[Int]]): VdomElement =
    component(Props(ctl, currentLoc, proxy))
}
