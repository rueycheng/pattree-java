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
	String outputPath = line.getOptionValue("output");
	int minFreq = Integer.parseInt(line.getOptionValue("min-freq", "2"));
	int maxN = Integer.parseInt(line.getOptionValue("max-n", "12"));
	int windowSize = Integer.parseInt(line.getOptionValue("window-size", "1"));

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

	File outputDir = new File(outputPath);
	if (!outputDir.exists()) outputDir.mkdirs();

	FileHandler treeFile = new FileHandler(outputDir, "tree");

	if (treeFile.exists())
	    te = (PATTermExtraction)treeFile.readObject();
	else {
	    try {
		te = new PATTermExtraction(outputDir.getAbsolutePath(), Strings.ChineseLike);
	    }
	    catch (Exception e) { e.printStackTrace(); }
	}
    }
}
