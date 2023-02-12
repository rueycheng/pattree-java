import java.io.*;

import jackteng.pattree.*;
import jackteng.util.*;
import jackteng.file.*;

import org.apache.commons.cli.*;

public class TermExtractor {
    public static void main(String[] args) {
	Option[] allOptions = new Option[] {
	    OptionBuilder.withLongOpt("help")
		.withDescription("Show this help screen")
		.create(),
	    OptionBuilder.withLongOpt("input")
		.withDescription("Path to the input (corpus) directory")
		.hasArg().withArgName("dir").create(),
	    OptionBuilder.withLongOpt("output")
		.withDescription("Path to the output directory")
		.hasArg().withArgName("dir").create(),
	    OptionBuilder.withLongOpt("measure")
		.withDescription("Use the association measure: SCPCD, SCP, GMSCP, GMSCPCD, CD")
		.hasArg().withArgName("name").create(),
	    OptionBuilder.withLongOpt("min-freq")
		.withDescription("Specify the minimum term frequency")
		.hasArg().withArgName("num").create(),
	    OptionBuilder.withLongOpt("max-n")
		.withDescription("Specify the maximum size of the n-gram")
		.hasArg().withArgName("num").create(),
	    OptionBuilder.withLongOpt("window-size")
		.withDescription("Specify the window size")
		.hasArg().withArgName("num").create(),
	};

	Options options = new Options();
	for (Option option: allOptions) options.addOption(option);

	CommandLineParser parser = new GnuParser();
	CommandLine line = null;

	try {
	    line = parser.parse(options, args);
	}
	catch (ParseException e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}

	if (line.hasOption("help")) {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(TermExtractor.class.getName() + " [option..]", options);
	    System.exit(0);
	}

	//--------------------------------------------------
	// Now set up options
	//-------------------------------------------------- 
	String inputPath = line.getOptionValue("input");
	String outputPath = line.getOptionValue("output");
	int minFreq = Integer.parseInt(line.getOptionValue("min-freq", "2"));
	int maxN = Integer.parseInt(line.getOptionValue("max-n", "12"));
	int windowSize = Integer.parseInt(line.getOptionValue("window-size", "1"));
	int langType = Strings.ChineseLike; // Dirty

	if (inputPath == null) {
	    System.err.println("The argument '--input' is required");
	    System.exit(1);
	}

	if (outputPath == null) {
	    System.err.println("The argument '--output' is required");
	    System.exit(1);
	}

	String measure = line.getOptionValue("measure", "SCPCD");
	int associationMeasure = 
	    (measure == "SCPCD")? PATTermExtraction.SCPCD:
	    (measure == "SCP")? PATTermExtraction.SCP:
	    (measure == "GMSCP")? PATTermExtraction.GMSCP:
	    (measure == "GMSCPCD")? PATTermExtraction.GMSCPCD:
	    (measure == "CD")? PATTermExtraction.CD: PATTermExtraction.SCPCD;

	//--------------------------------------------------
	// Go!
	//-------------------------------------------------- 
	PATTermExtraction te = null;

	File inputDir = new File(inputPath);
	if (!inputDir.isDirectory()) {
	    System.err.println(inputDir.getAbsolutePath() + " is not a directory");
	    System.exit(1);
	}

	File outputDir = new File(outputPath);
	if (!outputDir.exists()) outputDir.mkdirs();

	FileHandler treeFile = new FileHandler(outputDir, "tree");

	try {
	    if (treeFile.exists())
		te = (PATTermExtraction)treeFile.readObject();
	    else {
		te = new PATTermExtraction(inputDir.getAbsolutePath(), langType);
		te.toFile(treeFile.getAbsolutePath());
	    }

	    te.extract(associationMeasure, minFreq, maxN, windowSize, outputPath);
	}
	catch (Exception e) { e.printStackTrace(); }
    }
}
