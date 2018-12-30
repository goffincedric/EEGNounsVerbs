package stimuli.services.options

import java.io.{File, FileWriter, IOException, Writer}

import com.google.gson.{GsonBuilder, JsonParser}
import stimuli.model.option.AnalysisOption

import scala.io.Source


/**
  * @author Cédric Goffin & Thomas Verhoeven
  *         07/12/2018 09:23
  *
  */
class OptionsService(val optionsPath: String) {
    private val gson = new GsonBuilder().create()
    private val optionsFileName = "options.json"
    private val fullPath =
        if (optionsPath.endsWith("/") || optionsPath.endsWith("\\")) {
            optionsPath + optionsFileName
        } else {
            optionsPath + "/" + optionsFileName
        }

    def getOptions: Array[AnalysisOption] = {
        val d = new File(fullPath) // Get options file
        if (d.exists && d.isFile) {
            val json_string = Source.fromFile(fullPath).getLines.mkString
            val jsonStringAsArray = new JsonParser().parse(json_string).getAsJsonArray
            gson.fromJson(jsonStringAsArray, classOf[Array[AnalysisOption]])
        } else {
            val defaultOptions = getDefaultOptions
            writeOptions(defaultOptions)
            defaultOptions
        }
    }

    def getOption(name: String): AnalysisOption = {
        getOptions.filter(o => o.name.equals(name)).head
    }

    private def getDefaultOptions: Array[AnalysisOption] = {
        Array(
            // Sliding Window Pane
            new AnalysisOption("SlidingRangeSizeIncrement", "1000"),
            new AnalysisOption("SlidingMaxRangeSize", "4000"),
            new AnalysisOption("SlidingSplitPointMs", "2000"),
            new AnalysisOption("SlidingWindowOneMs", "10"),
            new AnalysisOption("SlidingSizeWindowOne", "4"),
            new AnalysisOption("SlidingWindowTwoMs", "100"),
            new AnalysisOption("SlidingSizeWindowTwo", "1"),
            // Normal Distribution Pane
            new AnalysisOption("NormalDistSplitPointMs", "2000"),
            new AnalysisOption("NormalDistWindowOneMs", "10"),
            new AnalysisOption("NormalDistSizeWindowOne", "4"),
            new AnalysisOption("NormalDistProbTriggerWindowOne", "0.95"),
            new AnalysisOption("NormalDistWindowTwoMs", "100"),
            new AnalysisOption("NormalDistSizeWindowTwo", "1"),
            new AnalysisOption("NormalDistProbTriggerWindowTwo", "0.97")
        )
    }

    def writeOptions(options: Array[AnalysisOption]): Unit = {
        val dir = new File(optionsPath)
        if (!dir.exists()) dir.mkdirs()

        val json = gson.toJson(options, classOf[Array[AnalysisOption]])
        val w: Writer = new FileWriter(fullPath)
        try {
            w.write(json)
            w.close()
        } catch {
            case e: IOException => e.printStackTrace()
        }
    }
}
