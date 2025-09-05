package de.akvsoft.cashflow

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.theme.Theme
import org.springframework.context.annotation.Configuration

@Configuration
@Theme("cashflow")
@Push
class FrontendConfiguration : AppShellConfigurator