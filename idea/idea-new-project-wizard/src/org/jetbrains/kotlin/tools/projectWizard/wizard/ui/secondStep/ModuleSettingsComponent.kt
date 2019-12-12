package org.jetbrains.kotlin.tools.projectWizard.wizard.ui.secondStep

import com.intellij.ui.components.panels.VerticalLayout
import org.jetbrains.kotlin.tools.projectWizard.core.entity.StringValidators
import org.jetbrains.kotlin.tools.projectWizard.core.ValuesReadingContext
import org.jetbrains.kotlin.tools.projectWizard.moduleConfigurators.ModuleConfigurator
import org.jetbrains.kotlin.tools.projectWizard.moduleConfigurators.configuratorSettings
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ModuleType
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.*
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.*
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.components.DropDownComponent
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.components.TextFieldComponent
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.setting.SettingsList
import javax.swing.BorderFactory
import javax.swing.JComponent

class ModuleSettingsComponent(valuesReadingContext: ValuesReadingContext) : DynamicComponent(valuesReadingContext) {
    private val validateModuleName =
        StringValidators.shouldNotBeBlank("Module name") and
                StringValidators.shouldBeValidIdentifier("Module Name")

    private val moduleConfigurator = DropDownComponent<ModuleConfigurator>(
        valuesReadingContext,
        iconProvider = { it.icon },
        labelText = "Consider as"
    ) { value ->
        module?.configurator = value
    }

    private val moduleConfiguratorSettingsList = SettingsList(emptyList(), valuesReadingContext)

    private val nameField = TextFieldComponent(
        valuesReadingContext,
        labelText = "Name",
        onAnyValueUpdate = { value ->
            module?.name = value
            eventManager.fireListeners(null)
        },
        validator = validateModuleName
    ).asSubComponent()


    override val component: JComponent = panel(VerticalLayout(5)) {
        border = BorderFactory.createEmptyBorder(
            UiConstants.GAP_BORDER_SIZE,
            UiConstants.GAP_BORDER_SIZE,
            UiConstants.GAP_BORDER_SIZE,
            UiConstants.GAP_BORDER_SIZE
        )
        add(nameField.component)
        add(moduleConfigurator.component)
        add(moduleConfiguratorSettingsList.component)
    }

    var module: Module? = null
        set(value) {
            field = value
            if (value != null) {
                updateModule(value)
            }
        }

    private fun updateModule(module: Module) {
        nameField.value = module.name
        val newValues = ModuleConfigurator.BY_MODULE_KIND.getValue(module.configurator.moduleKind)
            .filter { configurator ->
                if (configurator.moduleKind == ModuleKind.target)
                    configurator.moduleType == module.configurator.moduleType
                else false
            }

        nameField.component.isVisible = module.kind != ModuleKind.target
                || module.configurator.moduleType != ModuleType.common

        moduleConfiguratorSettingsList.setSettings(module.configuratorSettings)

        moduleConfigurator.apply {
            updateValues(newValues)
            component.isVisible = newValues.size > 1
            value = module.configurator
            component.updateUI()
        }
    }
}

