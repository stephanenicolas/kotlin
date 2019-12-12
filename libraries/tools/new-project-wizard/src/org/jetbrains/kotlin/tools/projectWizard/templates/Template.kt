package org.jetbrains.kotlin.tools.projectWizard.templates

import org.jetbrains.kotlin.tools.projectWizard.Identificator
import org.jetbrains.kotlin.tools.projectWizard.SettingsOwner
import org.jetbrains.kotlin.tools.projectWizard.core.*
import org.jetbrains.kotlin.tools.projectWizard.core.entity.*
import org.jetbrains.kotlin.tools.projectWizard.core.service.FileSystemService
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.BuildSystemIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.DependencyIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.SourcesetIR
import org.jetbrains.kotlin.tools.projectWizard.moduleConfigurators.ModuleConfigurator
import org.jetbrains.kotlin.tools.projectWizard.phases.GenerationPhase
import org.jetbrains.kotlin.tools.projectWizard.plugins.StructurePlugin
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ModuleType
import org.jetbrains.kotlin.tools.projectWizard.settings.DisplayableSettingItem
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.Sourceset
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.SourcesetType
import org.jetbrains.kotlin.tools.projectWizard.settings.version.Version
import java.nio.file.Path
import kotlin.properties.ReadOnlyProperty

interface TemplateEnvironment {
    val <V : Any, T : SettingType<V>> TemplateSetting<V, T>.reference: TemplateSettingReference<V, T>
}

class IdBasedTemplateEnvironment(
    private val template: Template,
    private val sourcesetId: Identificator
) : TemplateEnvironment {
    override val <V : Any, T : SettingType<V>> TemplateSetting<V, T>.reference
        get() = IdBasedTemplateSettingReference(template, sourcesetId, this)
}

class SourcesetBasedTemplateEnvironment<Q : Template>(
    val template: Q,
    private val sourceset: Sourceset
) : TemplateEnvironment {
    override val <V : Any, T : SettingType<V>> TemplateSetting<V, T>.reference
        get() = SourcesetBasedTemplateSettingReference(template, sourceset, this)
}

fun <T> withSettingsOf(
    sourceset: Sourceset,
    template: Template = sourceset.template!!,
    function: TemplateEnvironment.() -> T
): T = function(SourcesetBasedTemplateEnvironment(template, sourceset))

fun <T> withSettingsOf(
    identificator: Identificator,
    template: Template,
    function: TemplateEnvironment.() -> T
): T = function(IdBasedTemplateEnvironment(template, identificator))


abstract class Template : SettingsOwner {
    override fun <V : Any, T : SettingType<V>> settingDelegate(
        create: (path: String) -> SettingBuilder<V, T>
    ): ReadOnlyProperty<Any, TemplateSetting<V, T>> = cached { name ->
        TemplateSetting(create(name).buildInternal())
    }

    abstract val title: String
    abstract val htmlDescription: String
    abstract val moduleTypes: Set<ModuleType>
    abstract val sourcesetTypes: Set<SourcesetType>

    open val settings: List<TemplateSetting<*, *>> = emptyList()

    open fun TaskRunningContext.getRequiredLibraries(sourceset: SourcesetIR): List<DependencyIR> = emptyList()

    //TODO: use setting reading context
    open fun TaskRunningContext.getIrsToAddToBuildFile(
        sourceset: SourcesetIR
    ): List<BuildSystemIR> = emptyList()

    open fun TaskRunningContext.getFileTemplates(sourceset: SourcesetIR): List<FileTemplateDescriptor> = emptyList()

    fun TaskRunningContext.applyToSourceset(
        templateEngine: TemplateEngine,
        sourceset: SourcesetIR
    ): TaskResult<TemplateApplicationResult> {
        val fileSystemService = service<FileSystemService>()

        val allSettings = createDefaultSettings() + settingsAsMap(sourceset.original)

        val librariesToAdd = getRequiredLibraries(sourceset)
        val irsToAddToBuildFile = getIrsToAddToBuildFile(sourceset)

        val result = TemplateApplicationResult(librariesToAdd, irsToAddToBuildFile)

        return getFileTemplates(sourceset).map { fileTemplate ->
            val fileText = templateEngine.renderTemplate(fileTemplate, allSettings)
            val path = sourceset.path / fileTemplate.relativePath
            fileSystemService.createFile(path, fileText)
        }.sequenceIgnore() andThen result.asSuccess()
    }

    private fun TaskRunningContext.settingsAsMap(sourceset: Sourceset): Map<String, Any> =
        withSettingsOf(sourceset) {
            settings.associate { setting ->
                setting.path to setting.reference.settingValue
            }
        }


    private fun TaskRunningContext.createDefaultSettings() = mapOf(
        "projectName" to StructurePlugin::name.settingValue.capitalize()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <V : DisplayableSettingItem> dropDownSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        parser: Parser<V>,
        init: DropDownSettingType.Builder<V>.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<V, DropDownSettingType<V>>> =
        super.dropDownSetting(
            title,
            neededAtPhase,
            parser,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<V, DropDownSettingType<V>>>

    @Suppress("UNCHECKED_CAST")
    override fun stringSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        init: StringSettingType.Builder.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<String, StringSettingType>> =
        super.stringSetting(
            title,
            neededAtPhase,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<String, StringSettingType>>

    @Suppress("UNCHECKED_CAST")
    override fun booleanSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        init: BooleanSettingType.Builder.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<Boolean, BooleanSettingType>> =
        super.booleanSetting(
            title,
            neededAtPhase,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<Boolean, BooleanSettingType>>

    @Suppress("UNCHECKED_CAST")
    override fun <V : Any> valueSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        parser: Parser<V>,
        init: ValueSettingType.Builder<V>.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<V, ValueSettingType<V>>> =
        super.valueSetting(
            title,
            neededAtPhase,
            parser,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<V, ValueSettingType<V>>>

    @Suppress("UNCHECKED_CAST")
    override fun versionSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        init: VersionSettingType.Builder.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<Version, VersionSettingType>> =
        super.versionSetting(
            title,
            neededAtPhase,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<Version, VersionSettingType>>

    @Suppress("UNCHECKED_CAST")
    override fun <V : Any> listSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        parser: Parser<V>,
        init: ListSettingType.Builder<V>.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<List<V>, ListSettingType<V>>> =
        super.listSetting(
            title,
            neededAtPhase,
            parser,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<List<V>, ListSettingType<V>>>


    @Suppress("UNCHECKED_CAST")
    override fun pathSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        init: PathSettingType.Builder.() -> Unit
    ): ReadOnlyProperty<Any, TemplateSetting<Path, PathSettingType>> =
        super.pathSetting(
            title,
            neededAtPhase,
            init
        ) as ReadOnlyProperty<Any, TemplateSetting<Path, PathSettingType>>

    inline fun <reified E> enumSetting(
        title: String,
        neededAtPhase: GenerationPhase,
        crossinline init: DropDownSettingType.Builder<E>.() -> Unit = {}
    ) where E : Enum<E>, E : DisplayableSettingItem = dropDownSetting<E>(title, neededAtPhase, enumParser()) {
        values = enumValues<E>().asList()
        init()
    }

    companion object {
        fun parser(sourcesetIdentificator: Identificator): Parser<Template> = mapParser { map, path ->
            val (id) = map.parseValue<String>(path, "id")
            val (template) = state.idToTemplate[id].toResult { TemplateNotFoundError(id) }
            val (settingsWithValues) = template.settings.mapComputeM { setting ->
                val (settingValue) = map[setting.path].toResult { ParseError("No value was found for a key `$path.${setting.path}`") }
                val reference = withSettingsOf(sourcesetIdentificator, template) { setting.reference }
                setting.type.parse(this, settingValue, setting.path).map { reference to it }
            }.sequence()
            updateState { it.withSettings(settingsWithValues) }
            template
        } or valueParserM { value, path ->
            val (id) = value.parseAs<String>(path)
            state.idToTemplate[id].toResult { TemplateNotFoundError(id) }
        }
    }
}

fun Template.settings(sourceset: Sourceset) = withSettingsOf(sourceset) {
    settings.map { it.reference }
}

fun TaskRunningContext.applyTemplateToSourceset(
    template: Template?,
    sourceset: SourcesetIR,
    templateEngine: TemplateEngine
): TaskResult<TemplateApplicationResult> = when (template) {
    null -> TemplateApplicationResult(emptyList(), emptyList()).asSuccess()
    else -> with(template) {
        applyToSourceset(templateEngine, sourceset)
    }
}


data class TemplateApplicationResult(
    val librariesToAdd: List<DependencyIR>,
    val irsToAddToBuildFile: List<BuildSystemIR>
) {
    companion object {
        val EMPTY = TemplateApplicationResult(emptyList(), emptyList())
    }
}

fun List<TemplateApplicationResult>.fold() =
    fold(TemplateApplicationResult.EMPTY, TemplateApplicationResult::plus)

operator fun TemplateApplicationResult.plus(other: TemplateApplicationResult) =
    TemplateApplicationResult(
        librariesToAdd + other.librariesToAdd,
        irsToAddToBuildFile + other.irsToAddToBuildFile
    )