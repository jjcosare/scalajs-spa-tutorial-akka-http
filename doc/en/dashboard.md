# Dashboard

[Dashboard module](https://github.com/ochrons/scalajs-spa-tutorial/tree/master/client/src/main/scala/spatutorial/client/modules/Dashboard.scala) is really simple 
in terms of React components as it contains no internal state nor backend functionality. It's basically just a placeholder for two other components 
`Motd` and `Chart`. The only method is the `render` method which is responsible for rendering the component when it's mounted by React. It also provides 
fake data for the Chart component, to keep things simple.

```scala
// create the React component for Dashboard
private val component = ScalaComponent.builder[Props]("Dashboard")
  // create and store the connect proxy in state for later use
  .initialStateFromProps(props => State(props.proxy.connect(m => m)))
  .renderPS { (_, props, state) =>
    <.div(
      // header, MessageOfTheDay and chart components
      <.h2("Dashboard"),
      state.motdWrapper(Motd(_)),
      Chart(cp),
      // create a link to the To Do view
      <.div(props.router.link(TodoLoc)("Check your todos!"))
    )
  }
  .build
```

## Message of the day

[Motd component](https://github.com/ochrons/scalajs-spa-tutorial/tree/master/client/src/main/scala/spatutorial/client/components/Motd.scala) is a simple React
component that shows a *Message of the day* from the server in a panel. The `Motd` is given the message in properties (wrapped in a `Pot` and a
`ModelProxy`).

```scala
val Motd = ScalaComponent.builder[ModelProxy[Pot[String]]]("Motd")
  .render_P { proxy =>
    Panel(Panel.Props("Message of the day"),
      // render messages depending on the state of the Pot
      proxy().renderPending(_ > 500, _ => <.p("Loading...")),
      proxy().renderFailed(ex => <.p("Failed to load")),
      proxy().render(m => <.p(m)),
      Button(Button.Props(proxy.dispatchCB(UpdateMotd()), CommonStyle.danger), Icon.refresh, " Update")
    )
  }
  .componentDidMount(scope =>
    // update only if Motd is empty
    Callback.ifTrue(scope.props.value.isEmpty, scope.props.dispatchCB(UpdateMotd()))
  )
  .build
```
A React component is defined through a series of function calls. Each of these calls, modifies the type of the component, meaning you cannot call
`componentDidMount` unless you have gone through `render` first.

Of course to actually get the message, we need to request it from the server. To do this automatically when the component is mounted, we hook a call to
`dispatch` in the `componentDidMount` method, but only if there is no value already for the message.

The use of `ModelProxy` and `Pot` will be covered in detail [later](todo-module-and-data-flow.md).

## Links to other routes

Sometimes you need to create a link that takes the user to another module behind a route. To create these links in a type-safe manner,
the tutorial passes an instance of `RouterCtl` to components.

```scala
ctl.link(TodoLoc)("Check your todos!")
```

