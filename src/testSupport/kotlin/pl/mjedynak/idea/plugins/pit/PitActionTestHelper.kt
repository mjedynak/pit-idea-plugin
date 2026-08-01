package pl.mjedynak.idea.plugins.pit

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

object PitActionTestHelper {
    @JvmStatic
    fun verifyActionUpdateForSourceClass(project: Project): String {
        val diagnostics = StringBuilder()
        try {
            diagnostics.append("Step 1: Getting RunAllPitAction...\n")
            val actionManager = ActionManager.getInstance()
            val action =
                actionManager.getAction("pitest.RunAllPit")
                    ?: return "ERROR\nRunAllPitAction not found"

            diagnostics.append("Step 2: Getting module...\n")
            val modules = ModuleManager.getInstance(project).modules
            if (modules.isEmpty()) return "ERROR\nNo modules found"
            val module = modules[0]

            diagnostics.append("Step 3: Creating DataContext with project and module...\n")
            val dataContext =
                DataContext { dataId ->
                    when (dataId) {
                        "project" -> project
                        "module" -> module
                        else -> null
                    }
                }

            diagnostics.append("Step 4: Creating AnActionEvent...\n")
            val event =
                AnActionEvent.createEvent(
                    dataContext,
                    null,
                    "ProjectViewPopupMenuRunGroup",
                    ActionUiKind.NONE,
                    null,
                )

            diagnostics.append("Step 5: Calling action.update() on EDT via invokeAndWait...\n")
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeAndWait {
                action.update(event)
            }

            diagnostics.append("Step 6: update() completed successfully\n")
            diagnostics.append("  isEnabledAndVisible: ").append(event.presentation.isEnabledAndVisible).append("\n")
            return "SUCCESS\n$diagnostics"
        } catch (e: Exception) {
            diagnostics
                .append("ERROR: ")
                .append(e.javaClass.name)
                .append(": ")
                .append(e.message)
                .append("\n")
            return "ERROR\n$diagnostics"
        }
    }

    @JvmStatic
    fun performActionForSourceClass(project: Project): String {
        val diagnostics = StringBuilder()
        try {
            diagnostics.append("Step 1: Getting RunAllPitAction...\n")
            val actionManager = ActionManager.getInstance()
            val action =
                actionManager.getAction("pitest.RunAllPit")
                    ?: return "ERROR\nRunAllPitAction not found"

            diagnostics.append("Step 2: Getting module...\n")
            val modules = ModuleManager.getInstance(project).modules
            if (modules.isEmpty()) return "ERROR\nNo modules found"
            val module = modules[0]

            diagnostics.append("Step 3: Creating DataContext with project and module...\n")
            val dataContext =
                DataContext { dataId ->
                    when (dataId) {
                        "project" -> project
                        "module" -> module
                        else -> null
                    }
                }

            diagnostics.append("Step 4: Creating AnActionEvent...\n")
            val event =
                AnActionEvent.createEvent(
                    dataContext,
                    null,
                    "ProjectViewPopupMenuRunGroup",
                    ActionUiKind.NONE,
                    null,
                )

            diagnostics.append("Step 5: Calling action.actionPerformed() on EDT...\n")
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeAndWait {
                action.actionPerformed(event)
            }

            diagnostics.append("Step 6: actionPerformed() completed (PIT started asynchronously)\n")
            return "SUCCESS\n$diagnostics"
        } catch (e: Exception) {
            diagnostics
                .append("ERROR: ")
                .append(e.javaClass.name)
                .append(": ")
                .append(e.message)
                .append("\n")
            return "ERROR\n$diagnostics"
        }
    }
}
