package com.trust.fog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ServerReader sReader = new ServerReader(Paths.get(System.getProperty("user.dir") + "/servers.txt"));
        try {
            sReader.readFromServers();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        String requestor = sReader.getRequestor();
        List<String> recommendorsList = sReader.getRecommendorsList();
        Map<String, Server> sMap = sReader.getServerMap();
        List<Server> recommendors = recommendorsList.stream().map(s -> sMap.get(s)).collect(Collectors.toList());

        float pastUnweightedDirectTrust = 0, pastCosineUnweightedIndirectTrust = 0, pastJaccardUnweightedIndirectTrust = 0;
        float pastAlpha = 0, pastBeta = 0;
        List<String> linesForAverage = new ArrayList<>();
        try {
            linesForAverage = Files.lines(Paths.get(System.getProperty("user.dir") + "/trust.txt")).collect(Collectors.toList());
        } catch (IOException e) {
        }
        for (String line: linesForAverage) {
            if (line.equals("")) {
                continue;
            }
            String[] parts = line.split(":");
            String key = parts[0], value = parts[1];
            if (key.equals("Past Alpha")) {
                pastAlpha = Float.parseFloat(value);
            } else if (key.equals("Past Beta")) {
                pastBeta = Float.parseFloat(value);
            } else if (key.equals("Unweighted Direct trust")) {
                pastUnweightedDirectTrust = Float.parseFloat(value);
            } else if (key.equals("Cosine Unweighted Indirect Trust")) {
                pastCosineUnweightedIndirectTrust = Float.parseFloat(value);
            } else if (key.equals("Jaccard Unweighted Indirect Trust")) {
                pastJaccardUnweightedIndirectTrust = Float.parseFloat(value);
            }
        }

        StringBuilder sBuilder = new StringBuilder("\n");

        Scanner s = new Scanner(System.in);
        System.out.println("Enter experience.");
        float experience = s.nextFloat();
        float directTrust = TrustUtilities.directTrust(experience, 7200, pastAlpha, pastBeta);
        System.out.println("Direct trust: " + directTrust);
        sBuilder.append("Unweighted Direct trust: " + directTrust + "\n");
        System.out.println("Direct trust: " + directTrust);

        App app = new App();
        app.computeCosineValues(sMap, requestor, recommendors, directTrust, sBuilder, pastUnweightedDirectTrust, pastCosineUnweightedIndirectTrust);
        app.computeJaccardValues(sMap, requestor, recommendors, directTrust, sBuilder, pastUnweightedDirectTrust, pastJaccardUnweightedIndirectTrust);

        sBuilder.append("Past Alpha: " + TrustUtilities.getPositiveExperience(experience, 7200, pastAlpha, pastBeta) + "\n");
        sBuilder.append("Past Beta: " + TrustUtilities.getNegativeExperience(experience, 7200, pastAlpha, pastBeta) + "\n");
        sBuilder.append("\n");
        app.appendUsingFileWriter(System.getProperty("user.dir") + "\\Trust.txt", sBuilder.toString());
    }

    private void appendUsingFileWriter(String filePath, String text) {
		File file = new File(filePath);
		FileWriter fr = null;
		try {
			fr = new FileWriter(file, true);
			fr.write(text);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    private void computeCosineValues(Map<String, Server> sMap,
        String requestor,
        List<Server> recommendors,
        float directTrust,
        StringBuilder sBuilder,
        float pastUnweightedDirectTrust,
        float pastCosineUnweightedIndirectTrust) {
        TrustUtilities.finalCardinality(sMap.get(requestor), recommendors, Method.COSINE);
        Float cosineIndirectTrust = TrustUtilities.indirectTrust(sMap.get(requestor), directTrust);
        sBuilder.append("Cosine Unweighted Indirect Trust: " + cosineIndirectTrust + "\n");
        System.out.println("Indirect trust using cosine: " + cosineIndirectTrust);
        

        Float cosineTotalTrust = TrustUtilities.totalTrust(directTrust, cosineIndirectTrust);
        System.out.println("Total trust using cosine: " + cosineTotalTrust);
        sBuilder.append("Cosine Unweighted Total Trust: " + cosineTotalTrust + "\n");

        Float cosineDirectWeight = TrustUtilities.getEntropyDirectWeight(pastUnweightedDirectTrust, pastCosineUnweightedIndirectTrust);
        Float cosineIndirectWeight = TrustUtilities.getEntropyIndirectWeight(pastUnweightedDirectTrust, pastCosineUnweightedIndirectTrust);
        System.out.println("Direct Weight using cosine: " + cosineDirectWeight);
        System.out.println("Indirect Weight using cosine: " + cosineIndirectWeight);

        sBuilder.append("Direct Weight using cosine: " + cosineDirectWeight + "\n");
        sBuilder.append("Indirect Weight using cosine: " + cosineIndirectWeight + "\n");
        float weightedDirectTrustCosine = TrustUtilities.getWeightedDirectTrust(directTrust, cosineDirectWeight);
        float weightedIndirectTrustCosine = TrustUtilities.getWeightedIndirectTrust(cosineIndirectTrust, cosineIndirectWeight);
            
        sBuilder.append("Cosine Direct: " + weightedDirectTrustCosine + "\n");
        System.out.println("Cosine Weighted Direct trust: " + weightedDirectTrustCosine);
        sBuilder.append("Cosine Indirect: " + weightedIndirectTrustCosine + "\n");
        System.out.println("Weighted Indirect trust using cosine: " + weightedIndirectTrustCosine);

        float weightedTotalCosine = TrustUtilities.weightedTotalTrust(weightedDirectTrustCosine, weightedIndirectTrustCosine);
        sBuilder.append("Cosine Total: " + weightedTotalCosine + "\n");
        System.out.println("Weighted total trust using cosine: " + weightedTotalCosine);
        System.out.println();
    }

    private void computeJaccardValues(Map<String, Server> sMap,
        String requestor,
        List<Server> recommendors,
        float directTrust,
        StringBuilder sBuilder,
        float pastUnweightedDirectTrust,
        float pastJaccardUnweightedIndirectTrust) {
        TrustUtilities.finalCardinality(sMap.get(requestor), recommendors, Method.JACCARD);
        Float indirectTrustJaccard = TrustUtilities.indirectTrust(sMap.get(requestor), directTrust);
        sBuilder.append("Jaccard Unweighted Indirect Trust: " + indirectTrustJaccard + "\n");
        System.out.println("Indirect trust using Jaccard: " + indirectTrustJaccard);

        Float totalTrustJaccard = TrustUtilities.totalTrust(directTrust, indirectTrustJaccard);
        System.out.println("Total trust using Jaccard: " + totalTrustJaccard);
        sBuilder.append("Jaccard Unweighted Total Trust: " + totalTrustJaccard + "\n");


        Float jaccardDirectWeight = TrustUtilities.getEntropyDirectWeight(pastUnweightedDirectTrust, pastJaccardUnweightedIndirectTrust);
        Float jaccardIndirectWeight = TrustUtilities.getEntropyIndirectWeight(pastUnweightedDirectTrust, pastJaccardUnweightedIndirectTrust);
        System.out.println("Direct Weight using jaccard: " + jaccardDirectWeight);
        System.out.println("Indirect Weight using jaccard: " + jaccardIndirectWeight);
        sBuilder.append("Direct Weight using jaccard: " + jaccardDirectWeight + "\n");
        sBuilder.append("Indirect Weight using jaccard: " + jaccardIndirectWeight + "\n");

        float weightedDirectTrustJaccard = TrustUtilities.getWeightedDirectTrust(directTrust, jaccardDirectWeight);
        float weightedIndirectTrustJaccard = TrustUtilities.getWeightedIndirectTrust(indirectTrustJaccard, jaccardIndirectWeight);

        sBuilder.append("Jaccard Direct: " + weightedDirectTrustJaccard + "\n");
        System.out.println("Jaccard Weighted Direct trust: " + weightedDirectTrustJaccard);
        sBuilder.append("Jaccard Indirect: " + weightedIndirectTrustJaccard + "\n");
        System.out.println("Weighted Indirect trust using Jaccard: " + weightedIndirectTrustJaccard);

        float weightedTotalJaccard = TrustUtilities.weightedTotalTrust(weightedDirectTrustJaccard, weightedIndirectTrustJaccard);
        sBuilder.append("Jaccard Total: " + weightedTotalJaccard + "\n");
        System.out.println("Weighted total trust using Jaccard: " + weightedTotalJaccard);
    }

}
