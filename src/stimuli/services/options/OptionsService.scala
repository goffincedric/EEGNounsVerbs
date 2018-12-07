package stimuli.services.options

import java.io.File

import com.google.gson.GsonBuilder
//import stimuli.model.option.AnalysisOption

import scala.io.Source


/**
  * @author Cédric Goffin
  *         07/12/2018 09:23
  *
  */
class OptionsService(val optionsPath: String) {
//    private def writeOptions(options: Vector[AnalysisOption]): Unit = {
//        val gsonBuilder = new GsonBuilder().registerTypeAdapter()
//    }
//
//    private def getListOfCSVFiles: List[File] = {
//        val d = new File(optionsPath) // Get directory with csv files
//        if (d.exists && d.isDirectory) {
//            // Get all files that have compatible names
//            d.listFiles.filter(_.isFile).filter(_.getName.endsWith(endsWith)).toList
//        } else {
//            // Return empty list of files
//            List[File]()
//        }
//    }
//
//    private def readOptionsFile(file: File): Vector[AnalysisOption] = {
//        val bufferedSource = Source.fromFile(file) // Open reader to file
//        val lines = bufferedSource.getLines().toVector // Get lines from file
//        bufferedSource.close // Close reader
//
//        // Convert lines to Stimulus objects and return sorted by StimulusType
//        splitNounsVerbs(linesToStimuli(lines.tail, contact_points))
//    }


}
