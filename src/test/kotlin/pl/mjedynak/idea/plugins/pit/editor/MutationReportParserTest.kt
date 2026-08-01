package pl.mjedynak.idea.plugins.pit.editor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile

class MutationReportParserTest {
    private val mutationReportParser = MutationReportParser()

    @Test
    fun `should parse mutation records from valid report file`() {
        val xmlFile = createTempReportFile(VALID_REPORT_XML)

        val records = mutationReportParser.parse(xmlFile)

        assertEquals(2, records.size)
        val firstRecord = records[0]
        assertEquals("Calculator.java", firstRecord.sourceFile)
        assertEquals("calculator.Calculator", firstRecord.mutatedClass)
        assertEquals(6, firstRecord.lineNumber)
        assertEquals(MutationStatus.KILLED, firstRecord.status)
        assertTrue(firstRecord.detected)
        assertEquals("add", firstRecord.mutatedMethod)
        assertEquals("Replaced integer addition with subtraction", firstRecord.description)
        val secondRecord = records[1]
        assertEquals("calculator.Calculator", secondRecord.mutatedClass)
        assertEquals(10, secondRecord.lineNumber)
        assertEquals(MutationStatus.SURVIVED, secondRecord.status)
    }

    @Test
    fun `should parse mutation records from input stream`() {
        val records = mutationReportParser.parse(ByteArrayInputStream(VALID_REPORT_XML.toByteArray()))

        assertEquals(2, records.size)
        assertEquals("Calculator.java", records[0].sourceFile)
        assertEquals("calculator.Calculator", records[0].mutatedClass)
        assertEquals(6, records[0].lineNumber)
        assertEquals(MutationStatus.KILLED, records[0].status)
        assertTrue(records[0].detected)
        assertEquals("add", records[0].mutatedMethod)
        assertEquals("Replaced integer addition with subtraction", records[0].description)
        assertEquals(MutationStatus.SURVIVED, records[1].status)
    }

    @Test
    fun `should skip mutations with missing or blank line number`() {
        val xmlFile = createTempReportFile(REPORT_XML_WITH_INVALID_LINE_NUMBERS)

        val records = mutationReportParser.parse(xmlFile)

        assertEquals(1, records.size)
        assertEquals("calculator.Calculator", records[0].mutatedClass)
        assertEquals(6, records[0].lineNumber)
    }

    @Test
    fun `should keep mutations without test coverage`() {
        val xmlFile = createTempReportFile(REPORT_XML_WITH_NO_COVERAGE_MUTATION)

        val records = mutationReportParser.parse(xmlFile)

        assertEquals(3, records.size)
        val noCoverageRecord = records.single { it.lineNumber == 14 }
        assertEquals(MutationStatus.NO_COVERAGE, noCoverageRecord.status)
        assertTrue(!noCoverageRecord.detected)
        val survivedRecord = records.single { it.lineNumber == 10 }
        assertEquals(MutationStatus.SURVIVED, survivedRecord.status)
    }

    @Test
    fun `should return empty list for empty file`() {
        val xmlFile = createTempReportFile("")

        val records = mutationReportParser.parse(xmlFile)

        assertTrue(records.isEmpty())
    }

    @Test
    fun `should return empty list for garbage input`() {
        val xmlFile = createTempReportFile("this is not xml at all")

        val records = mutationReportParser.parse(xmlFile)

        assertTrue(records.isEmpty())
    }

    private fun createTempReportFile(content: String): File {
        val xmlFile = createTempFile(createTempDirectory(), "mutations", ".xml").toFile()
        xmlFile.writeText(content)
        xmlFile.deleteOnExit()
        return xmlFile
    }

    private companion object {
        val VALID_REPORT_XML =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <mutations>
              <mutation detected='true' status='KILLED' numberOfTestsRun='1'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <mutatedMethod>add</mutatedMethod>
                <methodDescription>(II)I</methodDescription>
                <lineNumber>6</lineNumber>
                <mutator>org.pitest.mutationtest.engine.gregor.mutators.MathMutator</mutator>
                <indexes><index>8</index></indexes>
                <blocks><block>0</block></blocks>
                <killingTest>calculator.CalculatorTest.shouldAddTwoNumbers</killingTest>
                <description>Replaced integer addition with subtraction</description>
              </mutation>
              <mutation detected='false' status='SURVIVED' numberOfTestsRun='1'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <mutatedMethod>subtract</mutatedMethod>
                <methodDescription>(II)I</methodDescription>
                <lineNumber>10</lineNumber>
                <mutator>org.pitest.mutationtest.engine.gregor.mutators.MathMutator</mutator>
                <indexes><index>12</index></indexes>
                <blocks><block>0</block></blocks>
                <killingTest/>
                <description>Replaced integer subtraction with addition</description>
              </mutation>
            </mutations>
            """.trimIndent()

        val REPORT_XML_WITH_NO_COVERAGE_MUTATION =
            """
            <mutations>
              <mutation detected='true' status='KILLED' numberOfTestsRun='1'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber>6</lineNumber>
              </mutation>
              <mutation detected='false' status='NO_COVERAGE' numberOfTestsRun='0'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber>14</lineNumber>
              </mutation>
              <mutation detected='false' status='SURVIVED' numberOfTestsRun='1'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber>10</lineNumber>
              </mutation>
            </mutations>
            """.trimIndent()

        val REPORT_XML_WITH_INVALID_LINE_NUMBERS =
            """
            <mutations>
              <mutation detected='true' status='KILLED'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber>6</lineNumber>
              </mutation>
              <mutation detected='true' status='KILLED'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber></lineNumber>
              </mutation>
              <mutation detected='true' status='KILLED'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
              </mutation>
              <mutation detected='true' status='KILLED'>
                <sourceFile>Calculator.java</sourceFile>
                <mutatedClass>calculator.Calculator</mutatedClass>
                <lineNumber>not-a-number</lineNumber>
              </mutation>
            </mutations>
            """.trimIndent()
    }
}
