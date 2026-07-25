package pl.mjedynak.idea.plugins.pit.cli.factory

import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.mjedynak.idea.plugins.pit.cli.PitCommandLineArgumentsContainer

class DefaultArgumentsContainerFactoryTest {
    private val defaultArgumentsContainerPopulator: DefaultArgumentsContainerPopulator = mock()
    private val project: Project = mock()
    private val defaultArgumentsContainerFactory =
        DefaultArgumentsContainerFactory(defaultArgumentsContainerPopulator)

    @Test
    fun `should delegate creation to populator`() {
        val container = defaultArgumentsContainerFactory.createDefaultPitCommandLineArgumentsContainer(project)

        assertNotNull(container)
        verify(defaultArgumentsContainerPopulator).addReportDir(any(), any())
        verify(defaultArgumentsContainerPopulator).addSourceDir(any())
        verify(defaultArgumentsContainerPopulator).addTargetClasses(any(), any())
    }
}
